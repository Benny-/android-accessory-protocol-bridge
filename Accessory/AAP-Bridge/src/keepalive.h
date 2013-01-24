#ifndef KEEPALIVE_H_
#define KEEPALIVE_H_

#include "server.h"

void KeepaliveOnBytesReceived(void* service_data, BridgeService* service, void* buffer, int size);
void KeepaliveOnEof(void* service_data, BridgeService* service);
void KeepaliveClose(void* service_data, BridgeService* service);

#endif /* KEEPALIVE_H_ */
