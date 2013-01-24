
#include <string.h>
#include <stdlib.h>
#include <stdio.h>

#include "BridgeService.h"

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

void ServiceSpawnerOnBytesReceived(void* service_data, BridgeService* service, void* buffer, int size)
{
	BridgeService* new_service;
	char response[8]; // This variable will be a "Port status message", see protocol specs.
	memset(response,0,sizeof(response));
	response[0] = 's';

	SpawnerServiceData* servicespawner_data = service_data;
	const int8_t* requested_protocol = ((int8_t*)buffer)+1;
	printf("ServiceSpawner requested_protocol: %hhi\n", *requested_protocol);
	switch(*requested_protocol)
	{
		case 1: // Bulk data
			response[3] = STREAM_DENIED;
			response[4] = 2; // The error.
			break;

		case 2: // D-Bus function calls
			new_service = openNewPort(servicespawner_data->bridge);
			new_service->service_data = MethodInit(new_service);
			if(new_service->service_data != NULL)
			{
				new_service->onBytesReceived = &MethodOnBytesReceived;
				new_service->onCloseService = &MethodClose;
				new_service->onEof = &MethodOnEof;
				response[3] = STREAM_OPEN;
				response[1] = new_service->port;
				response[2] = new_service->port >> 8;
			}
			else
			{
				response[3] = STREAM_DENIED;
				response[4] = 3; // The error.
			}
			break;

		case 3: // D-Bus signal
			response[3] = STREAM_DENIED;
			response[4] = 2; // The error.
			break;

		default:
			response[3] = STREAM_DENIED;
			response[4] = 1; // The error.
			break;
	}
	writeAllPort(servicespawner_data->service, response, sizeof(response));
}

void ServiceSpawnerOnEof(void* service_data, BridgeService* service)
{

}

void ServiceSpawnerClose(void* service_data, BridgeService* service)
{
	free(service_data);
}
