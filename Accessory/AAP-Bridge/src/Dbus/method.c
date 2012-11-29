#include <dbus/dbus.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>
#include "dbuslib.h"
#include "../Message/AccessoryMessage.h"
#include "../Message/handlemessage.h"

extern DBusConnection *con;
extern DBusError dbusError;
//blocking
void callmethod(MESSAGE* accessoryMessage) {

	void* dbusMessage = accessoryMessage->data;

	//dbus vars
	DBusMessage* message;
	DBusMessageIter args;
	DBusPendingCall* pending;

	//return value
	uint8_t returnvalue;

	// create a signal and check for errors
	message = dbus_message_new_method_call(
			"org.gnome.ScreenSaver",
			"/org/gnome/ScreenSaver",
			"org.gnome.ScreenSaver",
			dbusMessage);

	if (message == NULL) {
		printf("Message Null\n");
	} else {
		/*
		 //@todo check for empty vartypes/vars not the nices way for adding multiple vars to method solution
		 dbus_message_iter_init_append(message, &args);
		 if (!dbus_message_iter_append_basic(&args, dbusMessage->vartype1, &dbusMessage->var1)) {
		 printff(stderr, "Out Of Memory!\n");
		 exit(1);
		 }

		 if (!dbus_message_iter_append_basic(&args, dbusMessage->vartype2, &dbusMessage->var2)) {
		 printff(stderr, "Out Of Memory!\n");
		 exit(1);
		 }

		 if (!dbus_message_iter_append_basic(&args, dbusMessage->vartype3, &dbusMessage->var3)) {
		 printff(stderr, "Out Of Memory!\n");
		 exit(1);
		 }

		 if (!dbus_message_iter_append_basic(&args, dbusMessage->vartype4, &dbusMessage->var4)) {
		 printff(stderr, "Out Of Memory!\n");
		 exit(1);
		 }
		 */
		// send the message and flush the connection
		if (!dbus_connection_send_with_reply(methodCallsCon, message, &pending, -1)) { // -1 is default timeout
			printf("Out Of Memory!\n");
			exit(1);
		}


		if (NULL == pending) {
			printf("Pending Call Null\n");
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

		// read the parameters
		if (!dbus_message_iter_init(message, &args))
			printf("Message has no arguments!\n");

		//find a solution for dynamic returning....
		else if (DBUS_TYPE_BOOLEAN != dbus_message_iter_get_arg_type(&args))
			printf("Argument is not boolean!\n");
		else
			dbus_message_iter_get_basic(&args, &returnvalue);

		char* marshalled;
		int marshalled_size;
		dbus_message_marshal(message,&marshalled,&marshalled_size);// XXX: Memory leak, right here.

		// free reply
		dbus_message_unref(message);

		encodemessage(marshalled, marshalled_size, DBUS);
	}
}

/*
DBusMessage* createMessage(char id[64], int vartype1, uint8_t var1,
		int vartype2, uint8_t var2, int vartype3, uint8_t var3, int vartype4,
		uint8_t var5, char returntype, int returnvar, int prevmessage) {
	return message;
}
*/
