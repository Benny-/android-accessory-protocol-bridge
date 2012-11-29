#include <stdlib.h>
#include <stdint.h>
#include <dbus/dbus.h>

#ifndef DBUSLIB_H
#define DBUSLIB_H

//dbus connection
extern DBusConnection *methodCallsCon;

typedef struct DMessage {
	char name[64];
	int vartype1;
	uint8_t var1;
	int vartype2;
	uint8_t var2;
	int vartype3;
	uint8_t var3;
	int vartype4;
	uint8_t var4;
	int returntype;
	int returnvar;
	int prevmessage;
} DMESSAGE;

char* PrintDBusMessage(DBusMessage* message);
void initDbus(DBusBusType bus_type);

#endif
