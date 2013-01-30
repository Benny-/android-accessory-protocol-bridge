#ifndef METHOD_H
#define METHOD_H

#include "../server.h"

void* MethodInit(BridgeService* service);
void  MethodOnBytesReceived(void* service_data, BridgeService* service, void* buffer, int size);
void  MethodOnEof(void* service_data, BridgeService* service);
void  MethodCleanup(void* service_data, BridgeService* service);

#endif
