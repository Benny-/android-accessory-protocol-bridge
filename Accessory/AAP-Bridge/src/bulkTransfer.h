#ifndef BULKTRANSFER_C_
#define BULKTRANSFER_C_

#include "bridge.h"

void* BulkInit(BridgeService* service, char* arguments);
void BulkOnBytesReceived(void* service_data, BridgeService* service, void* buffer, int size);
void BulkOnEof(void* service_data, BridgeService* service);
void BulkCleanup(void* service_data, BridgeService* service);

#endif /* BULKTRANSFER_C_ */
