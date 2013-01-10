#ifndef SERVER_H
#define SERVER_H

#include <accessory.h>

void initServer(AapConnection* newcon);
AapConnection* getCurrentConnection();
void receiver();
void sender();
void deInitServer();

extern volatile int connectedToAndroid;

#endif
