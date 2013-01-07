#include "AccessoryMessage.h"

#ifndef RECEIVEMESSAGEQUEUE_H
#define RECEIVEMESSAGEQUEUE_H

void initreceiveQueue();
void deInitreceiveQueue();
void addreceivequeue(MESSAGE *buffer);
void pollReceiveQueue(MESSAGE **tmp);

#endif
