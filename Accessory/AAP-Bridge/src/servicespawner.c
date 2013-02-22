
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <pthread.h>

#include "BridgeService.h"
#include "servicespawner.h"

#include "bulkTransfer.h"
#include "Dbus/method.h"
#include "Dbus/listener.h"

#define STREAM_OPEN   0x01 //Port id field is the port associated with the new service
#define STREAM_DENIED 0x02 //Port id field must be ignored

typedef struct ServiceSpawnData
{
	/**
	 * This mutex's purpose to to prevent a newly created service to send data to Android before Android
	 * knows on which port the service will be located on.
	 *
	 * This mutex will be locked by the receivedThread once a new service is starting.
	 * It will be unlocked once it has send confirmation about the new service to Android.
	 *
	 * This is only important for services who send data asynchronous. AKA, only if they start a thread.
	 * The thread should to obtain this lock and release it right away. This ensures the
	 * confirmation has send to Android about this service before data is send.
	 */
	pthread_mutex_t startingLock;
	BridgeConnection* bridge;
} ServiceSpawnData;

void* ServiceSpawnerInit(BridgeConnection* bridge)
{
	ServiceSpawnData* service_data = malloc(sizeof(ServiceSpawnData));
	pthread_mutex_init(&service_data->startingLock, NULL);
	service_data->bridge = bridge;
	return service_data;
}

void ServiceSpawnerOnBytesReceived(void* service_data, BridgeService* service, void* buffer, int size)
{
	BridgeService* new_service;
	char response[8]; // This variable will be a "Port status message", see protocol specs.
	memset(response,0,sizeof(response));
	response[0] = 's';

	ServiceSpawnData* serviceSpawnData = service_data;
	int8_t* requested_protocol = ((int8_t*)buffer)+1;
	int16_t arguments_length = 0;
	arguments_length = (*(requested_protocol+1)) | ( (*(requested_protocol+2)) << 8);
	void* arguments = requested_protocol + 3;
	printf("ServiceSpawner requested_protocol: %hhi\n", *requested_protocol);
	printf("ServiceSpawner arguments_length: %hi\n", arguments_length);
	pthread_mutex_t* startLock = &serviceSpawnData->startingLock;
	pthread_mutex_lock(startLock);
	new_service = openNewPort(serviceSpawnData->bridge); // Fixme: leaking a open port if new_service->service_data is null
	if(new_service != NULL)
	{
		switch(*requested_protocol)
		{
			case 1: // Bulk data
				new_service->service_data = BulkInit(new_service, startLock, arguments);
				if(new_service->service_data != NULL)
				{
					new_service->onBytesReceived = &BulkOnBytesReceived;
					new_service->onCleanupService = &BulkCleanup;
					new_service->onEof = &BulkOnEof;
					response[1] = new_service->port;
					response[2] = new_service->port >> 8;
					response[3] = STREAM_OPEN;
				}
				else
				{
					response[3] = STREAM_DENIED;
					response[4] = 2; // The error.
				}
				break;

			case 2: // D-Bus function calls
				new_service->service_data = MethodInit(new_service);
				if(new_service->service_data != NULL)
				{
					new_service->onBytesReceived = &MethodOnBytesReceived;
					new_service->onCleanupService = &MethodCleanup;
					new_service->onEof = &MethodOnEof;
					response[1] = new_service->port;
					response[2] = new_service->port >> 8;
					response[3] = STREAM_OPEN;
				}
				else
				{
					response[3] = STREAM_DENIED;
					response[4] = 3; // The error.
				}
				break;

			case 3: // D-Bus signal
				new_service->service_data = SignalsInit(new_service, startLock, arguments);
				if(new_service->service_data != NULL)
				{
					new_service->onBytesReceived = &SignalsOnBytesReceived;
					new_service->onCleanupService = &SignalsCleanup;
					new_service->onEof = &SignalsOnEof;
					response[1] = new_service->port;
					response[2] = new_service->port >> 8;
					response[3] = STREAM_OPEN;
				}
				else
				{
					response[3] = STREAM_DENIED;
					response[4] = 2; // The error.
				}
				break;

			default:
				// Request for a unknown service if we come here.
				response[3] = STREAM_DENIED;
				response[4] = 1; // The error.
				break;
		}
	}
	else
	{
		// All ports are already in use if we come in this block.
		response[3] = STREAM_DENIED;
		response[4] = 5; // The error.
	}
	writeAllPort(service, response, sizeof(response));
	pthread_mutex_unlock(startLock); // Done sending confirmation. This is a signal to the service, it may now start sending bytes.
}

void ServiceSpawnerOnEof(void* service_data, BridgeService* service)
{

}

void ServiceSpawnerCleanup(void* service_data, BridgeService* service)
{
	ServiceSpawnData* serviceSpawnData = service_data;
	pthread_mutex_destroy(&serviceSpawnData->startingLock);
	free(serviceSpawnData);
}
