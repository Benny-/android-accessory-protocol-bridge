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

struct BridgeConnection
{
	AapConnection* con;
	BridgeService services[400];
	short lastAllocatedPort;
	pthread_t receive, send;
	volatile int work;
	volatile int connectedToAndroid;
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

void sendEof		(BridgeService* service)
{
	fprintf(stderr, "sendEof() not implemented\n");
}

void closeService	(BridgeService* service)
{
	if(service->port < 0)
	{
		fputs("closeService() Service not active",stderr);
	}
	else
	{
		service->onCloseService(service->service_data, service);
		service->port = -1;
		service->onBytesReceived = NULL;
		service->onCloseService = NULL;
		service->onEof = NULL;
	}
}

/**
 * Returns NULL if all ports are in use.
 */
BridgeService* openNewPort(BridgeConnection* bridge)
{
	short port = bridge->lastAllocatedPort + 1;

	while(bridge->services[port].port != -1 && port != bridge->lastAllocatedPort)
		port++;

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
	uint8_t buffer[1024];
	while(bridge->work==1 && !error)
	{
		memset(buffer, 0, sizeof(buffer));
		short port;
		short size;

		error = readAllAccessory(bridge->con,buffer,4);

		if(!error)
		{
			port   = buffer[0] + (buffer[1] << 8);
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
	}

	{	BridgeService* servicespawner = &bridge->services[0];
		servicespawner->port = 0;
		servicespawner->bridge = bridge;
		servicespawner->service_data = ServiceSpawnerInit(bridge, servicespawner);
		servicespawner->onBytesReceived = &ServiceSpawnerOnBytesReceived;
		servicespawner->onCloseService = NULL;
	}

	{	BridgeService* keepalive = &bridge->services[1];
		keepalive->port = 1;
		keepalive->bridge = bridge;
		keepalive->service_data = NULL;
		keepalive->onBytesReceived = &KeepaliveOnBytesReceived;
		keepalive->onCloseService = NULL;
	}

	bridge->lastAllocatedPort = 1;

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
