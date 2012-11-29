#ifndef SERVER_H
#define SERVER_H

void initServer();
void receiver();
void sender();
void deInitServer();

extern volatile int connectedToAndroid;

#endif
