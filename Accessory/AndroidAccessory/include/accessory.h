#ifndef ACCESSORY_H
#define ACCESSORY_H

/**
 * Returned by `initAccessory()`. This struct represents the resources required to accept new connections.
 *
 * You can free these resources by calling `deInitaccessory()`.
 *
 * @see initAccessory()
 * @see deInitaccessory()
 */
typedef struct Accessory Accessory;

/**
 * Returned by `getNextAndroidConnection()`. This struct represents the connection to a remote Android device. There is no way to know if this connection is over bluetooth or usb.
 *
 * This struct is required in all the read/write functions.
 *
 * You should always close this connection using `closeAndroidConnection()` if you are done or if a error occurred on read/write.
 *
 * @see readAccessory()
 * @see readAllAccessory()
 * @see writeAccessory()
 * @see writeAllAccessory()
 */
typedef struct AapConnection AapConnection;

/*! \mainpage How to use this library
 *
 * \section intro_sec Introduction
 *
 * This library offers the ability to communicate to a Android device more easily. This library is designed to be run on any - embedded - Linux system. This library can not be run on a micro controller unit without a operating system.
 *
 * The main feature is its ability to communicate to a Android device using the Android accessory protocol or bluetooth. This is done transparently and you have no way of knowing what underlying communication protocol is used.
 *
 * \section install_sec Installation
 *
 * This project has the following dependencies:
 * \li libudev for new device detection
 * \li libusb-1.0 for usb communication
 * \li libbluetooth (bluez) for bluetooth communication
 * 
 * All libraries must be installed, regardless if only one form of communication is used.
 *
 * It is a autotools project and detailed instructions for compilation can be read in INSTALL.
 *
 * \subsection step1 Accepting connections from Android devices
 *
 * Briefly: Call `initAccessory()` to start listening. Call `getNextAndroidConnection()` to accept a new connection. Now use any of the read/write functions on the `::AapConnection`. Call `closeAndroidConnection()` when you are done.
 *
 * You may need special permissions to create a bluetooth socket or communicate using usb. This may involve adding yourself to the bluetooth group and writing a udev rule. A alternative is running your program as sudo.
 *
 * \subsection step2 Connecting to the accessory from Android
 * 
 * The Android device initiates the connection and decides if connection should be made using usb or bluetooth.
 *
 * To connect to the accessory using bluetooth you should call [createRfcommSocketToServiceRecord()](https://developer.android.com/reference/android/bluetooth/BluetoothDevice.html#createRfcommSocketToServiceRecord%28java.util.UUID%29) on the bluetooth device associated with the accessory and make sure the uuid is the same as in the `initAccessory()` function.
 * 
 * Read https://developer.android.com/guide/topics/connectivity/usb/accessory.html if you wish to connect using the usb accessory protocol.
 *
 * \example example.c
 * 
 */

/**
 * This function call starts the monitors for new incoming bluetooth and usb android connections. The arguments will be used for identification by the Android device and should match on the Android device.
 *
 * This function may be called if no bluetooth device is present or if the bluetooth device can't be accessed. This will result in listening to new usb connections only.
 *
 * Returns NULL if something went wrong.
 */
Accessory* initAccessory(
		/**
		 * Used for usb accessory protocol
		 */
		const char* manufacturer,
		/**
		 * Used for usb accessory protocol & bluetooth
		 *
		 * modelName is added to the bluetooth service discovery protocol for humans, not as identification for the Android app
		 */
		const char* modelName,
		/**
		 * Used for usb accessory protocol & bluetooth
		 *
		 * description is added to the bluetooth service discovery protocol for humans, not as identification for the Android app
		 */
		const char* description,
		/**
		 * Used for bluetooth
		 *
		 * This value is used as identification for connecting Android applications and MUST be valid. Create a new random uuid using the `uuidgen` program (ubuntu) for every non-compatible application and use it as parameter.  This uuid should match on the Android device when connecting using [bluetooth](https://developer.android.com/reference/android/bluetooth/BluetoothDevice.html#createRfcommSocketToServiceRecord%28java.util.UUID%29)
		 *
		 * Expecting a pointer to a null terminated array of uuid string pointers. Multiple uuid's are allowed as bluetooth allows cascading services (first element should be most specific, the last element should be most general). You most likely only need one uuid.
		 */
		const char* const* bt_uuids,
		/**
		 * Used for usb accessory protocol
		 */
		const char* version,
		/**
		 * Used for usb accessory protocol
		 *
		 * The android accessory protocol allows the accessory to inform a Android phone which application is required to operate the accessory.
		 *
		 * This uri will be shown when the application is not installed on the connected Android device. This uri can point to any website. Normally this points to a compatible application in the play-store.
		 */
		const char* uri,
		/**
		 * Used for usb accessory protocol
		 */
		const char* serialNumber);

/**
 * Releases all resources used by the library.
 *
 * You may not have any open Android connections when calling this function. Doing so results in undefined behavior.
 *
 * The argument may not be used once this function is called.
 */
void deInitaccessory(Accessory* accessory);

/**
 * This is a blocking function. It listens on both bluetooth and usb for a new Android connection.
 *
 * At the moment of writing only one simultaneous Android connection is supported.
 */
AapConnection* getNextAndroidConnection(Accessory* accessory);

/**
 * Release internal buffers and system resources associated with this connection.
 *
 * The argument may not be used once this function is called.
 */
void closeAndroidConnection(AapConnection* con);

/**
 * Read a number of bytes from the Android application.
 *
 * Returns bytes read or a non-positive value in case of error.
 */
int readAccessory		(AapConnection* con, void* buffer, int size_max);

/**
 * Repeatebly read from the Android application untill 'size' bytes have been read.
 *
 * Return zero on success (if all bytes have been writen) or a non-zero value in case of error.
 */
int readAllAccessory	(AapConnection* con, void* buffer, int size);

/**
 * Write a number of bytes to the Android application.
 *
 * Returns bytes read or a non-positive value in case of error.
 */
int writeAccessory		(AapConnection* con, const void* buffer, int size_max);

/**
 * Repeatebly write untill all the bytes have been send to the Android application.
 *
 * Return zero on success (if requested numbers of bytes have been read) or a non-zero value in case of error.
 *
 * This function is not thread-safe. If two or more threads call this function, the data might be intermingled.
 */
int writeAllAccessory	(AapConnection* con, const void* buffer, int size);

#endif
