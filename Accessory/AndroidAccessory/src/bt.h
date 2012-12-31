
typedef struct BT_SERVICE BT_SERVICE;

BT_SERVICE* bt_listen(uuid_int[4] uuid);
/*
Return a standart server socket. accept() from socket.h can be used to obtain connections.
Close must never be called on this socket. Use release(BT_SERVICE* service) if you are done.
*/
int bt_getFD(BT_SERVICE* service);
void bt_release(BT_SERVICE* service);

