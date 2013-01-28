#include <stdio.h>
#include <pthread.h> //threading
#include <string.h> //memset
#include <stdint.h>
#include <stdlib.h>

#include "server.h"
#include "BridgeService.h"

#include "servicespawner.h"
#include "keepalive.h"
#include "Message/receivequeue.h"
#include "Message/sendqueue.h"

#define MAX_PORTS 400

struct BridgeConnection
{
	AapConnection* con;
	BridgeService services[MAX_PORTS];
	BridgeService* portStatusService;
	short lastAllocatedPort;
	pthread_t receive, send;
	volatile int8_t work;
	volatile int8_t connectedToAndroid;
};

void sendToCorrectService(BridgeConnection* bridge, short port, void* data, short size)
{
	if(bridge->services[port].port == -1)
	{
		fprintf(stderr, "Received data on port %i, but port is closed\n",port);
	}
	else
	{
		BridgeService* service = &bridge->services[port];
		service->onBytesReceived(service->service_data, service, data, size);
	}
}

void writeAllPort	(BridgeService* service, const void* buffer, int size)
{
	MultiplexedMessage* msg = malloc(sizeof(MultiplexedMessage));
	msg->port = service->port;
	msg->size = size;
	msg->data = malloc(size);
	memcpy(msg->data, buffer, size);
	addSendQueue(msg);
}

/**
 * Returns NULL if all ports are in use.
 */
BridgeService* openNewPort(BridgeConnection* bridge)
{
	short port = bridge->lastAllocatedPort + 1;

	while(bridge->services[port].port != -1 && port != bridge->lastAllocatedPort)
	{
		port = (port + 1) % ( MAX_PORTS );
	}

	if(bridge->services[port].port == -1)
	{
		BridgeService* service = &bridge->services[port];
		service->port = port;
		bridge->lastAllocatedPort = port;
		return service;
	}

	return NULL;
}

short getPortNr(BridgeService service)
{
	service.port;
}

/**
 * Accessory receive thread
 * called by "starthandeling"
 */
void* receiver(void* user_data) {
	BridgeConnection* bridge = user_data;
	int error =0;
	uint8_t buffer[4000];
	while(bridge->work==1 && !error)
	{
		memset(buffer, 0, sizeof(buffer));
		short port;
		short size;

		error = readAllAccessory(bridge->con,buffer,4);

		if(!error)
		{
			port = buffer[0] + (buffer[1] << 8);
			size = buffer[2] + (buffer[3] << 8);
			error = readAllAccessory(bridge->con, buffer, size);
			if (!error)
			{
#ifdef DEBUG
				printf("PORT %hu: received %hu bytes\n",port, size);
				PrintBin(buffer, size);
				puts("");
#endif

				MultiplexedMessage* msg = malloc(sizeof(MultiplexedMessage));
				msg->port = port;
				msg->size = size;
				msg->data = malloc(size);
				memcpy(msg->data, buffer, size);
				addreceivequeue(msg);
			}
		}
	}
	fprintf(stderr, "Receiver thread going to stop Work:%i Error:%i\n",bridge->work, error);
	addreceivequeue(NULL);
	bridge->work = 0;
	fprintf(stderr,"Receiver thread has stopped\n");
	return NULL;
}

/**
 * Accessory send thread called by "starthandeling"
 */
void* sender(void* user_data) {
	BridgeConnection* bridge = user_data;
	int transferred=0;
	int error;

	bridge->connectedToAndroid = 1;
	while(bridge->work==1) {
		MultiplexedMessage* msg = pollSendQueue();

		if(msg == NULL)
		{
			fprintf(stderr,"Sender thread going to make a graceful exit\n");
			break;
		}

		char header[4];
		header[0] = msg->port;
		header[1] = msg->port >> 8;
		header[2] = msg->size;
		header[3] = msg->size >> 8;

		error = writeAllAccessory(bridge->con, header, sizeof(header) );
		if(!error)
			error = writeAllAccessory(bridge->con, msg->data, msg->size );

		if (error) {
			fprintf(stderr,"Error writing to accessory\n");
			// Our device disconnected, stop the loop
			bridge->work = 0;
			break;
		}
		else
		{
#ifdef DEBUG
			printf("PORT %hu: send %hu bytes\n",msg->port, msg->size);
			PrintBin(msg->data, msg->size);
			puts("");
#endif
		}
	}
	bridge->connectedToAndroid = 0;
	fprintf(stderr, "Sender thread has stopped\n");
	return NULL;
}

#define STREAM_EOF    0x03
#define STREAM_CLOSE  0x04

/**
 * Remember the difference between "close", "eof" and "cleanup".
 *
 * "close" and "eof" are signals. They can be send from services on one end of the port to the other service on the other remote device.
 *
 * "Eof"  : "I will no longer send bytes and will consider my output to be closed".
 * "Close": "I will no longer expect bytes and will consider my input to be closed"
 *
 * Once both output and input on a port are closed, a cleanup will occur to free all resources and make the port ready for reuse.
 * This situation can occur in the following ways (but not limited to, as all arrows may be reversed and ordered differently):
 *
 * android_service --EOF--> accessory_service
 * android_service --CLOSE--> accessory_service
 * Cleanup now occurs on both sides.
 *
 * android_service --EOF--> accessory_service
 * android_service <--EOF-- accessory_service
 * Cleanup now occurs on both sides.
 *
 * android_service --CLOSE--> accessory_service
 * android_service <--CLOSE-- accessory_service
 * Cleanup now occurs on both sides.
 *
 */
static void CleanupService(BridgeService* service)
{
	if(service->inputOpen || service->outputOpen)
	{
		fprintf(stderr, "I'm not cleaning up a  service who's input or output are still open");
	}
	else
	{
		printf("Cleanup for port %hi\n",service->port);

		service->onCleanupService(service->service_data, service);
		service->port = -1;
		service->inputOpen = 1;
		service->outputOpen = 1;
		service->service_data = NULL;
		service->onBytesReceived = NULL;
		service->onEof = NULL;
		service->onCleanupService = NULL;
	}
}

static void* PortStatusInit(BridgeConnection* bridge)
{
	return bridge;
}

static void PortStatusOnBytesReceived(void* service_data, BridgeService* service, void* buffer, int size)
{
	short port; // <--- This is the port above defines are talking about.
	int8_t* casted_buffer = buffer;
	if(size != 4)
		fprintf(stderr, "Port status messages should be 4 bytes, but received one of %i",size);

	port = casted_buffer[0] + (casted_buffer[1] << 8);
	BridgeService* target_service = &service->bridge->services[port];
	if(target_service->port == -1)
	{
		fprintf(stderr, "PORT_STATUS ignoring new status for closed port: %hi \n",port);
	}
	else
	{
		switch(casted_buffer[2])
		{
			case STREAM_EOF:
				printf("PORT %hi received EOF\n", port);
				if(!target_service->inputOpen)
					fprintf(stderr,"PORT %hi received a double EOF", port);
				target_service->inputOpen = 0;
				target_service->onEof(target_service->service_data, target_service);
				break;
			case STREAM_CLOSE:
				printf("PORT %hi received CLOSE\n", port);
				if(!target_service->outputOpen)
					fprintf(stderr,"PORT %hi received a double CLOSE", port);
				target_service->outputOpen = 0;
				break;
			default:
				fprintf(stderr,"PORT %hi received unknown status %hhi", port, casted_buffer[3]);
				break;
		}
		if(!target_service->inputOpen && !target_service->outputOpen)
		{
			CleanupService(target_service);
		}
	}
}

static void PortStatusOnEof(void* service_data, BridgeService* service)
{

}

static void PortStatusCleanup(void* service_data, BridgeService* service)
{

}

void sendEof		(BridgeService* service)
{
	int8_t buffer[4];
	buffer[0] = service->port;
	buffer[1] = service->port >> 8;
	buffer[3] = STREAM_EOF;
	buffer[4] = 0;
	service->outputOpen = 0;
	writeAllPort(service->bridge->portStatusService, buffer, sizeof(buffer));

	if(!service->inputOpen && !service->outputOpen)
	{
		CleanupService(service);
	}
}

void sendClose	(BridgeService* service)
{
	int8_t buffer[4];
	buffer[0] = service->port;
	buffer[1] = service->port >> 8;
	buffer[3] = STREAM_CLOSE;
	buffer[4] = 0;
	service->inputOpen = 0;
	writeAllPort(service->bridge->portStatusService, buffer, sizeof(buffer));

	if(!service->inputOpen && !service->outputOpen)
	{
		CleanupService(service);
	}
}

/**
 * Creates the Android accessory two threads for reading and writing on
 * the Android Accessory bus it also initialize the send/receive queue
 */
BridgeConnection* initServer(AapConnection* con){
	BridgeConnection* bridge = malloc(sizeof(BridgeConnection));
	bridge->work = 1;
	bridge->connectedToAndroid = 0;
	bridge->con = con;

	for(int i = 0; i < (sizeof(bridge->services) / sizeof(bridge->services[0])); i++)
	{
		bridge->services[i].port = -1;
		bridge->services[i].bridge = bridge;
		bridge->services[i].inputOpen = 1;
		bridge->services[i].outputOpen = 1;
		bridge->services[i].service_data = NULL;
		bridge->services[i].onBytesReceived = NULL;
		bridge->services[i].onEof = NULL;
		bridge->services[i].onCleanupService = NULL;
	}

	{	bridge->portStatusService = &bridge->services[0];
		bridge->portStatusService->port = 0;
		bridge->portStatusService->service_data = PortStatusInit(bridge);
		bridge->portStatusService->onBytesReceived = &PortStatusOnBytesReceived;
		bridge->portStatusService->onEof = &PortStatusOnEof;
		bridge->portStatusService->onCleanupService = &PortStatusCleanup;
	}

	{	BridgeService* servicespawner = &bridge->services[1];
		servicespawner->port = 1;
		servicespawner->service_data = ServiceSpawnerInit(bridge);
		servicespawner->onBytesReceived = &ServiceSpawnerOnBytesReceived;
		servicespawner->onEof = &ServiceSpawnerOnEof;
		servicespawner->onCleanupService = &ServiceSpawnerCleanup;
	}

	{	BridgeService* keepalive = &bridge->services[2];
		keepalive->port = 2;
		keepalive->service_data = KeepaliveInit();
		keepalive->onBytesReceived = &KeepaliveOnBytesReceived;
		keepalive->onEof = &KeepaliveOnEof;
		keepalive->onCleanupService = &KeepaliveOnEof;
	}

	bridge->lastAllocatedPort = 2;

	//initialize the send and receive queue
	initreceiveQueue();
	initSendQueue();

	pthread_create(&bridge->receive, NULL, receiver, bridge);
	pthread_create(&bridge->send, NULL, sender, bridge);
	return bridge;
}

void deInitServer(BridgeConnection* bridge)
{
	addSendQueue(NULL); // Signal for the send thread to stop.
	// We assume the receive thread already stopped due to a read error.

	pthread_join(bridge->receive,NULL);
	pthread_join(bridge->send,NULL);
	bridge->receive = 0;
	bridge->send = 0;

	deInitSendQueue();
	deInitreceiveQueue();
}
