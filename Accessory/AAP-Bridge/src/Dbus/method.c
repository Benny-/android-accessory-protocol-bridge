#include <dbus/dbus.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>
#include <string.h>
#include <endian.h>
#include "dbuslib.h"
#include "../Message/AccessoryMessage.h"

extern DBusConnection *con;
extern DBusError dbusError;

static unsigned int bytesToDbus(char type, char* bytes, DBusBasicValue* dbusValue)
{
	 switch(type)
	 {
	 	 case 'y':
	 		 dbusValue->byt = *bytes;
	 		 return 1;
	 	 case 'b':
	 		 dbusValue->bool_val = *bytes; // Conversion from 1 bytes boolean to 4 byte boolean.
	 		 // I do not know why dbus's boolean is 4 byte.
	 		 return 1; // Our format is 1 bytes however, so we return 1.
	 	 case 'n':
	 		 dbusValue->i16 = le16toh( *((int16_t*)bytes) );
	 		 return 2;
	 	 case 'q':
	 		 dbusValue->u16 = le16toh( *((uint16_t*)bytes) );
	 		 return 2;
	 	 case 'i':
	 		 dbusValue->i32 = le32toh( *((int32_t*)bytes) );
	 		 return 4;
	 	 case 'u':
	 		 dbusValue->u32 = le32toh( *((uint32_t*)bytes) );
	 		 return 4;
	 	 case 'x':
	 		 dbusValue->i64 = le64toh( *((int64_t*)bytes) );
	 		 return 8;
	 	 case 't':
	 		 dbusValue->u64 = le64toh( *((uint64_t*)bytes) );
	 		 return 8;
	 	 case 'd':
	 		 dbusValue->dbl = le64toh( *((double*)bytes) );
	 		 return 8;
	 	 case 's':
	 		 dbusValue->str = bytes;
	 		 return strlen(bytes)+1;
	 	 default:
	 		 fprintf(stderr, "You asked me for the size of type %c, but I dont know", type);
	 		return -1;
	 }
	 return -1;
}

/**
 * This call blocks until it receives a reply from dbus.
 */
void callmethod(MultiplexedMessage* accessoryMessage) {

	void* dbusMessage = accessoryMessage->data;
	char* busname = dbusMessage;
	char* objectpath = busname + strlen(busname) + 1;
	char* interfacename = objectpath + strlen(objectpath) + 1;
	char* function_name = interfacename + strlen(interfacename) + 1;
	char* arg_pointer = function_name + strlen(function_name) + 1;

	puts("callmethod(): data-> ");
	PrintBin(accessoryMessage->data, 150);
	puts("\n");

	//dbus vars
	DBusMessage* message;
	DBusMessageIter args;
	DBusPendingCall* pending;

	//return value
	uint8_t returnvalue;

	// create a signal and check for errors
	message = dbus_message_new_method_call(
			busname,
			objectpath,
			interfacename,
			function_name);

	if (message == NULL) {
		fprintf(stderr,"Method.c: Reply message is Null\n");
	} else {
		int32_t numberOfArguments = le32toh(*arg_pointer);
		arg_pointer += 4;
		while(numberOfArguments--)
		{
			char varType;
			DBusBasicValue dbusVal;

			dbus_message_iter_init_append(message, &args);
			varType = *arg_pointer;
			arg_pointer++;
			arg_pointer = arg_pointer + bytesToDbus(varType, arg_pointer, &dbusVal);
			if (!dbus_message_iter_append_basic(&args, varType, &dbusVal)) {
				fprintf(stderr, "Out Of Memory!\n");
			exit(1);
			}
		}

		// send the message and flush the connection
		if (!dbus_connection_send_with_reply(methodCallsCon, message, &pending, -1)) { // -1 is default timeout
			fprintf(stderr,"Out Of Memory!\n");
			exit(1);
		}

		if (NULL == pending) {
			fprintf(stderr,"Pending Call Null\n");
		}

		dbus_connection_flush(methodCallsCon);

		// free the message
		dbus_message_unref(message);

		//block until theirs a reply
		if (dbus_error_is_set(&dbusError)) {
			printf("an error occurred: %s\n", dbusError.message);
			dbus_error_free(&dbusError);
		}

		// block until we receive a reply
		dbus_pending_call_block(pending);

		// get the reply message
		message = dbus_pending_call_steal_reply(pending);
		if (NULL == message) {
			printf("Reply Null\n");
		}
		// free the pending message handle
		dbus_pending_call_unref(pending);

		char* marshalled;
		int marshalled_size;
		dbus_message_marshal(message,&marshalled,&marshalled_size);// XXX: Memory leak, right here.

		// free reply
		dbus_message_unref(message);

		// TODO: Send dbus response to Android.
	}
}

/*
DBusMessage* createMessage(char id[64], int vartype1, uint8_t var1,
		int vartype2, uint8_t var2, int vartype3, uint8_t var3, int vartype4,
		uint8_t var5, char returntype, int returnvar, int prevmessage) {
	return message;
}
*/
