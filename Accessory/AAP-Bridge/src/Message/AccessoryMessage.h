
#ifndef ACCESSORYMESSAGE_H
#define ACCESSORYMESSAGE_H

typedef struct MultiplexedMessage
{
	short port;
	short size;
	void* data;
} MultiplexedMessage;

#endif
