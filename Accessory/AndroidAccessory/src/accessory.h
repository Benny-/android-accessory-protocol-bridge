#ifndef ACCESSORY_INTERNAL_H
#define ACCESSORY_INTERNAL_H

#include <accessory.h>
#include <libusb-1.0/libusb.h>
#include <pthread.h>
#include <libudev.h>
#include <poll.h>

#include "bt.h"

struct Accessory
{
	// The following variables are used for usb:
	const char* manufacturer;
	const char* modelName;
	const char* name;
	const char* description;
	const char* version;
	const char* uri;
	const char* serialNumber;
	libusb_context* usb_context;
	struct udev *udev_context;
	struct udev_monitor *udev_monitor;

	// The next variable used for bluetooth:
	BT_SERVICE* bt_service;

	// The next variable is used for poll()
	struct pollfd fds[2];
};

typedef struct USBConnection
{
	libusb_device_handle* dev_handle;
	int aoa_endpoint_in;
	int aoa_endpoint_out;
	/**
	 * The receive buffer is internally handled to prevent a buffer overflow.
	 *
	 * See http://libusb.sourceforge.net/api-1.0/packetoverflow.html
	 */
	void* receiveBuffer;

	/**
	 * The amount of bytes in receiveBuffer containing valid data.
	 */
	unsigned int read;

	/**
	 * Points to the first valid data in receiveBuffer.
	 *
	 * Points to undefined data if the read variable is zero.
	 */
	void* startValidData;

	/**
	 * The total size of the receiveBuffer.
	 */
	int length;
} USBConnection;

typedef struct BTConnection
{
	int fd;
} BTConnection;

struct AapConnection
{
	union
	{
		BTConnection btConnection;
		USBConnection usbConnection;
	} physicalConnection;
	int (*readAccessory ) (AapConnection* con,       void* buffer, int size_max);
	int (*writeAccessory) (AapConnection* con, const void* buffer, int size_max);
	void (*closeAccessory) (AapConnection* con);
};

#endif
