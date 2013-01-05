#include <stdio.h>
#include <libusb-1.0/libusb.h>
#include "config.h"
#include "usb.h"

#include <libudev.h>
#include <string.h>
#include <stdlib.h>
#include <locale.h>
#include <unistd.h>

static const unsigned int vendors[] = {
		0x0502, 0x0B05, 0x413C, 0x0489, 0x04C5, 0x04C5,
		0x091E, 0x18D1, 0x109B, 0x0BB4, 0x12D1, 0x24E3, 0x2116, 0x0482, 0x17EF,
		0x1004, 0x22B8, 0x0409, 0x2080, 0x0955, 0x2257, 0x10A9, 0x1D4D, 0x0471,
		0x04DA, 0x05C6, 0x1F53, 0x04E8, 0x04DD, 0x054C, 0x0FCE, 0x2340, 0x0930,
		0x19D2 };

/**
 * tries to open the usbdevice with pid and pid
 * @param vid vendor id of the usb device
 * @param pid product id of the usb device
 * @return handle of the given pid and vid
 */
libusb_device_handle* openUsb(u_int16_t vid, u_int16_t pid) {
	libusb_device_handle* handle = NULL;
	int response = -1;

	handle = libusb_open_device_with_vid_pid(NULL, vid, pid);
	if (handle == NULL) {
		return handle;
	} else {
		if (libusb_kernel_driver_active(handle, 0)) {
			response = libusb_detach_kernel_driver(handle, 0);
			if (response < 0) {
				error(response);
			}
		}
		response = libusb_claim_interface(handle, 0);
		if (response < 0) {
			error(response);
		}

		return handle;
	}
}

/**
 * Releases, Reattach kernel driver, closes handle
 * @param handle libusb handle
 */
void closeUsb(libusb_device_handle* handle) {
	int response = -1;
	if(handle != NULL) {
		response = libusb_release_interface(handle, 0);
		if (response < 0) {
			error(response);
		}
		libusb_attach_kernel_driver(handle, 0);
		libusb_close(handle);
	}
}

struct udev_device* tryGetNextUSB(struct udev_monitor *udev_monitor)
{
	struct udev_device* udev_device = NULL;

	udev_device = udev_monitor_receive_device(udev_monitor);
	if(udev_device == NULL)
	{
		fprintf(stderr,"Error in receiving next udev device (udev event)\n");
	}
	else
	{
		if(	strcmp("usb_device",udev_device_get_devtype(udev_device)) == 0 &&
			strcmp("add",udev_device_get_action(udev_device)) == 0 )
		{
			// udev_device is a new usb device
		}
		else
		{
			udev_device_unref(udev_device);
			udev_device = NULL;
		}
	}
	return udev_device;
}

int isAndroid(struct udev_device *usb_device)
{
	int isAndroid = 0;

	printf("ID_VENDOR_ID: %s\n", udev_device_get_property_value(usb_device, "ID_VENDOR_ID"));
	printf("ID_MODEL_ID: %s\n", udev_device_get_property_value(usb_device, "ID_MODEL_ID"));
	printf("ID_VENDOR: %s\n", udev_device_get_property_value(usb_device, "ID_VENDOR"));
	printf("ID_MODEL: %s\n", udev_device_get_property_value(usb_device, "ID_MODEL"));

//	if(atoi(udev_device_get_property_value(usb_device, "ID_VENDOR_ID")) == 0x0FF9)
//	{
//		for (int i = 0; i < (sizeof vendors / sizeof *vendors) && !isAndroid; i++)
//		{
//			if ( atoi(udev_device_get_property_value(usb_device, "ID_VENDOR_ID")) == vendors[i] )
//			{
//				isAndroid = 1;
//			}
//		}
//	}
//	return isAndroid;

	return strcmp(udev_device_get_property_value(usb_device, "ID_MODEL"), "Android_Phone");
}

int isAccessory(struct udev_device *usb_device)
{
	return 0;
}

void tryPutAccessoryMode(struct udev_device *usb_device)
{

}

libusb_device_handle* tryOpenAccessory(struct udev_device *usb_device)
{
	return NULL;
}

AccessoryRead readAccessoryUSB(AapConnection* con)
{
	int response = 0;
	if(con == NULL) {
		AccessoryRead accessoryRead;
		accessoryRead.error = -1;
		accessoryRead.read = 0;
		accessoryRead.buffer = NULL;
		return accessoryRead;
	}
	response = libusb_bulk_transfer(
			con->usbConnection.dev_handle,
			con->usbConnection.aoa_endpoint_in,
			con->receiveBuffer,
			con->length,
			&con->read,
			(unsigned)1000);
	AccessoryRead accessoryRead;
	accessoryRead.error = response;
	accessoryRead.read = con->read;
	accessoryRead.buffer = con->receiveBuffer;
	return accessoryRead;
}

int writeAccessoryUSB(const void* buffer, int size, AapConnection* con)
{
	pthread_mutex_lock(&con->writeLock);
	int response = 0;
	int transferred = 0;
	if(con == NULL) {
		return -1;
	}

	/*
	 * libusb_bulk_transfer() does not guarantee to write everything in one go.
	 * And thus a loop is required to ensure we write everything.
	 */
	while(size && ((response == LIBUSB_ERROR_TIMEOUT)||(response == 0)) )
	{
		response = libusb_bulk_transfer(
				con->usbConnection.dev_handle,
				con->usbConnection.aoa_endpoint_out,
				(void*)buffer,
				size,
				&transferred,
				(unsigned)1000);
		buffer += transferred;
		size -= transferred;
		if(response == LIBUSB_ERROR_TIMEOUT)
			fprintf(stderr, "A timeout occurred when writing over the usb interface");
	}
	pthread_mutex_unlock(&con->writeLock);
	return response;
}

void closeAccessoryUSB(AapConnection* con)
{
	closeUsb(con->usbConnection.dev_handle);
}
