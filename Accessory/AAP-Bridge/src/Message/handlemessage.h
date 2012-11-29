#include "AccessoryMessage.h"

#ifndef HANDLEMESSAGE_H
#define HANDLEMESSAGE_H

void decodemessage(uint8_t* message);
void encodemessage(uint8_t* data, size_t data_len,  MESSAGETYPE type);
MESSAGE* createmessage(int id, int number, int total, size_t totalsize, uint8_t* data, MESSAGETYPE type);

#endif
