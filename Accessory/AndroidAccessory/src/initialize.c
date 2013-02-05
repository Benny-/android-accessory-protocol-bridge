#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include "config.h"
#include "usb.h"
#include "initialize.h"

const int vendors[] = { 0x0502, 0x0B05, 0x413C, 0x0489, 0x04C5, 0x04C5,
		0x091E, 0x18D1, 0x109B, 0x0BB4, 0x12D1, 0x24E3, 0x2116, 0x0482, 0x17EF,
		0x1004, 0x22B8, 0x0409, 0x2080, 0x0955, 0x2257, 0x10A9, 0x1D4D, 0x0471,
		0x04DA, 0x05C6, 0x1F53, 0x04E8, 0x04DD, 0x054C, 0x0FCE, 0x2340, 0x0930,
		0x19D2 };

void debugDescriptor(struct libusb_device_descriptor* desc)
{
	printf("Vendor  ID      : 0x%04hx\n",desc->idVendor);
	printf("Product ID      : 0x%04hx\n",desc->idProduct);
	printf("Descriptor type : 0x%02hx\n",desc->bDescriptorType);
	printf("Device class    : 0x%02hx\n",desc->bDeviceClass);
	printf("Device subclass : 0x%02hx\n",desc->bDeviceSubClass);
	printf("\n");
}

/**
 * try to find a android accessory device, and try to open it.
 *
 * @return handle of the accessory device, or null if none are found.
 */
libusb_device_handle* findAndInitAccessory(
		const char* manufacturer,
		const char* modelName,
		const char* description,
		const char* version,
		const char* uri,
		const char* serialNumber)
{
	libusb_device** list;
	printf("Enumerating devices\n");
	ssize_t cnt = libusb_get_device_list(NULL, &list);

	int i, j;
	struct libusb_device_descriptor desc;
	libusb_device_handle* handle = NULL;
	for (i = 0; i < cnt; i++) {
		libusb_device* tmpdevice = list[i];

		if (libusb_get_device_descriptor(tmpdevice, &desc) < 0) {
			continue;
		}

		debugDescriptor(&desc);

		for (j = 0; j < (sizeof vendors / sizeof *vendors); ++j)
		{
			if (desc.idVendor == vendors[j]) {
				printf("Supported vendor Id found: %04hx\n", desc.idVendor);

				// Check to see if the device is already in accessory mode
				switch(desc.idProduct)
				{
				case ACCESSORY:
				case ACCESSORY_ADB:
				case AUDIO:
				case AUDIO_ADB:
				case ACCESSORY_AUDIO:
				case ACCESSORY_AUDIO_ADB:
					printf("Connected device already in accessory mode, continuing!\n");
					int errorCode = libusb_open(tmpdevice,&handle);
					if ( errorCode != 0 )
					{
						fputs(libusb_error_name(errorCode), stderr);
					}
					determineEndpoints(tmpdevice);
					libusb_free_device_list(list, 1);
					return handle;
				default:
					break;
				}

				// We're not already in accessory mode, try to send configuration commands.
				handle = openUsb(desc.idVendor, desc.idProduct); //try to open the USB interface
				if (handle != NULL) {
					int version_supported = -1;

					version_supported = checkAndroid(handle);

					switch(version_supported) {
					case 1:
						printf("Android Open Accessory version 1 support detected\n");
						// Fallthrough to next case
						/* no break */
					case 2:
						printf("Android Open Accessory version 2 support detected\n");
						printf("Trying to setup accessory mode\n");
						handle = setupAccessory(
								manufacturer,
								modelName,
								description,
								version,
								uri,
								serialNumber,
								handle); //try to set-up Accessory
						libusb_free_device_list(list, 1);
						return handle;
						break;
					default:
						printf("Unsupported or no Android device support\n");
						closeUsb(handle);
						break;

					}
				}
			}
		}
	}
	libusb_free_device_list(list, 1);

	// Upon reaching this location we didn't find any supported android device, so we return NULL
	return NULL;
}

libusb_device_handle* reInitAccessory() {
	libusb_device** list;
	ssize_t cnt = libusb_get_device_list(NULL, &list);

	int i;
	struct libusb_device_descriptor* desc;
	desc = (struct libusb_device_descriptor*) malloc(
			sizeof(struct libusb_device_descriptor));
	libusb_device_handle* handle = NULL;

	printf("Attempting to re-open device as Accessory Device... ");

	for (i = 0; i < cnt; i++) {
		libusb_device* tmpdevice = list[i];

		printf("%p\n",tmpdevice);

		if (libusb_get_device_descriptor(tmpdevice, desc) < 0) {
			continue;
		}

		debugDescriptor(desc);

		switch (desc->idProduct)
		{
		case ACCESSORY:
		case ACCESSORY_ADB:
		case AUDIO:
		case AUDIO_ADB:
		case ACCESSORY_AUDIO:
		case ACCESSORY_AUDIO_ADB:
			printf("Found a accessory device descriptor\n");
			int errorCode = libusb_open(tmpdevice,&handle);
			if ( errorCode == 0 ) {
				// We have our handle, lets begin by determining our endpoints
				determineEndpoints(tmpdevice);
				printf("Success!\n");
			}
			else
			{
				fputs(libusb_error_name(errorCode), stderr);
			}
			libusb_free_device_list(list, 1);
			return handle;
		default:
			break;
		}
	}

	libusb_free_device_list(list, 1);
	// Upon reaching this location we didn't find any supported android device, so we return NULL
	return NULL ;
}

/**
 * Determine the endpoints of the currently active configuration. If the endpoints can be determined,
 * they are stored in the global variables aoa_endpoint_in and aoa_endpoint_out.
 * @param dev Pointer to the currently opened device
 */
void determineEndpoints(libusb_device* dev) {
	int response = -1;

	// We have our handle, lets begin by determining our endpoints
	struct libusb_config_descriptor *desc = NULL;
	response = libusb_get_config_descriptor_by_value(dev, 1, &desc);
	if (response == 0) {
		printf("%d actual interfaces in this descriptor\n",
				desc->bNumInterfaces);
		for (int i = 0; i < desc->bNumInterfaces; ++i) {
			const struct libusb_interface* iface = &desc->interface[i];
			printf("Found %d interfaces\n", iface->num_altsetting);
			for (int j = 0; j < iface->num_altsetting; ++j) {
				const struct libusb_interface_descriptor* interfacedesc =
						&iface->altsetting[j];

				if (interfacedesc->bInterfaceNumber != 0) {
					// this is not the regular interface, but the ADB interface, skip it
					printf("Skipping ADB interface... \n");
					continue;
				}

				// We enumerate the endpoints, not dealing with more than 2 endpoints here
				for (int k = 0; k < interfacedesc->bNumEndpoints; ++k) {
					const struct libusb_endpoint_descriptor* endpointsdesc =
							&interfacedesc->endpoint[k];
					uint8_t endp = endpointsdesc->bEndpointAddress;
					if (endp & 0x80) {
						// Direction is inbound
						aoa_endpoint_in = endp;
						printf("Endpoint in = %x\n", aoa_endpoint_in);
					} else {
						// Direction is outbound
						aoa_endpoint_out = endp;
						printf("Endpoint out = %x\n", aoa_endpoint_out);
					}
				}
			}
		}
		libusb_free_config_descriptor(desc);
	}
}

/*
 * Send Android accessory identification
 * (in most of cases you don't have to call this function)
 *
 * @param handle the usb handle which has to turn to accessory
 * @return handle to accessory device
 */
libusb_device_handle* setupAccessory(
		const char* MANUFACTURER,
		const char* MODELNAME,
		const char* DESCRIPTION,
		const char* VERSION,
		const char* URI,
		const char* SERIALNUMBER,
		libusb_device_handle* handle) {
	int response;

	if ((response = libusb_control_transfer(handle,
			LIBUSB_ENDPOINT_OUT | LIBUSB_REQUEST_TYPE_VENDOR, 52, 0, 0,
			(unsigned char*)MANUFACTURER, strlen(MANUFACTURER) + 1, 0)) < 0) {
		fputs(libusb_error_name(response), stderr);
		return NULL ;
	}

	if ((response = libusb_control_transfer(handle,
			LIBUSB_ENDPOINT_OUT | LIBUSB_REQUEST_TYPE_VENDOR, 52, 0, 1,
			(unsigned char*)MODELNAME, strlen(MODELNAME) + 1, 0)) < 0) {
		return NULL ;
	}

	if ((response = libusb_control_transfer(handle,
			LIBUSB_ENDPOINT_OUT | LIBUSB_REQUEST_TYPE_VENDOR, 52, 0, 2,
			(unsigned char*)DESCRIPTION, strlen(DESCRIPTION) + 1, 0)) < 0) {
		return NULL ;
	}

	if ((response = libusb_control_transfer(handle,
			LIBUSB_ENDPOINT_OUT | LIBUSB_REQUEST_TYPE_VENDOR, 52, 0, 3,
			(unsigned char*)VERSION, strlen(VERSION) + 1, 0)) < 0) {
		return NULL ;
	}

	if ((response = libusb_control_transfer(handle,
			LIBUSB_ENDPOINT_OUT | LIBUSB_REQUEST_TYPE_VENDOR, 52, 0, 4,
			(unsigned char*)URI,strlen(URI) + 1, 0)) < 0) {
		return NULL ;
	}

	if ((response = libusb_control_transfer(handle,
			LIBUSB_ENDPOINT_OUT | LIBUSB_REQUEST_TYPE_VENDOR, 52, 0, 5,
			(unsigned char*)SERIALNUMBER, strlen(SERIALNUMBER) + 1, 0)) < 0) {
		return NULL ; //try to set-up Accessory
	}

#ifdef DEBUG
	printf("Accessory Identification sent\n");
#endif

	// SET_AUDIO_MODE. XXX: We are not checking if the Android device supports audio mode.
	libusb_control_transfer(handle, LIBUSB_ENDPOINT_OUT|LIBUSB_REQUEST_TYPE_VENDOR, 58, 1, 0, NULL, 0, 0);

	// ACCESSORY_START
	if ((response = libusb_control_transfer(handle, LIBUSB_ENDPOINT_OUT|LIBUSB_REQUEST_TYPE_VENDOR, 53, 0, 0, NULL, 0, 0)) < 0) {
		return NULL;
	}

#ifdef DEBUG
	printf("Attempted to put device into accessory mode\n");
#endif

	if (handle != NULL) {
		libusb_release_interface(handle, 0);
		libusb_close(handle);
	}

	//@todo automatic detection of reconnecting phone
	sleep(5);


	handle = reInitAccessory();

	printf("Interface claimed, ready to transfer data\n");

	return handle;
}

/*
 * Has the Android device Android Accessory
 * @param USB handle
 * @return 1 if it has android accessory device
 */
int checkAndroid(libusb_device_handle* handle) {
	unsigned char ioBuffer[2];
	int devVersion;
	int response = -1;

	response = libusb_control_transfer(handle, //handle
			0xC0, //bmRequestType
			51, //bRequest
			0, //wValue
			0, //wIndex
			ioBuffer, //data
			2, //wLength
			2500 //timeout
	);

	if (response < 0) {
		if (response == LIBUSB_ERROR_TIMEOUT) {
			printf("No Android Accessory support, continuing...\n");
		}
		else {
			fputs(libusb_error_name(response), stderr);
		}
		return -1;
	}

	devVersion = ioBuffer[1] << 8 | ioBuffer[0];

	return devVersion;
}



