#ifndef SERVER_H
#define SERVER_H

#include <accessory.h>

typedef struct BridgeConnection BridgeConnection;
typedef struct BridgeService BridgeService;

BridgeConnection* initServer(AapConnection* con);
void deInitServer(BridgeConnection* bridge);

void sendToCorrectService(BridgeConnection* bridge, short port, const void* data, short size);

/**
 * Write the buffer to the remote android application.
 */
void writeAllPort	(BridgeService* service, const void* buffer, int size);

/**
 * Send a eof to the remote android application. No more write calls may be made from the service.
 */
void sendEof		(BridgeService* bridge);

#endif
