#include <stdio.h>
#include <libusb-1.0/libusb.h>
#include "config.h"
#include "usb.h"

#include <libudev.h>
#include <string.h>
#include <stdlib.h>
#include <locale.h>
#include <unistd.h>

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
				fputs(libusb_error_name(response), stderr);
			}
		}
		response = libusb_claim_interface(handle, 0);
		if (response < 0) {
			fputs(libusb_error_name(response), stderr);
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
			fputs(libusb_error_name(response), stderr);
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
                if(     strcmp("usb_device",udev_device_get_devtype(udev_device)) == 0 &&
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

AccessoryRead readAccessoryUSB(AapConnection* con)
{
	int error = 0;
	if(con == NULL) {
		AccessoryRead accessoryRead;
		accessoryRead.error = -1;
		accessoryRead.read = 0;
		accessoryRead.buffer = NULL;
		return accessoryRead;
	}
	error = libusb_bulk_transfer(
			con->usbConnection.dev_handle,
			con->usbConnection.aoa_endpoint_in,
			con->receiveBuffer,
			con->length,
			&con->read,
			0u);
	AccessoryRead accessoryRead;
	accessoryRead.error = error;
	accessoryRead.read = con->read;
	accessoryRead.buffer = con->receiveBuffer;
	return accessoryRead;
}

int writeAccessoryUSB(const void* buffer, int size, AapConnection* con)
{
	pthread_mutex_lock(&con->writeLock);
	int error = 0;
	int transferred = 0;
	if(con == NULL) {
		return -1;
	}

	/*
	 * libusb_bulk_transfer() does not guarantee to write everything in one go.
	 * And thus a loop is required to ensure we write everything.
	 */
	while(size && !error )
	{
		error = libusb_bulk_transfer(
				con->usbConnection.dev_handle,
				con->usbConnection.aoa_endpoint_out,
				(void*)buffer,
				size,
				&transferred,
				0u);
		buffer += transferred;
		size -= transferred;
	}
	pthread_mutex_unlock(&con->writeLock);
	return error;
}

void closeAccessoryUSB(AapConnection* con)
{
	closeUsb(con->usbConnection.dev_handle);
}
