
#ifndef CONFIG_H
#define CONFIG_H

/*Accessory information */

//out and in point when the android device is in android accessory protocol mode
#define IN 0x81
#define OUT 0x02

#define ACCESSORY           0x2D00
#define ACCESSORY_ADB       0x2D01
#define AUDIO               0x2D02
#define AUDIO_ADB           0x2D03
#define ACCESSORY_AUDIO     0x2D04
#define ACCESSORY_AUDIO_ADB 0x2D05

#define DEBUG 1

#define MESSAGEMAX 1024

int aoa_endpoint_in;
int aoa_endpoint_out;

#endif


