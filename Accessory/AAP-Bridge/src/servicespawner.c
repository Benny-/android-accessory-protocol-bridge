
#include <string.h>
#include <stdlib.h>
#include <stdio.h>

#include "BridgeService.h"

#include "Dbus/method.h"
#include "Dbus/listener.h"
#include "servicespawner.h"

#define STREAM_OPEN   0x01 //Port id field is the port associated with the new service
#define STREAM_DENIED 0x02 //Port id field must be ignored

void* ServiceSpawnerInit(BridgeConnection* bridge)
{
	return bridge;
}

void ServiceSpawnerOnBytesReceived(void* service_data, BridgeService* service, void* buffer, int size)
{
	BridgeService* new_service;
	char response[8]; // This variable will be a "Port status message", see protocol specs.
	memset(response,0,sizeof(response));
	response[0] = 's';

	BridgeConnection* bridge = service_data;
	int8_t* requested_protocol = ((int8_t*)buffer)+1;
	int16_t arguments_length = 0;
	arguments_length = (*(requested_protocol+1)) || ( (*(requested_protocol+2)) << 8);
	void* arguments = requested_protocol + 3;
	printf("ServiceSpawner requested_protocol: %hhi\n", *requested_protocol);
	switch(*requested_protocol)
	{
		case 1: // Bulk data
			response[3] = STREAM_DENIED;
			response[4] = 2; // The error.
			break;

		case 2: // D-Bus function calls
			new_service = openNewPort(bridge); // Fixme: leaking a open port if new_service->service_data is null
			new_service->service_data = MethodInit(new_service);
			if(new_service->service_data != NULL)
			{
				new_service->onBytesReceived = &MethodOnBytesReceived;
				new_service->onCleanupService = &MethodCleanup;
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
			new_service = openNewPort(bridge); // Fixme: leaking a open port if new_service->service_data is null
			new_service->service_data = SignalsInit(new_service, arguments);
			if(new_service->service_data != NULL)
			{
				new_service->onBytesReceived = &SignalsOnBytesReceived;
				new_service->onCleanupService = &SignalsCleanup;
				new_service->onEof = &SignalsOnEof;
				response[3] = STREAM_OPEN;
				response[1] = new_service->port;
				response[2] = new_service->port >> 8;
			}
			else
			{
				response[3] = STREAM_DENIED;
				response[4] = 2; // The error.
			}
			break;

		default:
			response[3] = STREAM_DENIED;
			response[4] = 1; // The error.
			break;
	}
	writeAllPort(service, response, sizeof(response));
}

void ServiceSpawnerOnEof(void* service_data, BridgeService* service)
{

}

void ServiceSpawnerCleanup(void* service_data, BridgeService* service)
{

}
