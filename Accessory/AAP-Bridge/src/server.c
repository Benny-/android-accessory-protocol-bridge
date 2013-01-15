#include <stdio.h>
#include <pthread.h> //threading
#include <string.h> //memset
#include <stdint.h>
#include <accessory.h>
#include "Message/handlemessage.h"
#include "Message/receivequeue.h"
#include "Message/sendqueue.h"

pthread_t receive = 0, send = 0;

static volatile int work;
volatile int connectedToAndroid = 0;
static AapConnection* con = NULL;

static

/**
 * Accessory receive thread
 * called by "starthandeling"
 */
void* receiver(void* user_data) {
	int error =0;
	uint8_t buffer[1024];
	while(work==1 && !error)
	{
		memset(buffer, 0, sizeof(buffer));
		short port;
		short length;

		error = readAllAccessory(con,buffer,4);

		if(!error)
		{
			port   = buffer[0] + (buffer[1] << 8);
			length = buffer[2] + (buffer[3] << 8);
			printf("Received multiplexed msg for port %hu length %hu\n",port, length);
			error = readAllAccessory(con, buffer, length);
			if (!error)
			{
				PrintBin(buffer, length);
				puts("");
			}
			//decodemessage(buffer);
		}
	}
	addreceivequeue(NULL);
	work = 0;
	fprintf(stderr,"Receiver thread has stopped\n");
	return NULL;
}

/**
 * Accessory send thread called by "starthandeling"
 */
void* sender(void* user_data) {
	int transferred=0;
	int error;

	connectedToAndroid = 1;
	while(work==1) {
		MESSAGE* buffer = pollSendQueue();

		if(buffer == NULL)
		{
			fprintf(stderr,"Sender thread going to make a graceful exit\n");
			break;
		}

		//decodemessage(buffer);
		error = writeAllAccessory(con, buffer, sizeof(MESSAGE) );

		printf("Bytes send: %zu\n",sizeof(MESSAGE));
		PrintBin(buffer, sizeof(MESSAGE));
		puts("\n");

		if (error) {
			fprintf(stderr,"Error writing to accessory\n");
			// Our device disconnected, stop the loop
			work = 0;
			break;
		}
	}
	connectedToAndroid = 0;
	fprintf(stderr, "Sender thread has stopped\n");
	return NULL;
}

/**
 * Creates the Android accessory two threads for reading and writing on
 * the Android Accessory bus it also initialize the send/receive queue
 */
void initServer(AapConnection* newCon){
	work = 1;
	con = newCon;

	//initialize the send and receive queue
	initreceiveQueue();
	initSendQueue();

	pthread_create(&receive, NULL, receiver, NULL);
	pthread_create(&send, NULL, sender, NULL);
}

AapConnection* getCurrentConnection()
{
	return con;
}

void deInitServer()
{
	pthread_join(receive,NULL);
	pthread_join(send,NULL);
	receive = 0;
	send = 0;

	deInitSendQueue();
	deInitreceiveQueue();
}
