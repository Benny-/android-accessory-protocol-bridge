
#include "keepalive.h"

#include <stdio.h>

const static char* const pong = "pong";

void* KeepaliveInit(void)
{
	return NULL;
}

#ifdef DEBUG
#define ONLY_SEND_TEN_KEEPALIVES
#error "Shit man, I errored out."
#endif

#ifdef ONLY_SEND_TEN_KEEPALIVES
static int counter = 0;
#endif

void KeepaliveOnBytesReceived(void* service_data, BridgeService* service, const void* buffer, int size)
{
	printf("Keepalive received: %.*s\n", size, (const char*) buffer);
#ifdef ONLY_SEND_TEN_KEEPALIVES
	if(counter < 10)
		writeAllPort(service, pong, 4);
#elif
	writeAllPort(service, pong, 4);
#endif
}

void KeepaliveOnEof(void* service_data, BridgeService* service)
{

}

void KeepaliveClose(void* service_data, BridgeService* service)
{

}
