#ifndef ACCESSORY_USB_H
#define ACCESSORY_USB_H

#include <libusb-1.0/libusb.h>
#include <libudev.h>
#include "accessory.h"

libusb_device_handle* openUsb(u_int16_t vid, u_int16_t pid);
libusb_device_handle* openUsbUdev(struct udev_device* usb_device);
void closeUsb(libusb_device_handle* handle);

struct udev_device* tryGetNextUSB(struct udev_monitor *udev_monitor);

AccessoryRead readAccessoryUSB(AapConnection* con);
int writeAccessoryUSB(const void* buffer, int size, AapConnection* con);
void closeAccessoryUSB(AapConnection* con);

#endif
