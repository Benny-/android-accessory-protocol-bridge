
#include "keepalive.h"

#include <stdio.h>

const static char* const pong = "pong";

void* KeepaliveInit(void)
{
	return NULL;
}

void KeepaliveOnBytesReceived(void* service_data, BridgeService* service, const void* buffer, int size)
{
	printf("Keepalive received: %.*s\n", size, (const char*) buffer);
	writeAllPort(service, pong, 4);
}

void KeepaliveOnEof(void* service_data, BridgeService* service)
{

}

void KeepaliveClose(void* service_data, BridgeService* service)
{

}
