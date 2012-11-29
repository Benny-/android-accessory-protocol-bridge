#include <stdio.h>
#include <libusb-1.0/libusb.h>
#include <pthread.h>
#include "accessory.h"
#include "config.h"
#include "usb.h"
#include "initialize.h"

libusb_device_handle* accessoryHandle;
pthread_mutex_t accessoryWrite;
pthread_mutex_t accessoryRead;

int readAccessory(unsigned char* buffer, int* transferred) {
	int response = 0;
	if(accessoryHandle == NULL) {
		return -1;
	}
	response = libusb_bulk_transfer(accessoryHandle,aoa_endpoint_in, buffer,MESSAGEMAX,transferred,0);
	return response;
}

int writeAccessory(unsigned char* buffer, int* transferred) {
	pthread_mutex_lock(&accessoryWrite);
	int response = 0;
	if(accessoryHandle == NULL) {
		return -1;
	}
	response = libusb_bulk_transfer(accessoryHandle,aoa_endpoint_out, buffer,1024, transferred,0);
	pthread_mutex_unlock(&accessoryWrite);
	return response;
}

int initAccessory(
		const char* manufacturer,
		const char* modelName,
		const char* description,
		const char* version,
		const char* uri,
		const char* serialNumber)
{
	printf("Setting up USB\n");
	initUsb();
	printf("Setting up accessory mode\n");
	accessoryHandle = findAndInitAccessory(
			manufacturer,
			modelName,
			description,
			version,
			uri,
			serialNumber);
	if(accessoryHandle == NULL){
		deInitUsb();
		return -1;
	} else {
		return 1;
	}
 }

void deInitaccessory() {
	closeUsb(accessoryHandle);
	deInitUsb();
}
