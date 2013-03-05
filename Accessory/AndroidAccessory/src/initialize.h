#include <libusb-1.0/libusb.h>

#ifndef INITIALIZE_H
#define INITIALIZE_H

extern const int vendors[];
libusb_device_handle*  findAndInitAccessory(
		libusb_context* ctx,
		const char* manufacturer,
		const char* modelName,
		const char* description,
		const char* version,
		const char* uri,
		const char* serialNumber);
libusb_device_handle* setupAccessory(
		const char* MANUFACTURER,
		const char* MODELNAME,
		const char* DESCRIPTION,
		const char* VERSION,
		const char* URI,
		const char* SERIALNUMBER,
		libusb_device_handle* handle);
int checkAndroid(libusb_device_handle*);
void determineEndpoints(libusb_device*);
void deInitAccessory();
#endif // INITIALIZE_H
