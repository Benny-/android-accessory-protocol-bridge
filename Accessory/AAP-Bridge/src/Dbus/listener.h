#ifndef LISTENER_H
#define LISTENER_H

#include "../bridge.h"

void* SignalsInit(BridgeService* service, char* compressed_rule);
void  SignalsOnBytesReceived(void* service_data, BridgeService* service, void* buffer, int size);
void  SignalsOnEof(void* service_data, BridgeService* service);
void  SignalsCleanup(void* service_data, BridgeService* service);

#endif


