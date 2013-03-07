#include <dbus/dbus.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>
#include <string.h>
#include <endian.h>
#include "dbuslib.h"
#include "../Message/AccessoryMessage.h"

#include "../flags.h"
#include "method.h"

/**
 * Reads the type "type" from pointer "bytes" and puts the d-bus value into dbusValue
 *
 * Return the size of the data read from the byte array
 */
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

typedef struct Methods
{
	DBusConnection* con;
	BridgeService* service;
} Methods;

void* MethodInit(BridgeService* service)
{
	DBusError dbusError;
	dbus_error_init(&dbusError);
	Methods* methods = malloc(sizeof(Methods));
	methods->con = dbus_bus_get(FLAGS_bustype, &dbusError);
	methods->service = service;

	dbus_threads_init_default();

	if(dbus_error_is_set(&dbusError))
	{
		fprintf(stderr, "%s %d MethodInit Error occurred: %s %s\n", __FILE__, __LINE__,  dbusError.name, dbusError.message);
		free(methods);
		return NULL;
	}
	return methods;
}

/**
 * At this moment we are waiting for the d-bus method to finish.
 *
 * This might become a issue, as the service handling thread cant send bytes to other services.
 * In that case we should wait for the response in another thread or use poll, epoll, select, ect..
 */
void MethodOnBytesReceived(void* service_data, BridgeService* service, void* buffer, int size)
{
	Methods* methods = service_data;
	void* dbusMessage = buffer;
	char* busname = dbusMessage;
	char* objectpath = busname + strlen(busname) + 1;
	char* interfacename = objectpath + strlen(objectpath) + 1;
	char* function_name = interfacename + strlen(interfacename) + 1;
	char* arg_pointer = function_name + strlen(function_name) + 1;

	//dbus vars
	DBusMessage* message;
	DBusMessageIter args;
	DBusPendingCall* pending;

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
		if (!dbus_connection_send_with_reply(methods->con, message, &pending, -1)) { // -1 is default timeout
			fprintf(stderr,"Out Of Memory!\n");
			exit(1);
		}

		if (NULL == pending) {
			fprintf(stderr,"Pending Call Null\n");
		}

		dbus_connection_flush(methods->con);

		// block until we receive a reply
		dbus_pending_call_block(pending);

		// get the reply message
		DBusMessage* reply = dbus_pending_call_steal_reply(pending);

		// free the pending message handle
		dbus_pending_call_unref(pending);

		if(reply != NULL)
		{
			char* marshalled;
			int marshalled_size;
			dbus_message_marshal(reply,&marshalled,&marshalled_size);
			// Our little protocol can only handle dbus messages of
			// 4000 bytes or less.
			if(marshalled_size > 4000)
			{
				fprintf(stderr,"Could not send a d-bus reply back (It is too big: %i bytes). Sending a exception back.\n", marshalled_size);
				char* marshalled_error;
				int marshalled_error_size;
				DBusMessage* error = dbus_message_new_error_printf(
						message,
						"nl.ict.aab.dbus.messageTooLong",
						"The reply is %i bytes",
						marshalled_size);
				dbus_message_marshal(error, &marshalled_error, &marshalled_error_size);
				writeAllPort(service, marshalled_error, marshalled_error_size);
				free(marshalled_error);
			}
			else
			{
				writeAllPort(service, marshalled, marshalled_size);
			}
			free(marshalled);
		}
		else
		{
			fprintf(stderr, "Something horrible went wrong and we are sorry\n");
			// TODO: Send something to Android to inform of our failure.
		}

		dbus_message_unref(message);
		dbus_message_unref(reply);
	}
}

void  MethodOnEof(void* service_data, BridgeService* service)
{
	sendEof(service);
}

void  MethodCleanup(void* service_data, BridgeService* service)
{
	Methods* methods = service_data;
	// dbus_connection_close(methods->con); // Only private connections should be closed. Currently this is a shared d-bus connection.
	free(methods);
}
