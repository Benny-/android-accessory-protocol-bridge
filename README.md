# AAP bridge

The Android Accessory Bridge allows communication between a android device and a d-bus application on a Linux system using the android accessory protocol.

The project consist of two parts. A Android library and a accessory application (AAP-bridge, running on linux).

## Accessory

The Accessory map contains two eclipse/autotool projects. Make sure to read dependencies.txt and run `./autogen.sh` to bootstrap the autotools projects. Both projects contain a generic INSTALL file detailing further installing instructions. The payloads map contains applications which have a d-bus interface, these applications are used for testing and demonstration purposes.

- /Accessory/dependencies.txt
- /Accessory/AndroidAccessory/
- /Accessory/AAP-Bridge/
- /Accessory/Payloads/

AAP-Bridge managed the communication from android and the d-bus on the local system. It uses AndroidAccessory for the communication between the Android system. AndroidAccessory is a Library which implements the android accessory protocol on Linux.

## Android

- /Android/libAndroidAccessoryBridge/
- /Android/Remote Dbus/
- /Android/MediaRemote/
- /Android/WeatherCapeReader/
- /Android/D-Finger/
- /Android/UnitTestlibAAB/

The Accessory map contains the library for communicating to the remote d-bus. In addition to this it contains several example projects. All examples use the libAndroidAccessoryBridge library to call d-bus function and listen to d-bus signals.

All examples use the default configuration for AAP-Bridge except for MediaRemote.

### MediaRemote example
Use the AAP-Bridge configuration file found in `/Accessory/AAP-Bridge/AAP-Bridge.MediaRemote.config`. MediaRemote connects to Rhythmbox using a d-bus interface. Rhythmbox is the payload in this situation and should be running beforehand.

### D-finger example
D-finger tries to mimic D-feet's ability to query the d-bus and view all object paths.

### Remote Dbus example
Remote Dbus can be used to send arbitrary d-bus function calls and listen to arbitrary d-bus signals.

### WeatherCapeReader example
WeatherCapeReader is designed to read the sensor values from a beaglebone's weathercape. The payload `/AAP-Bridge/Accessory/Payloads/sensors.py` should be running on the beaglebone.

### Unit test
The UnitTestlibAAB project is used for unit testing. The payload `/AAP-Bridge/Accessory/Payloads/testStub.py` should be running for all unit tests to pass. More information about unit testing can be found in the package description for `nl.ict.aapbridge.test`.

