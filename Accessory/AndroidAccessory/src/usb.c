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
 * libusb_error_name() function originates from the libusb-1.0 source.
 *
 * Old versions of libusb-1.0 do not provide this function. This function is included
 * here so it does not break on a old version of libusb-1.0.
 */
const char *libusb_error_name(int error_code)
{
	enum libusb_error error = error_code;
	switch (error) {
	case LIBUSB_SUCCESS:
		return "LIBUSB_SUCCESS";
	case LIBUSB_ERROR_IO:
		return "LIBUSB_ERROR_IO";
	case LIBUSB_ERROR_INVALID_PARAM:
		return "LIBUSB_ERROR_INVALID_PARAM";
	case LIBUSB_ERROR_ACCESS:
		return "LIBUSB_ERROR_ACCESS";
	case LIBUSB_ERROR_NO_DEVICE:
		return "LIBUSB_ERROR_NO_DEVICE";
	case LIBUSB_ERROR_NOT_FOUND:
		return "LIBUSB_ERROR_NOT_FOUND";
	case LIBUSB_ERROR_BUSY:
		return "LIBUSB_ERROR_BUSY";
	case LIBUSB_ERROR_TIMEOUT:
		return "LIBUSB_ERROR_TIMEOUT";
	case LIBUSB_ERROR_OVERFLOW:
		return "LIBUSB_ERROR_OVERFLOW";
	case LIBUSB_ERROR_PIPE:
		return "LIBUSB_ERROR_PIPE";
	case LIBUSB_ERROR_INTERRUPTED:
		return "LIBUSB_ERROR_INTERRUPTED";
	case LIBUSB_ERROR_NO_MEM:
		return "LIBUSB_ERROR_NO_MEM";
	case LIBUSB_ERROR_NOT_SUPPORTED:
		return "LIBUSB_ERROR_NOT_SUPPORTED";
	case LIBUSB_ERROR_OTHER:
		return "LIBUSB_ERROR_OTHER";
	}
	return "**UNKNOWN**";
}

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
				fprintf(stderr, "libAndroidAccessory: %s %d -> %s\n",__FILE__, __LINE__, libusb_error_name(response));
			}
		}
		response = libusb_claim_interface(handle, 0);
		if (response < 0) {
			fprintf(stderr, "libAndroidAccessory: %s %d -> %s\n",__FILE__, __LINE__, libusb_error_name(response));
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
			fprintf(stderr, "libAndroidAccessory: %s %d -> %s\n",__FILE__, __LINE__, libusb_error_name(response));
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
                fprintf(stderr,"libAndroidAccessory: Error in receiving next udev device (udev event)\n");
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

int readAccessoryUSB(AapConnection* con, void* buffer, int size)
{
	int read;
	if(con->physicalConnection.usbConnection.read)
	{
		read = size > con->physicalConnection.usbConnection.read ? con->physicalConnection.usbConnection.read : size;
		memcpy(buffer, con->physicalConnection.usbConnection.startValidData, read);
		con->physicalConnection.usbConnection.read -= read;
		con->physicalConnection.usbConnection.startValidData += read;
	}
	else
	{
		con->physicalConnection.usbConnection.startValidData = con->physicalConnection.usbConnection.receiveBuffer;
		int error = libusb_bulk_transfer(
				con->physicalConnection.usbConnection.dev_handle,
				con->physicalConnection.usbConnection.aoa_endpoint_in,
				con->physicalConnection.usbConnection.receiveBuffer,
				con->physicalConnection.usbConnection.length,
				&con->physicalConnection.usbConnection.read,
				0u);
		if(error)
		{
			read = error;
		}
		else
		{
			read = readAccessoryUSB(con, buffer, size);
		}
	}
	return read;
}

int writeAccessoryUSB(AapConnection* con, const void* buffer, int size)
{
	int written;
	int error;

	error = libusb_bulk_transfer(
			con->physicalConnection.usbConnection.dev_handle,
			con->physicalConnection.usbConnection.aoa_endpoint_out,
			(void*)buffer,
			size,
			&written,
			0u);
	return error ? error : written;
}

void closeAccessoryUSB(AapConnection* con)
{
	closeUsb(con->physicalConnection.usbConnection.dev_handle);
	free(con->physicalConnection.usbConnection.receiveBuffer);
}
