#include "AccessoryMessage.h"

#ifndef SENDMESSAGEQUEUE_H
#define SENDMESSAGEQUEUE_H

int initSendQueue();
void addSendQueue(MESSAGE* message);
void addBulkSendQueue(MESSAGE* message[], int count);
MESSAGE* pollSendQueue();

#endif // MESSAGEQUEUE_H
