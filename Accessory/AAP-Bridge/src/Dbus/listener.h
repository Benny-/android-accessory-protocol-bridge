#ifndef LISTENER_H
#define LISTENER_H

void initSignalWatcher();
void deInitSignalWatcher();

void addSignalWatch(
		char* busname,
		char* objectpath,
		char* interface,
		char* signalname);
void removeSignalWatch(
		char* busname,
		char* objectpath,
		char* interface,
		char* signalname);

#endif


