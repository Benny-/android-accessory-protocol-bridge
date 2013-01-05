#include <stdio.h>
#include <string.h>
#include <pthread.h>
#include <semaphore.h>
#include <inttypes.h>
#include "sendqueue.h"

#define MESSAGEQUEMAX 64

//the actual queue
MESSAGE* sendQueue[MESSAGEQUEMAX];
pthread_mutex_t queueSendMutex;
sem_t inSendQueue;
int currentsendposition = 0;
int itemsinsendqueue = 0;

int initSendQueue(){
	return sem_init(&inSendQueue, 0, 0) | pthread_mutex_init(&queueSendMutex, NULL);
}

void addSendQueue(MESSAGE* message) {
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

void addBulkSendQueue(MESSAGE* message[], int count) {
	pthread_mutex_lock(&queueSendMutex);
	int i=0;
	for(i=0;i<count;i++) {
		addSendQueue(message[i]);
	}
	pthread_mutex_unlock(&queueSendMutex);
}

MESSAGE* pollSendQueue() {
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
