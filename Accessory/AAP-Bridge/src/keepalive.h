#ifndef KEEPALIVE_H_
#define KEEPALIVE_H_

#include "server.h"

void* KeepaliveInit(void);
void KeepaliveOnBytesReceived(void* service_data, BridgeService* service, const void* buffer, int size);
void KeepaliveOnEof(void* service_data, BridgeService* service);
void KeepaliveClose(void* service_data, BridgeService* service);

#endif /* KEEPALIVE_H_ */
