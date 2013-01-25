#ifndef SERVER_H
#define SERVER_H

#include <accessory.h>

typedef struct BridgeConnection BridgeConnection;
typedef struct BridgeService BridgeService;

BridgeConnection* initServer(AapConnection* con);
void deInitServer(BridgeConnection* bridge);

BridgeService* openNewPort(BridgeConnection* bridge);
void sendToCorrectService(BridgeConnection* bridge, short port, void* data, short size);

/**
 * Write the buffer to the remote android application.
 */
void writeAllPort	(BridgeService* service, const void* buffer, int size);

/**
 * Send a eof to the remote android application. No more write calls may be made.
 *
 * Bytes can still be received from the Android device.
 */
void sendEof		(BridgeService* service);

/**
 * Send a close to the remote android application. No more bytes will be received from the android device.
 *
 * Bytes can still be written to the android device.
 */
void sendClose		(BridgeService* service);

#endif
