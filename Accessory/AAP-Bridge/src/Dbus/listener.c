#include <stdlib.h>
#include <stdio.h>
#include <stdint.h>
#include <string.h>
#include <pthread.h>
#include <dbus/dbus.h>
#include <sched.h>
#include "dbuslib.h"

#include "bstrlib.h"
#include "../Message/AccessoryMessage.h"
#include "../bridge.h"

typedef struct Signals
{
	BridgeService* service;
	DBusConnection* con;
	pthread_t dbusSignalWatcher;
	/**
	 * The function signalWatch() eats from the incomming dbus message queue.
	 *
	 * Sometimes another thread waits on a message in the function addSignalWatch().
	 * The message might get eaten by the signalWatch() thread. The mutex prevents it.
	 */
	pthread_mutex_t dbus_mutex;
	volatile int work;

} Signals;

/**
 * This function runs in a separate thread.
 *
 * It is started in initSignalWatcher()
 *
 * @param user_data (Ignored)
 */
static void* signalWatch(void* user_data) {
	Signals* signals = user_data;
	DBusMessage* m;

	while(signals->work)
	{
		pthread_mutex_lock(&signals->dbus_mutex);
		dbus_connection_read_write(signals->con, 100);
		m = dbus_connection_pop_message(signals->con);
		pthread_mutex_unlock(&signals->dbus_mutex);
		if(m != NULL)
		{
			char* str = PrintDBusMessage(m);

			if(dbus_message_get_type(m) == DBUS_MESSAGE_TYPE_SIGNAL)
			{
				char* marshalled;
				int size;
				dbus_message_marshal(m, &marshalled, &size);
				writeAllPort(signals->service, marshalled, size);
				free(marshalled);
			}

			dbus_message_unref(m);
		}
		else
		{
			/*
			 * pthread's mutexes are not fair (it does not prevent starvation),
			 * the signalWatch() thread might hog the mutex all for itself.
			 *
			 * Other threads might require the dbus connection (and the mutex)
			 * for adding/removing watches. One way to minimize this starvation
			 * is to yield control to another thread when the signalWatch()
			 * thread released the mutex.
			 *
			 * This is a sub-optimal solution. The best solution would be a fair mutex.
			 */
			sched_yield();
		}
	}
	fprintf(stderr, "signalWatch thread stopped\n");
	return NULL;
}

/**
 * The bstring library is a alternative to c-strings.
 * It makes appending strings together a lot easier.
 *
 * @param busname
 * @param objectpath
 * @param interface
 * @param member
 * @return A managed string object
 */
static bstring createRule(
		char* busname,
		char* objectpath,
		char* interface,
		char* member)
{
	bstring rule = bfromcstr("type='signal'");

	if(busname && *busname)
	{
		bcatcstr(rule,",sender='");
		bcatcstr(rule,busname);
		bconchar(rule,'\'');
	}

	if(objectpath && *objectpath)
	{
		bcatcstr(rule,",path='");
		bcatcstr(rule,objectpath);
		bconchar(rule,'\'');
	}

	if(interface && *interface)
	{
		bcatcstr(rule,",interface='");
		bcatcstr(rule,interface);
		bconchar(rule,'\'');
	}

	if(member && *member)
	{
		bcatcstr(rule,",member='");
		bcatcstr(rule,member);
		bconchar(rule,'\'');
	}

	return rule;
}

void addSignalWatch(
		Signals* signals,
		char* busname,
		char* objectpath,
		char* interface,
		char* signalname)
{
	DBusError dbusError;
	dbus_error_init(&dbusError);
	bstring rule = createRule(busname, objectpath, interface, signalname);

	char* cstr_rule = bstr2cstr(rule,'\0');
	printf("Rule         : %s\n",cstr_rule);

	pthread_mutex_lock(&signals->dbus_mutex);
	dbus_bus_add_match(signals->con,cstr_rule,&dbusError);
	pthread_mutex_unlock(&signals->dbus_mutex);
	if (dbus_error_is_set(&dbusError)) {
		fprintf(stderr, "%s %d: Error occurred: %s\n",__FILE__,__LINE__, dbusError.message);
		dbus_error_free(&dbusError);
	}

	bcstrfree(cstr_rule);
	bdestroy(rule);
}

void removeSignalWatch(
		Signals* signals,
		char* busname,
		char* objectpath,
		char* interface,
		char* signalname)
{
	DBusError dbusError;
	dbus_error_init(&dbusError);
	bstring rule = createRule(busname, objectpath, interface, signalname);

	char* cstr_rule = bstr2cstr(rule,'\0');
	printf("Rule         : %s\n",cstr_rule);

	pthread_mutex_lock(&signals->dbus_mutex);
	dbus_bus_remove_match(signals->con,cstr_rule,&dbusError);
	pthread_mutex_unlock(&signals->dbus_mutex);
	if (dbus_error_is_set(&dbusError)) {
		fprintf(stderr, "%s %d: Error occurred: %s\n",__FILE__,__LINE__, dbusError.message);
		dbus_error_free(&dbusError);
	}

	bcstrfree(cstr_rule);
	bdestroy(rule);
}

void* SignalsInit(BridgeService* service, char* compressed_rule)
{
	DBusError dbusError;
	dbus_error_init(&dbusError);

	Signals* signals = malloc(sizeof(Signals));
	signals->con = dbus_bus_get_private(DBUS_BUS_SESSION, &dbusError);
	signals->service = service;

	if(dbus_error_is_set(&dbusError))
	{
		fprintf(stderr, "%s %d MethodInit Error occurred: %s %s\n", __FILE__, __LINE__,  dbusError.name, dbusError.message);
		free(signals);
		return NULL;
	}
	pthread_mutex_init(&signals->dbus_mutex, NULL);

	signals->work = 1;
	dbus_threads_init_default();
    char* busname = compressed_rule;
    char* objectpath = busname + strlen(busname) + 1;
    char* interfacename = objectpath + strlen(objectpath) + 1;
    char* signalname = interfacename + strlen(interfacename) + 1;
	addSignalWatch(signals, busname, objectpath, interfacename, signalname);
	pthread_create(&signals->dbusSignalWatcher, NULL, signalWatch, signals);

	return signals;
}

void  SignalsOnBytesReceived(void* service_data, BridgeService* service, void* buffer, int size)
{
	// The signal service should never receive data. It only sends data.
}

void  SignalsOnEof(void* service_data, BridgeService* service)
{

}

void  SignalsCleanup(void* service_data, BridgeService* service)
{
	Signals* signals = service_data;

	signals->work = 0;
	pthread_join(signals->dbusSignalWatcher,NULL);
	dbus_connection_close(signals->con);
	pthread_mutex_destroy(&signals->dbus_mutex);

	free(signals);
}
