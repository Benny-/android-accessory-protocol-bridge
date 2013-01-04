#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <pthread.h>
#include <stdint.h>
#include <math.h>
#include "receivequeue.h"
#include "sendqueue.h"
#include "handlemessage.h"
#include "AccessoryMessage.h"

void decodemessage(uint8_t* message) {
	MESSAGE* accessoryMessage = malloc(sizeof(MESSAGE));

	int* tmp = (int*) message;
	accessoryMessage->id = *(tmp++);
	accessoryMessage->numberofmessage = *(tmp++);
	accessoryMessage->totalmessages = *(tmp++);
	accessoryMessage->totaldatasize = *(tmp++);
	accessoryMessage->type = *(tmp++);
	memcpy(accessoryMessage->data,tmp,accessoryMessage->totaldatasize);

	if (accessoryMessage != NULL) {
		if (accessoryMessage->numberofmessage == 1) { //is it the first message?
			uint8_t* messagedata;
			messagedata = malloc(accessoryMessage->totaldatasize);
			memcpy(messagedata, accessoryMessage->data,
					accessoryMessage->totaldatasize);
		}

		if (accessoryMessage->numberofmessage
				== accessoryMessage->totalmessages) {
			//encodemessage(messagedata, accessoryMessage->type);
			//accessoryMessage->data = messagedata;
			addreceivequeue(accessoryMessage);
		}
	} else {
		printf("message dropped\n");
	}
}

void reverse_bytes(char* bytes, size_t len) {
	char *tmp = malloc(len);
	int i = 0;
	memcpy(tmp, bytes, len);
	for (i = 0; i < len; ++i) {
		bytes[i] = tmp[len - i - 1];
	}
	free(tmp);
}

void encodemessage(uint8_t* data, size_t data_len,  MESSAGETYPE type)
{
	if (data == NULL) {
		fprintf(stderr, "Data = NULL\n");
	} else {
//	unsigned char tmp[MESSAGEMAX];
//	int i = 0;
//	int messagecount = 1;
		int totalnumberofmessages = 0;

		totalnumberofmessages = 1; //ceil(strlen(totalsizeofdata) / MESSAGEMAX); //detect totalmessage

		if (totalnumberofmessages > 1) {
			//segmented message work in progress
		} else { //just one message
			MESSAGE* message = createmessage(1, 1, 1, data_len, data,
					type);
			addSendQueue(message);
		}
	}
}

MESSAGE* createmessage(int id, int number, int total, size_t totalsize,
		uint8_t* data, MESSAGETYPE type) {
	MESSAGE* accessoryMessage = malloc(sizeof(MESSAGE));

	accessoryMessage->numberofmessage = number;
	accessoryMessage->totalmessages = total;
	accessoryMessage->totaldatasize = totalsize;
	accessoryMessage->type = type;
	accessoryMessage->id = id;
	memcpy((void*)accessoryMessage->data, data, totalsize);

	return accessoryMessage;
}
