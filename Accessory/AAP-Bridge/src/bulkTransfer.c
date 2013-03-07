#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <dbus/dbus.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <pthread.h>
#include <fcntl.h>

#include "flags.h"
#include "Dbus/dbuslib.h"
#include "bulkTransfer.h"

typedef struct BulkTransfer{
	BridgeService* service;
	char*	tmpdir;
	int		fifoToPayloadFD;
	int 	fifoToAndroidFD;
	pthread_t bulkTransferThread;
	pthread_mutex_t* startLock;
	char threadStarted;
} BulkTransfer;

static void* bulkTransferThread(void* user_data)
{
	BulkTransfer* bulk = user_data;
	int error = 0;
	char buffer[1024];

	pthread_mutex_lock(bulk->startLock);
	pthread_mutex_unlock(bulk->startLock);

	while(!error)
	{
		int rd = read(bulk->fifoToAndroidFD, buffer, sizeof(buffer));
		if(rd >= 1)
			writeAllPort(bulk->service, buffer, rd);
		else
		{
			fprintf(stderr, "bulk-> bulkTransferThread going to stop due to read error %i\n",rd);
			error = 1;
		}
	}

	sendEof(bulk->service);

	return NULL;
}

/**
 * Opens a d-bus communication channel to the payload and says "Your fifo is ready dear."
 *
 * Return DBusPendingCall* on succes. And NULL on failure.
 */
static DBusPendingCall* notifyPayload(char* busname, char* objectpath, char* arguments_for_payload, char* fifoToPayloadPath, char* fifoToAndroidPath)
{
	dbus_threads_init_default();

	DBusMessage* dbus_msg = dbus_message_new_method_call(busname, objectpath, "nl.ict.aapbridge.bulk", "onBulkRequest");
	DBusMessageIter args;
	DBusPendingCall* pending;
	DBusConnection* con = dbus_bus_get(FLAGS_bustype, NULL);

	if(dbus_msg == NULL || con == NULL)
		return NULL;

	dbus_message_iter_init_append(dbus_msg, &args);
	DBusBasicValue dbusVal;
	dbusVal.str = fifoToPayloadPath;
	dbus_message_iter_append_basic(&args, 's', &dbusVal);
	dbusVal.str = fifoToAndroidPath;
	dbus_message_iter_append_basic(&args, 's', &dbusVal);
	dbusVal.str = arguments_for_payload;
	dbus_message_iter_append_basic(&args, 's', &dbusVal);
	dbus_connection_send_with_reply(con, dbus_msg, &pending, -1);
	dbus_connection_flush(con);
	dbus_message_unref(dbus_msg);

	return pending;
}

/**
 * Return Zero if everything went well. Returns a non-zero value if payload diddent or failed to open fifo's.
 */
static int checkPayloadResponse( DBusPendingCall* pending)
{
	dbus_pending_call_block(pending);
	DBusMessage* dbus_msg = dbus_pending_call_steal_reply(pending);
	dbus_pending_call_unref(pending);

	if(dbus_message_get_error_name(dbus_msg) != NULL)
	{
		fprintf(stderr, "bulk-> transfer could not be started due to payload response: %s\n", dbus_message_get_error_name(dbus_msg));
		PrintDBusMessage(dbus_msg);
		dbus_message_unref(dbus_msg);
		return 1;
	}

	dbus_message_unref(dbus_msg);
	return 0;
}

static void* BulkInitInternal(BridgeService* service, pthread_mutex_t* startLock, char* busname, char* objectpath, char* arguments_for_payload)
{
	BulkTransfer* bulk = malloc(sizeof(BulkTransfer));
	bulk->service = service;
	bulk->tmpdir = NULL;
	bulk->fifoToPayloadFD = -1;
	bulk->fifoToAndroidFD = -1;
	bulk->threadStarted = 0;
	bulk->startLock = startLock;

	mkdir("/tmp/aap-bridge", S_IRWXU);
	mkdir("/tmp/aap-bridge/bulk", S_IRWXU);

	char tmpdir[] = "/tmp/aap-bridge/bulk/XXXXXX";
	mkdtemp(tmpdir);
	bulk->tmpdir = malloc(sizeof(tmpdir));
	strcpy(bulk->tmpdir, tmpdir);

	printf("bulk-> tmp dir: %s\n", bulk->tmpdir);

	char fifoToPayloadPath[100];
	sprintf(fifoToPayloadPath, "%s/fifoToPayload", bulk->tmpdir);
	if (mkfifo(fifoToPayloadPath, S_IRWXU))
	{
		BulkCleanup(bulk, NULL);
		return NULL;
	}

	char fifoToAndroidPath[100];
	sprintf(fifoToAndroidPath, "%s/fifoToAndroid", bulk->tmpdir);
	if (mkfifo(fifoToAndroidPath, S_IRWXU))
	{
		BulkCleanup(bulk, NULL);
		return NULL;
	}

	DBusPendingCall* pending = notifyPayload(busname, objectpath, arguments_for_payload, fifoToPayloadPath, fifoToAndroidPath);
	if (pending == NULL)
	{
		BulkCleanup(bulk, NULL);
		return NULL;
	}

	if (checkPayloadResponse(pending))
	{
		BulkCleanup(bulk, NULL);
		return NULL;
	}

	// Normally the open() function will block until the other end is opened too.
	// This could lead to a deathlock if the fifo's are not opened in correct order.
	// As a solution, we could open the fifo we will read in a non-blocking way.
	// And put it back into blocking mode. This allows the payload to open the
	// fifo's in any order.
	bulk->fifoToAndroidFD = open(fifoToAndroidPath, O_RDONLY | O_NONBLOCK);
	if(bulk->fifoToAndroidFD == -1)
	{
		BulkCleanup(bulk, NULL);
		return NULL;
	}

	bulk->fifoToPayloadFD = open(fifoToPayloadPath, O_WRONLY);
	if(bulk->fifoToPayloadFD == -1)
	{
		BulkCleanup(bulk, NULL);
		return NULL;
	}

	// Here we put the fifo we are reading back into blocking mode.
	int flags = fcntl( bulk->fifoToAndroidFD, F_GETFL );
	fcntl(bulk->fifoToAndroidFD, F_SETFL, flags & ~O_NONBLOCK);

	pthread_create(&bulk->bulkTransferThread, NULL, bulkTransferThread, bulk);
	bulk->threadStarted = 1;

	return bulk;
}

void* BulkInit(BridgeService* service, pthread_mutex_t* startLock, char* arguments)
{
	char* busname = arguments;
	char* objectpath = busname + strlen(busname) + 1;
	char* arguments_for_payload = objectpath + strlen(objectpath) + 1;
	return BulkInitInternal(service, startLock, busname, objectpath, arguments_for_payload);
}

void BulkOnBytesReceived(void* service_data, BridgeService* service, void* buffer, int size)
{
	BulkTransfer* bulk = service_data;

	while(size > 0)
	{
		int wrote;
		if ( (wrote = write(bulk->fifoToPayloadFD, buffer, size)) == -1)
		{
			fprintf(stderr, "BulkTransfer: %s %d -> %s %i\n",__FILE__, __LINE__, "write returned error ", wrote);
			sendClose(bulk->service);
			return;
		}
		size -= wrote;
	}

}

void BulkOnEof(void* service_data, BridgeService* service)
{
	BulkTransfer* bulk = service_data;

	close(bulk->fifoToPayloadFD);
	bulk->fifoToPayloadFD = -1;
}

void BulkCleanup(void* service_data, BridgeService* service)
{
	BulkTransfer* bulk = service_data;

	char fifoToPayloadPath[100];
	sprintf(fifoToPayloadPath, "%s/fifoToPayload", bulk->tmpdir);

	char fifoToAndroidPath[100];
	sprintf(fifoToAndroidPath, "%s/fifoToAndroid", bulk->tmpdir);

	if(bulk->fifoToAndroidFD != -1)
		close(bulk->fifoToAndroidFD);

	if(bulk->fifoToPayloadFD != -1)
		close(bulk->fifoToPayloadFD);

	remove(fifoToAndroidPath);
	remove(fifoToPayloadPath);

	if(bulk->tmpdir != NULL)
	{
		rmdir(bulk->tmpdir);
		free(bulk->tmpdir);
	}

	if(bulk->threadStarted)
		pthread_join(bulk->bulkTransferThread,NULL);

	free(bulk);
}
