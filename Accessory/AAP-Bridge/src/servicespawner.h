#ifndef SERVICESPAWNER_H_
#define SERVICESPAWNER_H_

#include "server.h"

void* ServiceSpawnerInit(BridgeConnection* bridge);
void ServiceSpawnerOnBytesReceived(void* service_data, BridgeService* service, void* buffer, int size);
void ServiceSpawnerOnEof(void* service_data, BridgeService* service);
void ServiceSpawnerCleanup(void* service_data, BridgeService* service);

#endif /* SERVICESPAWNER_H_ */
