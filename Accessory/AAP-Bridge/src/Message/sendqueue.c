#include <stdio.h>
#include <string.h>
#include <pthread.h>
#include <semaphore.h>
#include <inttypes.h>
#include "sendqueue.h"

#define MESSAGEQUEMAX 64

static MultiplexedMessage* sendQueue[MESSAGEQUEMAX];

static pthread_mutex_t queueSendMutex;
static sem_t inSendQueue;
static int currentsendposition;
static int itemsinsendqueue;

void initSendQueue(){
	itemsinsendqueue = 0;
	currentsendposition = 0;
	sem_init(&inSendQueue, 0, 0);
	pthread_mutex_init(&queueSendMutex, NULL);
}

void deInitSendQueue(){
	sem_destroy(&inSendQueue);
	pthread_mutex_destroy(&queueSendMutex);
}

void addSendQueue(MultiplexedMessage* message) {
	//lock the message queue
	pthread_mutex_lock(&queueSendMutex);

	if(currentsendposition > MESSAGEQUEMAX) {
		sendQueue[currentsendposition - MESSAGEQUEMAX] = message;
	} else {
		sendQueue[currentsendposition] = message;
	}

	itemsinsendqueue++;
	currentsendposition++;
	sem_post(&inSendQueue);
	pthread_mutex_unlock(&queueSendMutex);
}

void addBulkSendQueue(MultiplexedMessage* message[], int count) {
	pthread_mutex_lock(&queueSendMutex);
	int i=0;
	for(i=0;i<count;i++) {
		addSendQueue(message[i]);
	}
	pthread_mutex_unlock(&queueSendMutex);
}

MultiplexedMessage* pollSendQueue() {
	sem_wait(&inSendQueue);
	pthread_mutex_lock(&queueSendMutex);
	int pollpos = 0;
	if(itemsinsendqueue == 1) {
		pollpos = --currentsendposition;
		currentsendposition++;
	} else {
		pollpos = currentsendposition - itemsinsendqueue; //location to poll
	}

	if(pollpos < 0) {
		pollpos = MESSAGEQUEMAX - pollpos; //it its smaller then zero start at the end
	}

	itemsinsendqueue--;
	pthread_mutex_unlock(&queueSendMutex);
	return sendQueue[pollpos];
}
