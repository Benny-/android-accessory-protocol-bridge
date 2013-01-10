#include <stdio.h>
#include <string.h>
#include <stdint.h>
#include <pthread.h>
#include <semaphore.h>
#include "receivequeue.h"
#include "../Message/AccessoryMessage.h"

#define MESSAGEQUEMAX 64

static MESSAGE* receiveequeue[MESSAGEQUEMAX];

static pthread_mutex_t queueReceiveMutex;
static int currentposistion;
static sem_t inReceiveQueue;

void initreceiveQueue() {
	currentposistion = 0;
	sem_init(&inReceiveQueue, 0, 0);
	pthread_mutex_init(&queueReceiveMutex, NULL);
}

void deInitreceiveQueue() {
	sem_destroy(&inReceiveQueue);
	pthread_mutex_destroy(&queueReceiveMutex);
}

void addreceivequeue(MESSAGE *buffer) {
	//lock the message queue
	pthread_mutex_lock(&queueReceiveMutex);
	currentposistion++;
	if (currentposistion > MESSAGEQUEMAX) {
		receiveequeue[currentposistion - MESSAGEQUEMAX] = buffer;
	} else {
		receiveequeue[currentposistion] = buffer;
	}
	sem_post(&inReceiveQueue);
	pthread_mutex_unlock(&queueReceiveMutex);
}

void pollReceiveQueue(MESSAGE **tmp) {
	sem_wait(&inReceiveQueue);
	pthread_mutex_lock(&queueReceiveMutex);
	int itemsinrecievequeue = 0;
	int pollpos = 0;

	sem_getvalue(&inReceiveQueue, &itemsinrecievequeue);
	pollpos = currentposistion - itemsinrecievequeue; //location to pull

	if (pollpos < 0) {
		pollpos = MESSAGEQUEMAX - pollpos; //its smaller then zero start at the end
	}

	pthread_mutex_unlock(&queueReceiveMutex);
	*tmp = receiveequeue[pollpos];

	//empty the location
	receiveequeue[pollpos] = NULL;
}
