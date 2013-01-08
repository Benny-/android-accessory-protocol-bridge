#include <stdlib.h>
#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>

#include "initialize.h"
#include "accessory.h"
#include "usb.h"
#include "config.h"

int writeAccessory(const void* buffer, int size, AapConnection* con)
{
	return con->writeAccessory(buffer, size, con);
}

AccessoryRead readAccessory(AapConnection* con)
{
	return con->readAccessory(con);
}

Accessory* initAccessory(
		const char* manufacturer,
		const char* modelName,
		const char* description,
		const char* version,
		const char* uri,
		const char* serialNumber)
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
	accessory->description = description;
	accessory->version = version;
	accessory->uri = uri;
	accessory->serialNumber = serialNumber;
	accessory->usb_context = usb_context;
	accessory->udev_context = udev;
	accessory->udev_monitor = udev_monitor;

	uint32_t svc_uuid_int[] = { 0x01110000, 0x00100000, 0x80000080, 0xFB349B5F };
	accessory->bt_service = bt_listen(modelName, description, NULL, svc_uuid_int );

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

		if(accessory->fds[0].revents)
		{
			printf("LOG: UDEV descriptor ready\n");
			accessory->fds[0].revents = 0;

			usb_device = tryGetNextUSB(accessory->udev_monitor);
			if (usb_device == NULL)
			{
				continue;
			}
			else
			{
				libusb_device_handle* dev_handle = findAndInitAccessory(
						accessory->manufacturer,
						accessory->modelName,
						accessory->description,
						accessory->version,
						accessory->uri,
						accessory->serialNumber);
				if(dev_handle != NULL)
				{
					aapconnection = mallocAapConnection();
					aapconnection->physicalConnection.usbConnection.dev_handle = dev_handle;
					aapconnection->physicalConnection.usbConnection.aoa_endpoint_in = aoa_endpoint_in;
					aapconnection->physicalConnection.usbConnection.aoa_endpoint_out = aoa_endpoint_out;
					/**
					 * Very carefully picked buffer size.
					 *
					 * Buffer need to be a multiple of 64 and 512 to prevent packetoverflows.
					 *
					 * http://libusb.sourceforge.net/api-1.0/packetoverflow.html
					 */
					aapconnection->receiveBuffer = malloc(16384);
					aapconnection->length = 16384;
					aapconnection->writeAccessory = &writeAccessoryUSB;
					aapconnection->readAccessory = &readAccessoryUSB;
					aapconnection->closeAccessory = &closeAccessoryUSB;
				}
				udev_device_unref(usb_device);
			}
		}

		if(accessory->fds[1].revents)
		{
			printf("LOG: BT server descriptor ready\n");
			accessory->fds[1].revents = 0;

			int fd = accept(bt_getFD(accessory->bt_service),NULL,NULL);
			aapconnection = mallocAapConnection();
			aapconnection->physicalConnection.btConnection.fd = fd;
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
