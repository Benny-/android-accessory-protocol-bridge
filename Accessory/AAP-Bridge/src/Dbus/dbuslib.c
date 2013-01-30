#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <dbus/dbus.h>
#include "dbuslib.h"

/**
 * Print binary data as hexidecimal characters to stdout
 */
void PrintBin(const char* data, int len)
{
	for(int i = 0; i<len; i++)
	{
		printf("%02hhx",*(data+i));
	}
}

/**
 * The PrintDBusMessage returns a string. It points to the last string argument in the message.
 * There is no reason why it should be this way. It was out of convenience.
 *
 * The returned string points to a part from the DBusMessage parameter and should not be freed.
 */
char* PrintDBusMessage(DBusMessage* message)
{
	char* retval = NULL;

	if(message != NULL)
	{
		printf("Sender        : %s\n",dbus_message_get_sender(message));
		printf("Destination   : %s\n",dbus_message_get_destination(message));
		printf("Response type : %s\n",dbus_message_type_to_string(dbus_message_get_type(message)) );
		printf("Object path   : %s\n",dbus_message_get_path(message));
		printf("Interface     : %s\n",dbus_message_get_interface(message));
		printf("Member(method): %s\n",dbus_message_get_member(message));
		printf("Error name    : %s\n",dbus_message_get_error_name(message));
		{
			char* marshalled;
			int size;
			dbus_message_marshal(message, &marshalled, &size);
			printf("Marshalled len: %i\n",size);
			printf("Marshalled data follows as hexidecimal\n");
			PrintBin(marshalled,size);
			printf("\n");
			free(marshalled);
		}
		printf("Signature     : %s\n",dbus_message_get_signature(message));

		DBusMessageIter MessageIter;
		if (!dbus_message_iter_init(message, &MessageIter))
			printf("Return message has no arguments.\n");
		else
		{
			do
			{
				//printf("%i\n",dbus_message_iter_get_element_type (&MessageIter));

				DBusBasicValue value;
				if(dbus_message_iter_get_arg_type(&MessageIter) == 115 )
				{
					dbus_message_iter_get_basic (&MessageIter, &value);

					if(value.str && *value.str)
					{
						printf("type          : %s (%i)\n",
							dbus_message_iter_get_signature(&MessageIter),
							dbus_message_iter_get_arg_type(&MessageIter));

						printf("%s\n",value.str);
						retval = value.str;
					}
				}
			}
			while(dbus_message_iter_next(&MessageIter));
		}
	}
	else
	{
		printf("dbus message is NULL\n");
	}

	printf("\n");
	return retval;
}

