#include <stdlib.h>
#include <stdint.h>
#include <dbus/dbus.h>

#ifndef DBUSLIB_H
#define DBUSLIB_H

void PrintBin(const char* data, int len);
char* PrintDBusMessage(DBusMessage* message);

#endif
