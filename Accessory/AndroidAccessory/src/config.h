
#ifndef CONFIG_H
#define CONFIG_H

/*
 * The Android device first enumerates using a vendor's own VID & PID.
 *
 * Once very specific control requests are made, the device disconnects and
 * reconnects as accessory usb slave using the VID & PID's below.
 */

// Vendor ID
// The usb vendor id for a accessory is always the same. Its always google's VID.
#define GOOGLE				0x18d1

// Product ID
// The usb product id (PID) for a accessory can be one of the following:
#define ACCESSORY           0x2D00
#define ACCESSORY_ADB       0x2D01
#define AUDIO               0x2D02
#define AUDIO_ADB           0x2D03
#define ACCESSORY_AUDIO     0x2D04
#define ACCESSORY_AUDIO_ADB 0x2D05

int aoa_endpoint_in;
int aoa_endpoint_out;

#endif


