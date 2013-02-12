#include "AccessoryMessage.h"

#ifndef RECEIVEMESSAGEQUEUE_H
#define RECEIVEMESSAGEQUEUE_H

void initreceiveQueue(void);
void deInitreceiveQueue(void);
void addreceivequeue(MultiplexedMessage *message);
MultiplexedMessage* pollReceiveQueue(void);

#endif
