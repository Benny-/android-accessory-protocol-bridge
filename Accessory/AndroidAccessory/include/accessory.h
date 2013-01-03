#ifndef ACCESSORY_H
#define ACCESSORY_H

typedef struct Accessory Accessory;
typedef struct AapConnection AapConnection;

typedef struct AccessoryRead
{
	int error;
	int read;
	/**
	 * AccessoryRead.buffer points to a shared buffer (managed by struct AapConnection)
	 * and will change between calls to readAccessory(AapConnection* con)
	 *
	 * As a result, you will not have to deallocate it.
	 *
	 * The reason why it should be this way is related to usb buffer overflows.
	 * See http://libusb.sourceforge.net/api-1.0/packetoverflow.html
	 *
	 */
	void* buffer;
} AccessoryRead;

Accessory* initAccessory(
		const char* manufacturer,	// usb accessory protocol
		const char* modelName,		// usb accessory protocol
		const char* name,			// Used for bt
		const char* description,	// Used for bt & usb accessory protocol
		const char* version,		// usb accessory protocol
		const char* uri,			// usb accessory protocol
		const char* serialNumber);	// usb accessory protocol
void deInitaccessory(Accessory* accessory);

AapConnection* getNextAndroidConnection(Accessory* accessory);
void closeAndroidConnection(AapConnection* con);

int writeAccessory(const void* buffer, int size, AapConnection* con);
AccessoryRead readAccessory(AapConnection* con);

const char* AccessoryError(int errorcode);

#endif
