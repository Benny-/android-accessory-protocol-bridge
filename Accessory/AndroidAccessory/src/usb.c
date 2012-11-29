#include <stdio.h>
#include <libusb-1.0/libusb.h>
#include "config.h"
#include "usb.h"

#include <libudev.h>
#include <string.h>
#include <stdlib.h>
#include <locale.h>
#include <unistd.h>
#include <fcntl.h>

int initUsb() {
	return libusb_init(NULL);
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

/**
 * Exits libusb
 * @return void
 */
void deInitUsb() {
	libusb_exit(NULL);
}

/**
 *
 * @param code
 */
void error(int code){
	fprintf(stdout,"\n");
	switch(code){
	case LIBUSB_ERROR_IO:
		fprintf(stdout,"Error: LIBUSB_ERROR_IO\nInput/output error.\n");
		break;
	case LIBUSB_ERROR_INVALID_PARAM:
		fprintf(stdout,"Error: LIBUSB_ERROR_INVALID_PARAM\nInvalid parameter.\n");
		break;
	case LIBUSB_ERROR_ACCESS:
		fprintf(stdout,"Error: LIBUSB_ERROR_ACCESS\nAccess denied (insufficient permissions).\n");
		break;
	case LIBUSB_ERROR_NO_DEVICE:
		fprintf(stdout,"Error: LIBUSB_ERROR_NO_DEVICE\nNo such device (it may have been disconnected).\n");
		break;
	case LIBUSB_ERROR_NOT_FOUND:
		fprintf(stdout,"Error: LIBUSB_ERROR_NOT_FOUND\nEntity not found.\n");
		break;
	case LIBUSB_ERROR_BUSY:
		fprintf(stdout,"Error: LIBUSB_ERROR_BUSY\nResource busy.\n");
		break;
	case LIBUSB_ERROR_TIMEOUT:
		fprintf(stdout,"Error: LIBUSB_ERROR_TIMEOUT\nOperation timed out.\n");
		break;
	case LIBUSB_ERROR_OVERFLOW:
		fprintf(stdout,"Error: LIBUSB_ERROR_OVERFLOW\nOverflow.\n");
		break;
	case LIBUSB_ERROR_PIPE:
		fprintf(stdout,"Error: LIBUSB_ERROR_PIPE\nPipe error.\n");
		break;
	case LIBUSB_ERROR_INTERRUPTED:
		fprintf(stdout,"Error:LIBUSB_ERROR_INTERRUPTED\nSystem call interrupted (perhaps due to signal).\n");
		break;
	case LIBUSB_ERROR_NO_MEM:
		fprintf(stdout,"Error: LIBUSB_ERROR_NO_MEM\nInsufficient memory.\n");
		break;
	case LIBUSB_ERROR_NOT_SUPPORTED:
		fprintf(stdout,"Error: LIBUSB_ERROR_NOT_SUPPORTED\nOperation not supported or unimplemented on this platform.\n");
		break;
	case LIBUSB_ERROR_OTHER:
		fprintf(stdout,"Error: LIBUSB_ERROR_OTHER\nOther error.\n");
		break;
	default:
		fprintf(stdout, "Error: unkown error\n");
		break;
	}
}

// Blocks untill a new USB device connects to the system
void waitForConnectedUSBDevice()
{
	struct udev *udev;
	struct udev_monitor *udev_monitor;
	struct udev_device *udev_device;
	int new_usb_device_connected = 0;

	/* Create the udev object */
	udev = udev_new();
	if (!udev) {
		fprintf(stderr,"Can't create udev\n");
		exit(1);
	}

	udev_monitor = udev_monitor_new_from_netlink(udev,"udev");
	int fd = udev_monitor_get_fd(udev_monitor);
	int flags = fcntl(fd, F_GETFL, 0 );
	fcntl(fd, F_SETFL, (~O_NONBLOCK) & flags );
	udev_monitor_enable_receiving(udev_monitor);

	while(!new_usb_device_connected)
	{
		udev_device = udev_monitor_receive_device(udev_monitor);
		if(udev_device == NULL)
		{
			fprintf(stderr,"Error in receiving next udev device (event)\n");
		}
		else
		{
			if(	strcmp("usb_device",udev_device_get_devtype(udev_device)) == 0 &&
				strcmp("add",udev_device_get_action(udev_device)) == 0 )
			{
				new_usb_device_connected = 1;
			}
		}
		udev_device_unref(udev_device);
	}

	udev_monitor_unref(udev_monitor);
	udev_unref(udev);
}
