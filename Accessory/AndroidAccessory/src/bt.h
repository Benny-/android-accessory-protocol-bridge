#ifndef ACCESSORY_BT_H
#define ACCESSORY_BT_H

typedef struct BT_SERVICE BT_SERVICE;

#include <inttypes.h>

#include "accessory.h"

BT_SERVICE* bt_listen(
        const char* service_name,
        const char* svc_dsc,
        const char* service_prov,
        const char* const* bt_uuids);
/*
Return a standard server socket. accept() from socket.h can be used to obtain connections.
Close must never be called on this socket. Use close(BT_SERVICE* service) if you are done.
*/
int bt_getFD(BT_SERVICE* service);
void bt_close(BT_SERVICE* service);

int readAccessoryBT   (AapConnection* con,       void* buffer, int size_max);
int writeAccessoryBT  (AapConnection* con, const void* buffer, int size_max);
void closeAccessoryBT (AapConnection* con);

#endif
