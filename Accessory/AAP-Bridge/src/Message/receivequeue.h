#include "../Message/AccessoryMessage.h"

#ifndef RECEIVEMESSAGEQUEUE_H
#define RECEIVEMESSAGEQUEUE_H

int initreceiveQueue();
void addreceivequeue(MESSAGE *buffer);
void pollReceiveQueue(MESSAGE **tmp);

#endif
