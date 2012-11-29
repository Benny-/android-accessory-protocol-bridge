/*-------------------------------------------------- accessory.h */

#ifndef ACCESSORY_H
#define ACCESSORY_H
#include "usb.h"

int initAccessory(
		const char* manufacturer,
		const char* modelName,
		const char* description,
		const char* version,
		const char* uri,
		const char* serialNumber);
void deInitaccessory();
int writeAccessory(unsigned char* buffer, int* transferred);
int readAccessory(unsigned char* buffer, int* transferred);

#endif

