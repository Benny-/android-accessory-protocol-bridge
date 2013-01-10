#include "AccessoryMessage.h"

#ifndef SENDMESSAGEQUEUE_H
#define SENDMESSAGEQUEUE_H

void initSendQueue();
void deInitSendQueue();
void addSendQueue(MESSAGE* message);
void addBulkSendQueue(MESSAGE* message[], int count);
MESSAGE* pollSendQueue();

#endif // MESSAGEQUEUE_H
