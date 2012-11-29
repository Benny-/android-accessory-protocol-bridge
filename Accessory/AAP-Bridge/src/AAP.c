#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <libusb-1.0/libusb.h>
#include <dbus/dbus.h>
#include <signal.h>

#include "server.h"
#include "Dbus/dbuslib.h"
#include "Dbus/method.h"
#include "Dbus/listener.h"
#include "Message/AccessoryMessage.h"
#include "Message/handlemessage.h"
#include "Message/receivequeue.h"
#include "Message/sendqueue.h"
#include "accessory.h"
#include "usb.h"

void stop(int sig);

/**
 * Signalhandler
 * @param sig signal by the signalhandler
 */
void stop(int sig) {
	printf("received signal: %i \n", sig);

	int transferred=0;
	int response;
	unsigned char deathmessage[] = "Im braindeath.";

	MESSAGE* message = createmessage(1, 1, 1, sizeof(deathmessage), deathmessage,
						OTHER);
	response = writeAccessory((unsigned char*)message, &transferred);
	if(response)
	{
		error(response);
		printf("Could not send braindeath message to android, but thats okay. *DIES*\n");
	}

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

	signal(SIGHUP, &stop);
	signal(SIGTERM, &stop);
	signal(SIGABRT, &stop);
	signal(SIGSEGV, &stop);
	signal(SIGINT, &stop);

	if(--argc) // Connect to sysBus if we have a few arguments.
	{
		puts("Connecting to DBUS_BUS_SYSTEM\n");
		initDbus(DBUS_BUS_SYSTEM);
		initSignalWatcher(DBUS_BUS_SYSTEM);
	}
	else
	{
		puts("Connecting to DBUS_BUS_SESSION\n");
		initDbus(DBUS_BUS_SESSION);
		initSignalWatcher(DBUS_BUS_SESSION);
	}

	while(1)
	{
		printf("Waiting for next USB connection\n");
		waitForConnectedUSBDevice();

		if(initAccessory(
				"ICT",
				"AAP",
				"AAP Bridge Prototype",
				"1.0",
				"http://www.ict.nl","2254711") < 0) {
			printf("initAccessory failed\n");
			return -1;
		} else {
			initServer();
		}

		MESSAGE *message = NULL;
		do {
			pollReceiveQueue(&message);

			printf("Message received!\n");
			if(message == NULL)
			{
				printf("Tearing down android accessory connection\n");
				addSendQueue(NULL);
			} else if(message->type == KEEPALIVE)
			{
				printf("Received a KEEPALIVE request\n");
				char reply[] = "Pong";
				encodemessage((uint8_t*)reply, sizeof(reply), KEEPALIVE);
			} else if(message->type == DBUS) {
				callmethod(message);
			} else if (message->type == SIGNAL) {
				printf("Received a SIGNAL message\n");
				char* busname = message->data + 1;
				char* objectpath = busname + strlen(busname) + 1;
				char* interfacename = objectpath + strlen(objectpath) + 1;
				char* signalname = interfacename + strlen(interfacename) + 1;

				if(*message->data) // The first byte indicates if we must add or remove a signal.
					addSignalWatch(busname,objectpath,interfacename,signalname);
				else
					removeSignalWatch(busname,objectpath,interfacename,signalname);
			} else {
				printf("can\'t handle received message type\n");
			}

			if(message != NULL)
				free(message);
		} while(message != NULL);


		deInitServer();
		printf("Read and write threads have stopped\n");
		deInitaccessory();
	}

	return 0;
}
