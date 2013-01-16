#include "AccessoryMessage.h"

#ifndef RECEIVEMESSAGEQUEUE_H
#define RECEIVEMESSAGEQUEUE_H

void initreceiveQueue();
void deInitreceiveQueue();
void addreceivequeue(MultiplexedMessage *buffer);
void pollReceiveQueue(MultiplexedMessage **tmp);

#endif
