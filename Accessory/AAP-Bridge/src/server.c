#include <stdio.h>
#include <pthread.h> //threading
#include <string.h> //memset
#include "accessory.h"
#include "Message/handlemessage.h"
#include "Message/receivequeue.h"
#include "Message/sendqueue.h"

pthread_t receive = 0, send = 0;

static volatile int work = 1;
volatile int connectedToAndroid = 0;

/**
 * Accessory receive thread
 * called by "starthandeling"
 */
void* receiver(void* user_data) {
	uint8_t buffer[1024];
	int transferred = 0;
	int response = 0;
	while(work==1) {
		memset(buffer, 0, 64);

		response = readAccessory(buffer, &transferred);
		if(transferred > 0 && response >= 0) {
			//try to handle the message
			decodemessage(buffer);
		} else {
			printf("byte received: %i \n", transferred);
			error(response);

			if (response == LIBUSB_ERROR_NO_DEVICE) {
				// Our device disconnected, stop the loop
				printf("Receiver thread is going to stop\n");
				addreceivequeue(NULL);
				work = 0;
				break;
			}
		}
	}
	printf("Receiver thread has stopped\n");
	return NULL;
}

/**
 * Accessory send thread called by "starthandeling"
 */
void* sender(void* user_data) {
	int transferred=0;
	int response;

	connectedToAndroid = 1;
	while(work==1) {
		MESSAGE* buffer = pollSendQueue();

		if(buffer == NULL)
			break;

		//decodemessage(buffer);
		response = writeAccessory((uint8_t*)buffer, &transferred);
		if(response) {
			error(response);
			if (response == LIBUSB_ERROR_NO_DEVICE) {
				// Our device disconnected, stop the loop
				work = 0;
				break;
			}
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
void initServer(){
	pthread_create(&receive, NULL, receiver, NULL);
	pthread_create(&send, NULL, sender, NULL);

	//initialize the send and receive queue
	initreceiveQueue();
	initSendQueue();
}

void deInitServer()
{
	work = 1;
	pthread_join(receive,NULL);
	pthread_join(send,NULL);
	receive = 0;
	send = 0;
}
