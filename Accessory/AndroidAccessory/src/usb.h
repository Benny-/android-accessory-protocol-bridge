#ifndef ACCESSORY_USB_H
#define ACCESSORY_USB_H

#include <libusb-1.0/libusb.h>
#include <libudev.h>
#include "accessory.h"

const char *libusb_error_name(int error_code);
libusb_device_handle* openUsb(libusb_context* ctx, u_int16_t vid, u_int16_t pid);
libusb_device_handle* openUsbUdev(struct udev_device* usb_device);
void closeUsb(libusb_device_handle* handle);

struct udev_device* tryGetNextUSB(struct udev_monitor *udev_monitor);

int readAccessoryUSB   (AapConnection* con,       void* buffer, int size_max);
int writeAccessoryUSB  (AapConnection* con, const void* buffer, int size_max);
void closeAccessoryUSB (AapConnection* con);

#endif
