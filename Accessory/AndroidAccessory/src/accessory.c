#include <stdlib.h>
#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>

#include "accessory.h"
#include "usb.h"

int writeAccessory(const void* buffer, int size, AapConnection* con)
{
	return con->writeAccessory(buffer, size, con);
}

AccessoryRead readAccessory(AapConnection* con)
{
	return con->readAccessory(con);
}

Accessory* initAccessory(
		const char* manufacturer,	// usb accessory protocol
		const char* modelName,		// usb accessory protocol
		const char* name,			// Used for bt
		const char* description,	// Used for bt & usb accessory protocol
		const char* version,		// usb accessory protocol
		const char* uri,			// usb accessory protocol
		const char* serialNumber)	// usb accessory protocol
{
 	libusb_context *usb_context;
 	if (libusb_init(&usb_context))
 	{
 		fprintf(stderr,"libusb_init failed\n");
 		exit(EXIT_FAILURE);
 	}

#ifdef DEBUG
 	libusb_set_debug(usb_context,3);
#else
 	libusb_set_debug(usb_context,2);
#endif

 	struct udev *udev = udev_new();
	if (!udev) {
		fprintf(stderr,"Can't create udev\n");
		exit(EXIT_FAILURE);
	}

	struct udev_monitor *udev_monitor = udev_monitor_new_from_netlink(udev,"udev");
	udev_monitor_enable_receiving(udev_monitor);

	Accessory* accessory = malloc(sizeof(Accessory));
	accessory->manufacturer = manufacturer;
	accessory->modelName = modelName;
	accessory->name = name;
	accessory->description = description;
	accessory->version = version;
	accessory->uri = uri;
	accessory->serialNumber = serialNumber;
	accessory->usb_context = usb_context;
	accessory->udev_context = udev;
	accessory->udev_monitor = udev_monitor;

	uint32_t svc_uuid_int[] = { 0x01110000, 0x00100000, 0x80000080, 0xFB349B5F };
	accessory->bt_service = bt_listen(name, description, NULL, svc_uuid_int );

	accessory->fds[0].fd = udev_monitor_get_fd(accessory->udev_monitor);
	accessory->fds[0].events = POLLIN;
	accessory->fds[0].revents = 0;

	if(accessory->bt_service != NULL)
	{
		accessory->fds[1].fd = bt_getFD(accessory->bt_service);
		accessory->fds[1].events = POLLIN;
		accessory->fds[1].revents = 0;
	}
	else
	{
		accessory->fds[1].fd = -1;
		accessory->fds[1].revents = 0;
	}

	return accessory;
}

static AapConnection* mallocAapConnection()
{
	AapConnection* aapconnection = malloc(sizeof(AapConnection));
	pthread_mutex_init(&aapconnection->writeLock, NULL);
	return aapconnection;
}

AapConnection* getNextAndroidConnection(Accessory* accessory)
{
	struct udev_device *usb_device;
	AapConnection* aapconnection = NULL;

	while(aapconnection == NULL)
	{
		int fds = poll(accessory->fds, accessory->bt_service == NULL ? 1 : 2, -1);
		printf("LOG: There are %i descriptor(s) ready\n",fds);

		if(accessory->fds[0].revents)
		{
			printf("LOG: UDEV descriptor ready\n");
			accessory->fds[0].revents = 0;

			usb_device = tryGetNextUSB(accessory->udev_monitor);
			if (usb_device == NULL)
			{
				printf("LOG: New UDEV device is not a usb device\n");
				continue;
			}
			else if(isAndroid(usb_device))
			{
				tryPutAccessoryMode(usb_device);
				udev_device_unref(usb_device);
			}
			else if(isAccessory(usb_device))
			{
				libusb_device_handle* dev_handle = tryOpenAccessory(usb_device);
				if(dev_handle != NULL)
				{
					aapconnection = mallocAapConnection();
					aapconnection->usbConnection.dev_handle = dev_handle;
					// TODO: Populate aapconnection
					aapconnection->writeAccessory = &writeAccessoryUSB;
					aapconnection->readAccessory = &readAccessoryUSB;
					aapconnection->closeAccessory = &closeAccessoryUSB;
				}
			}
			else
			{
				// The newly attached device is not android nor accessory.
				// So we ignore it.
				udev_device_unref(usb_device);
			}
		}

		if(accessory->fds[1].revents)
		{
			printf("LOG: BT server descriptor ready\n");
			accessory->fds[1].revents = 0;

			int fd = accept(bt_getFD(accessory->bt_service),NULL,NULL);
			aapconnection = mallocAapConnection();
			aapconnection->btConnection.fd = fd;
			aapconnection->receiveBuffer = malloc(1024); // Arbitrary buffer size.
			aapconnection->length = 1024;
			aapconnection->writeAccessory = &writeAccessoryBT;
			aapconnection->readAccessory = &readAccessoryBT;
			aapconnection->closeAccessory = &closeAccessoryBT;
		}
	}

	return aapconnection;
}

void closeAndroidConnection(AapConnection* con)
{
	pthread_mutex_destroy(&con->writeLock);
	con->closeAccessory(con);
	free(con);
}

void deInitaccessory(Accessory* accessory)
{
	udev_monitor_unref(accessory->udev_monitor);
	udev_unref(accessory->udev_context);
	libusb_exit(accessory->usb_context);
	bt_close(accessory->bt_service);
	free(accessory);
}
