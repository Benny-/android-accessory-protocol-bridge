#ifndef ACCESSORYAUDIO_H_
#define ACCESSORYAUDIO_H_

/**
 * This function starts another thread to poll the pulseaudio server for
 * new usb audio devices and starts a loopback device.
 *
 * The accessory audio component is very loosely coupled (software wise).
 * It has barely any relation to any other code.
 */
void accessory_audio_start(void);

#endif /* ACCESSORYAUDIO_H_ */
