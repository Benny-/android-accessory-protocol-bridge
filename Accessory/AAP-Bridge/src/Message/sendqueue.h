#include "AccessoryMessage.h"

#ifndef SENDMESSAGEQUEUE_H
#define SENDMESSAGEQUEUE_H

void initSendQueue();
void deInitSendQueue();
void addSendQueue(MultiplexedMessage* message);
void addBulkSendQueue(MultiplexedMessage* message[], int count);
MultiplexedMessage* pollSendQueue();

#endif // MESSAGEQUEUE_H
