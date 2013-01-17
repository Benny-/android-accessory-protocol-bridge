#include <stdio.h>
#include <pthread.h> //threading
#include <string.h> //memset
#include <stdint.h>
#include <stdlib.h>

#include "server.h"

#include "keepalive.h"
#include "Message/receivequeue.h"
#include "Message/sendqueue.h"

struct BridgeService
{
	/**
	 * The port this service is located on. It is negative if the service is down.
	 */
	short port;
	BridgeConnection* bridge;
	void* service_data;

	/**
	 * Called when the service receives some bytes from the android application.
	 */
	void (*onBytesReceived)	(void* service_data, BridgeService* service, const void* buffer, int size);

	/**
	 * Called when the service receives a eof from the android application. The onCloseService() function will not longer be called.
	 */
	void (*onEof)			(void* service_data, BridgeService* service);

	/**
	 * Called when the service should cleanup all service data. Service should not use the write function once this is called.
	 */
	void (*onCloseService)	(void* service_data, BridgeService* service);
};

struct BridgeConnection
{
	AapConnection* con;
	BridgeService ports[400];
	pthread_t receive, send;
	volatile int work;
	volatile int connectedToAndroid;
};

void sendToCorrectService(BridgeConnection* bridge, short port, const void* data, short size)
{
	if(bridge->ports[port].port == -1)
	{
		fprintf(stderr, "Received data on port %i, but port is closed\n",port);
	}
	else
	{
		BridgeService* service = &bridge->ports[port];
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

void sendEof		(BridgeService* bridge)
{

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
			printf("Received multiplexed msg for port %hu length %hu\n",port, size);
			error = readAllAccessory(bridge->con, buffer, size);
			if (!error)
			{
				MultiplexedMessage* msg = malloc(sizeof(MultiplexedMessage));
				msg->port = port;
				msg->size = size;
				msg->data = malloc(size);
				memcpy(msg->data, buffer, size);
				addreceivequeue(msg);
			}
		}
	}
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
			printf("Bytes send: %zu\n",msg->size);
			PrintBin(msg->data, msg->size);
			puts("");
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

	for(int i = 0; i < (sizeof(bridge->ports) / sizeof(bridge->ports[0])); i++)
	{
		bridge->ports[i].port = -1;
	}

	{	BridgeService* keepalive = &bridge->ports[1];
		keepalive->port = 1;
		keepalive->bridge = bridge;
		keepalive->service_data = NULL;
		keepalive->onBytesReceived = &KeepaliveOnBytesReceived;
		keepalive->onCloseService = NULL;
	}

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
