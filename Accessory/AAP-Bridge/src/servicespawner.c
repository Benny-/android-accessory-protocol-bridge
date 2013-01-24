
#include <stdlib.h>

#include "Dbus/method.h"
#include "Dbus/listener.h"
#include "servicespawner.h"

#define STREAM_OPEN   0x01 //Port id field is the port associated with the new service
#define STREAM_DENIED 0x02 //Port id field must be ignored
#define STREAM_EOF    0x03 //No data will be send from this device to specified stream
#define STREAM_CLOSE  0x04 //This device will no longer read from specified stream

typedef struct SpawnerServiceData
{
	BridgeConnection* bridge;
	BridgeService* service;
} SpawnerServiceData;

void* ServiceSpawnerInit(BridgeConnection* bridge, BridgeService* service)
{
	SpawnerServiceData* servicespawner_data = malloc(sizeof(SpawnerServiceData));
	servicespawner_data->bridge = bridge;
	servicespawner_data->service = service;
	return servicespawner_data;
}

void ServiceSpawnerOnBytesReceived(void* service_data, BridgeService* service, const void* buffer, int size)
{
	char response[8]; // This variable will be a "Port status message", see protocol specs.

	SpawnerServiceData* servicespawner_data = service_data;
	const int8_t* requested_protocool = buffer;
	switch(*requested_protocool)
	{
		case 1:
			//BridgeService* new_service = openNewPort(servicespawner_data.bridge);
			//break;

		case 2:
			//BridgeService* new_service = openNewPort(servicespawner_data.bridge);
			//break;

		default:
			response[0] = 's';
			response[3] = STREAM_DENIED;
			writeAllPort(servicespawner_data->service, response, sizeof(response));
			break;
	}
}

void ServiceSpawnerOnEof(void* service_data, BridgeService* service)
{

}

void ServiceSpawnerClose(void* service_data, BridgeService* service)
{
	free(service_data);
}
