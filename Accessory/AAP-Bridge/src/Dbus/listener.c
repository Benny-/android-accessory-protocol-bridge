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
#include "../server.h"
#include "../Message/handlemessage.h"

static DBusConnection* con;
static pthread_t dbusSignalWatcher;

/**
 * The function signalWatch() eats from the incomming dbus message queue.
 *
 * Sometimes another thread waits on a message in the function addSignalWatch().
 * The message might get eaten by the signalWatch() thread. The mutex prevents it.
 */
static pthread_mutex_t dbus_mutex;
static volatile int work = 1;

/**
 * This function runs in a separate thread.
 *
 * It is started by initSignalWatcher()
 *
 * @param user_data (Ignored)
 */
static void* signalWatch(void* user_data) {
	DBusMessage* m;

	while(work)
	{
		pthread_mutex_lock(&dbus_mutex);
		dbus_connection_read_write(con, 100);
		m = dbus_connection_pop_message(con);
		pthread_mutex_unlock(&dbus_mutex);
		if(m != NULL)
		{
			char* str = PrintDBusMessage(m);
			if(connectedToAndroid)
			{
				char* marshalled;
				int size;
				dbus_message_marshal(m, &marshalled, &size);
				encodemessage(marshalled, size, SIGNAL);
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
			 * Other threads might require the dbus connection (and the mutex).
			 * One way to minimize this starvation is to yield control to another
			 * thread when the signalWatch() thread released the mutex.
			 *
			 * This is a sub-optimal solution. The best solution would be a fair mutex.
			 */
			sched_yield();
		}
	}
	return NULL;
}

void initSignalWatcher(DBusBusType bus_type)
{
	DBusError dbusError;
	dbus_error_init(&dbusError);
	dbus_threads_init_default();

	con = dbus_bus_get_private(bus_type, &dbusError);

	if (dbus_error_is_set(&dbusError)) {
		fprintf(stderr, "%s %d: Error occurred: %s\n",__FILE__,__LINE__, dbusError.message);
		dbus_error_free(&dbusError);
	}

	if (con == NULL) {
		exit(1);
	}

	work = 1;
	pthread_create(&dbusSignalWatcher, NULL, signalWatch, NULL);
}

void deInitSignalWatcher()
{
	dbus_connection_close(con);

	work = 0;
	pthread_join(dbusSignalWatcher,NULL);
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

	pthread_mutex_lock(&dbus_mutex);
	dbus_bus_add_match(con,cstr_rule,&dbusError);
	pthread_mutex_unlock(&dbus_mutex);
	if (dbus_error_is_set(&dbusError)) {
		fprintf(stderr, "%s %d: Error occurred: %s\n",__FILE__,__LINE__, dbusError.message);
		dbus_error_free(&dbusError);
	}

	bcstrfree(cstr_rule);
	bdestroy(rule);
}

void removeSignalWatch(
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

	pthread_mutex_lock(&dbus_mutex);
	dbus_bus_remove_match(con,cstr_rule,&dbusError);
	pthread_mutex_unlock(&dbus_mutex);
	if (dbus_error_is_set(&dbusError)) {
		fprintf(stderr, "%s %d: Error occurred: %s\n",__FILE__,__LINE__, dbusError.message);
		dbus_error_free(&dbusError);
	}

	bcstrfree(cstr_rule);
	bdestroy(rule);
}
