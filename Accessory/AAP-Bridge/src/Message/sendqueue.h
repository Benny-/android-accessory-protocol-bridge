#include "AccessoryMessage.h"

#ifndef SENDMESSAGEQUEUE_H
#define SENDMESSAGEQUEUE_H

void initSendQueue(void);
void deInitSendQueue(void);
void addSendQueue(MultiplexedMessage* message);
MultiplexedMessage* pollSendQueue();

#endif // MESSAGEQUEUE_H
