
#ifndef ACCESSORYMESSAGE_H
#define ACCESSORYMESSAGE_H

typedef enum MessageTypes{
	BULK,
	DBUS,
	KEEPALIVE,
	SIGNAL,
	OTHER
}MESSAGETYPE;

typedef struct AccessoryMessage
{
	int id;
	int numberofmessage;
	int totalmessages;
	int totaldatasize;
	MESSAGETYPE type;
	unsigned char data[1024];
} MESSAGE;
#endif
