#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <libusb-1.0/libusb.h>
#include <dbus/dbus.h>
#include <signal.h>
#include <libconfig.h>
#include <accessory.h>
#include <config.h>

#include "flags.h"
#include "bridge.h"
#include "Dbus/dbuslib.h"
#include "Dbus/method.h"
#include "Dbus/listener.h"
#include "Message/AccessoryMessage.h"
#include "Message/receivequeue.h"
#include "Message/sendqueue.h"
#include "accessoryAudio.h"

static Accessory* accessory;
static BridgeConnection* bridge;

/**
 * Return 1 if config file is read. 0 if config file could not be read.
 */
static int readConfig(config_t* config)
{
	char config_file[100];

	sprintf(config_file,"./%s.config",PACKAGE_NAME);
	if(config_read_file(config, config_file) == CONFIG_FALSE)
	{
        fprintf(stderr, "Could not read config file %s:%d - %s\n", config_file,
            config_error_line(config), config_error_text(config));
		sprintf(config_file,"/etc/%s/%s.config",PACKAGE_NAME, PACKAGE_NAME);
		if(config_read_file(config, config_file) == CONFIG_FALSE)
		{
        fprintf(stderr, "Could not read config file %s:%d - %s\n", config_file,
            config_error_line(config), config_error_text(config));
			return 0;
		}
		else
		{
			printf("Using config file %s\n", config_file);
		}
	}
	else
	{
		printf("Using config file %s\n", config_file);
	}

	return 1;
}

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
	config_t config;

	signal(SIGHUP,  &stop);
	signal(SIGTERM, &stop);
	signal(SIGABRT, &stop);
	signal(SIGSEGV, &stop);
	signal(SIGINT,  &stop);

	config_init(&config);
	config_set_auto_convert(&config,1);
	if(!readConfig(&config))
	{
		// We could exit if no valid config is found or go into "demo" mode. In demo mode, some default values are assumed.
		printf("No valid config file found. Resuming in demo mode\n");
	}

	const char* BUS = NULL;
	config_lookup_string(&config, "BUS", &BUS);

	if(BUS != NULL && strcmp(BUS,"DBUS_BUS_SYSTEM") == 0)
	{
		puts("Connecting to DBUS_BUS_SYSTEM\n");
		FLAGS_bustype = DBUS_BUS_SYSTEM;
	}
	else
	{
		puts("Connecting to DBUS_BUS_SESSION\n");
		FLAGS_bustype = DBUS_BUS_SESSION;
	}

	const char* uuids[100];
	config_setting_t* uuids_setting = config_lookup(&config, "UUIDS");

	if(uuids_setting != NULL)
	{
		for(int i = 0; i<100; i++)
		{
			// config_setting_get_string_elem() returns NULL if we get out of bounds.
			uuids[i] = config_setting_get_string_elem(uuids_setting,i);
			if(uuids[i] == NULL)
			{
				/**
				 * All AAP-Bridge server's have this UUID. It represents a generic remote d-bus service where there
				 * is no guarantee a specific payload is running.
				 */
				uuids[i] = "a48e5d50-188b-4fca-b261-89c13914e118";
				uuids[i+1] = NULL;
				break;
			}
		}
	}
	else
	{
		uuids[0] = "a48e5d50-188b-4fca-b261-89c13914e118";
		uuids[1] = NULL;
	}

	const char* manufacturer = "ICT";
	config_lookup_string(&config,  "manufacturer",	&manufacturer);

	const char* modelName = "AAP";
	config_lookup_string(&config,  "modelName",		&modelName);

	const char* description = "AAP Bridge Prototype";
	config_lookup_string(&config, "description",	&description);

	const char* version = "1.0";
	config_lookup_string(&config,  "version",		&version);

	const char* uri = "http://www.ict.nl";
	config_lookup_string(&config,  "uri",			&uri);

	const char* serialNumber = "0";
	config_lookup_string(&config,  "modelName",		&serialNumber);

	static int accessoryStarted = 0;
	if(!accessoryStarted)
	{
		accessoryStarted = 1;
		accessory_audio_start();
	}

	accessory = initAccessory(
			manufacturer,
			modelName,
			description,
			uuids,
			version,
			uri,
			serialNumber);

	if(accessory == NULL)
	{
		fprintf(stderr, "Something went wrong in libAndroidAccessory. Please try again later\n");
		exit(EXIT_FAILURE);
	}

	while(1)
	{
		printf("Waiting for (next) connection\n");
		AapConnection* con = getNextAndroidConnection(accessory);
		printf("Connection established. setting up bridge\n");
		bridge = initServer(con);
		printf("Bridge is setup. Going into messaging loop\n");

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
	config_destroy(&config);

	return EXIT_SUCCESS;
}
