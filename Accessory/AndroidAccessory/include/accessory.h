#ifndef ACCESSORY_H
#define ACCESSORY_H

typedef struct Accessory Accessory;
typedef struct AapConnection AapConnection;

Accessory* initAccessory(
		const char* manufacturer,	// Used for usb accessory protocol
		const char* modelName,		// Used for bt & usb accessory protocol
		const char* description,	// Used for bt & usb accessory protocol
		const char* version,		// Used for usb accessory protocol
		const char* uri,			// Used for usb accessory protocol
		const char* serialNumber);	// Used for usb accessory protocol
void deInitaccessory(Accessory* accessory);

AapConnection* getNextAndroidConnection(Accessory* accessory);
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
 */
int writeAllAccessory	(AapConnection* con, const void* buffer, int size);

#endif
