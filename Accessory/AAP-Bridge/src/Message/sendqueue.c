#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <pthread.h>
#include <semaphore.h>
#include "sendqueue.h"

#define MESSAGEQUEMAX 64

static MultiplexedMessage* queue[MESSAGEQUEMAX];
static int messages; // The amount of messages in the above queue
static int writePosition;
static int readPosition;
static pthread_mutex_t lock;
static sem_t sem;

void initSendQueue(void)
{
	messages = 0;
	writePosition = 0;
	readPosition = 0;
	sem_init(&sem, 0, 0);
	pthread_mutex_init(&lock, NULL);
}

void deInitSendQueue(void)
{
	// Here we remove any stray items in the queue.
	pthread_mutex_lock(&lock);
	while(messages)
	{
		MultiplexedMessage* msg;
		if (readPosition >= MESSAGEQUEMAX) {
			readPosition -= MESSAGEQUEMAX;
		}
		msg = queue[readPosition];
		readPosition++;
		messages--;
		if(msg) // Okay. listen. I dont know why a NULL is in this queue.
		{
			free(msg->data);
			free(msg);
		}
	}
	pthread_mutex_unlock(&lock);


	sem_destroy(&sem);
	pthread_mutex_destroy(&lock);
}

void addSendQueue(MultiplexedMessage *message)
{
	pthread_mutex_lock(&lock);
	if (writePosition >= MESSAGEQUEMAX) {
		writePosition -= MESSAGEQUEMAX;
	}

	if(messages == MESSAGEQUEMAX)
	{
		// FIXME: This code will silently drop a message if the queue is full
		fprintf(stderr, "A message to Android designated for port %i was dropped\n", message->port);
		free(message->data);
		free(message);
	}
	else
	{
		queue[writePosition] = message;
		writePosition++;
		messages++;
		sem_post(&sem);
	}
	pthread_mutex_unlock(&lock);
}

MultiplexedMessage* pollSendQueue(void)
{
	MultiplexedMessage* retval;
	sem_wait(&sem);
	pthread_mutex_lock(&lock);
	if (readPosition >= MESSAGEQUEMAX) {
		readPosition -= MESSAGEQUEMAX;
	}
	retval = queue[readPosition];
	readPosition++;
	messages--;
	pthread_mutex_unlock(&lock);
	return retval;
}
