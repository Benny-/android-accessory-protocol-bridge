#include <stdlib.h>
#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>

#include "initialize.h"
#include "accessory.h"
#include "usb.h"
#include "config.h"

int readAccessory(AapConnection* con, void* buffer, int size_max)
{
	return con->readAccessory(con, buffer, size_max);
}

int readAllAccessory(AapConnection* con, void* buffer, int size)
{
	int response = 0;
	while(size > 1 && response >= 0)
	{
		response = readAccessory(con, buffer, size);
		size -= response;
	}
	return response < 0 ? response : 0;
}

int writeAccessory(AapConnection* con, const void* buffer, int size_max)
{
	return con->writeAccessory(con, buffer, size_max);
}

int writeAllAccessory(AapConnection* con, const void* buffer, int size)
{
	int response = 0;
	while(size > 1 && response >= 0)
	{
		response = writeAccessory(con, buffer, size);
		size -= response;
	}
	return response < 0 ? response : 0;
}

Accessory* initAccessory(
		const char* manufacturer,
		const char* modelName,
		const char* description,
		const char* const* bt_uuids,
		const char* version,
		const char* uri,
		const char* serialNumber)
{
 	libusb_context *usb_context;
 	if (libusb_init(&usb_context))
 	{
 		fprintf(stderr,"libAndroidAccessory: Failed to init libusb-1.0\n");
 		return NULL;
 	}

#ifdef DEBUG
 	libusb_set_debug(usb_context,3);
#else
 	libusb_set_debug(usb_context,2);
#endif

 	struct udev *udev = udev_new();
	if (!udev) {
		fprintf(stderr,"libAndroidAccessory: Failed to init udev\n");
		libusb_exit(usb_context);
		return NULL;
	}

	struct udev_monitor *udev_monitor = udev_monitor_new_from_netlink(udev,"udev");
	udev_monitor_filter_add_match_subsystem_devtype(udev_monitor, "usb", "usb_device");
	udev_monitor_enable_receiving(udev_monitor);

	Accessory* accessory = malloc(sizeof(Accessory));
	accessory->manufacturer = manufacturer;
	accessory->modelName = modelName;
	accessory->description = description;
	accessory->bt_uuids = bt_uuids;
	accessory->version = version;
	accessory->uri = uri;
	accessory->serialNumber = serialNumber;
	accessory->usb_context = usb_context;
	accessory->udev_context = udev;
	accessory->udev_monitor = udev_monitor;

	accessory->bt_service = bt_listen(modelName, description, NULL, accessory->bt_uuids );

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
		fprintf(stderr, "libAndroidAccessory: Not listening on bluetooth. Do you have a bt device and have sufficient permissions (added to bluetooth group) ?\n");
		accessory->fds[1].fd = -1;
		accessory->fds[1].revents = 0;
	}

	return accessory;
}

static AapConnection* mallocAapConnection()
{
	AapConnection* aapconnection = malloc(sizeof(AapConnection));
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
#ifdef DEBUG
			printf("libAndroidAccessory: UDEV descriptor ready\n");
#endif
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
					aapconnection->physicalConnection.usbConnection.receiveBuffer = malloc(16384);
					aapconnection->physicalConnection.usbConnection.length = 16384;
					aapconnection->physicalConnection.usbConnection.startValidData = aapconnection->physicalConnection.usbConnection.receiveBuffer;
					aapconnection->physicalConnection.usbConnection.read = 0;
					aapconnection->writeAccessory = &writeAccessoryUSB;
					aapconnection->readAccessory = &readAccessoryUSB;
					aapconnection->closeAccessory = &closeAccessoryUSB;
				}
				udev_device_unref(usb_device);
			}
		}

		if(accessory->fds[1].revents)
		{
#ifdef DEBUG
			printf("libAndroidAccessory: BT server descriptor ready\n");
#endif
			accessory->fds[1].revents = 0;

			int fd = accept(bt_getFD(accessory->bt_service),NULL,NULL);
			aapconnection = mallocAapConnection();
			aapconnection->physicalConnection.btConnection.fd = fd;
			aapconnection->writeAccessory = &writeAccessoryBT;
			aapconnection->readAccessory = &readAccessoryBT;
			aapconnection->closeAccessory = &closeAccessoryBT;
		}
	}

	return aapconnection;
}

void closeAndroidConnection(AapConnection* con)
{
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
