#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <libusb-1.0/libusb.h>
#include <dbus/dbus.h>
#include <signal.h>

#include "bridge.h"
#include "Dbus/dbuslib.h"
#include "Dbus/method.h"
#include "Dbus/listener.h"
#include "Message/AccessoryMessage.h"
#include "Message/receivequeue.h"
#include "Message/sendqueue.h"
#include <accessory.h>

Accessory* accessory;
BridgeConnection* bridge;

/**
 * Signalhandler
 * @param sig signal by the signalhandler
 */
void stop(int sig) {
	int error;
	unsigned char deathmessage[] = "Im braindeath.";

	printf("received signal: %i \n", sig);

	/*
	MultiplexedMessage* message = createmessage(1, 1, 1, sizeof(deathmessage), deathmessage,
						OTHER);
	error = writeAccessory(getCurrentConnection(), message, sizeof(MESSAGE) );
	if(error)
	{
		printf("Could not send braindeath message to android, but thats okay. *DIES*\n");
	}
	*/
    exit(sig);
}

/**
 * The main loop of the application
 * @param argc number arguments
 * @param argv array of arguments
 * @return shutdown signal
 */
int main (int argc, char *argv[])
{
	printf("Program started\n");

	signal(SIGHUP,  &stop);
	signal(SIGTERM, &stop);
	signal(SIGABRT, &stop);
	signal(SIGSEGV, &stop);
	signal(SIGINT,  &stop);

	DBusBusType bus;
	if(--argc) // Connect to sysBus if we have no arguments.
	{
		puts("Connecting to DBUS_BUS_SYSTEM\n");
		bus = DBUS_BUS_SYSTEM;
	}
	else
	{
		puts("Connecting to DBUS_BUS_SESSION\n");
		bus = DBUS_BUS_SESSION;
	}

	const char* const uuids[] = {
			"",
			"",
			NULL
	};

	accessory = initAccessory(
					"ICT",
					"AAP",
					"AAP Bridge Prototype",
					uuids,
					"1.0",
					"http://www.ict.nl",
					"2254711");

	while(1)
	{
		printf("Waiting for next connection\n");
		AapConnection* con = getNextAndroidConnection(accessory);
		bridge = initServer(con);

		MultiplexedMessage *msg;
		do {
			msg = pollReceiveQueue();
			if(msg != NULL)
			{
				sendToCorrectService(bridge, msg->port, msg->data, msg->size);
				free(msg->data);
				free(msg);
			}
		} while(msg != NULL);

		deInitServer(bridge);
		printf("Read and write threads have stopped\n");
	}
	deInitaccessory(accessory);

	return 0;
}
