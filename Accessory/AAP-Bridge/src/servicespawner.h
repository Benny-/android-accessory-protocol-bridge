#ifndef SERVICESPAWNER_H_
#define SERVICESPAWNER_H_

#include "server.h"

void* ServiceSpawnerInit(BridgeConnection* bridge, BridgeService* service);
void ServiceSpawnerOnBytesReceived(void* service_data, BridgeService* service, void* buffer, int size);
void ServiceSpawnerOnEof(void* service_data, BridgeService* service);
void ServiceSpawnerClose(void* service_data, BridgeService* service);

#endif /* SERVICESPAWNER_H_ */
