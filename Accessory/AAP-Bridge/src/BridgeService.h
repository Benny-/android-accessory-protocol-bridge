#ifndef BRIDGESERVICE_H_
#define BRIDGESERVICE_H_

#include "server.h"

struct BridgeService
{
	/**
	 * The port this service is located on. It is negative if the service is down.
	 *
	 * This value MUST correspond to the index in BridgeConnection.ports[]
	 */
	short port;
	BridgeConnection* bridge;
	void* service_data;

	/**
	 * Called when the service receives some bytes from the android application.
	 */
	void (*onBytesReceived)	(void* service_data, BridgeService* service, void* buffer, int size);

	/**
	 * Called when the service receives a eof from the android application. The onCloseService() function will not longer be called.
	 */
	void (*onEof)			(void* service_data, BridgeService* service);

	/**
	 * Called when the service should cleanup all service data. Service should not use the write function once this is called.
	 */
	void (*onCloseService)	(void* service_data, BridgeService* service);
};

#endif /* BRIDGESERVICE_H_ */
