#include <libusb-1.0/libusb.h>

#ifndef USB_H
#define USB_H

int initUsb();
void deInitUsb();
libusb_device_handle* openUsb(u_int16_t vid, u_int16_t pid);
void closeUsb(libusb_device_handle* handle);
void error(int code);
void waitForConnectedUSBDevice();
#endif
