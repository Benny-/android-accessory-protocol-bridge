
#include "keepalive.h"

#include <stdio.h>

const static char* const pong = "pong";

#ifdef BUGGY_KEEPALIVE_SERVICE
#warning This is a buggy build and will suddenly stop sending keepalives
static int counter = 0;
#endif

void KeepaliveOnBytesReceived(void* service_data, BridgeService* service, void* buffer, int size)
{
	printf("Keepalive received: %.*s\n", size, (const char*) buffer);
#ifdef BUGGY_KEEPALIVE_SERVICE
	if(counter < 5)
	{
		writeAllPort(service, pong, 4);
		counter++;
	}
	else
	{
		fprintf(stderr, "Diddent send a reply to a keepalive\n");
	}
#else
	writeAllPort(service, pong, 4);
#endif
}

void KeepaliveOnEof(void* service_data, BridgeService* service)
{

}

void KeepaliveClose(void* service_data, BridgeService* service)
{

}
