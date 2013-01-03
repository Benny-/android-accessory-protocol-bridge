# AAP bridge

The Android Accessory Bridge allows communication between a android device and a d-bus application on a Linux system using the android accessory protocol.

The project consist of two parts. A Android library and a accessory application (AAP-bridge, running on linux).

## Accessory

The Accessory map contains two eclipse/autotool project. Both contain a INSTALL file detailing installing instructions. The payloads map contains applications who have a d-bus interface, these applications are used for testing and demonstration purposes.

- /Accessory/AndroidAccessory
- /Accessory/AAP-Bridge
- /Accessory/Payloads/

AAP-Bridge managed the communication from android and the d-bus on the local system. It uses AndroidAccessory for the communication between the Android system. AndroidAccessory is a Library which implements the android accessory protocol on Linux.

## Android

- /Android/libAndroidAccessoryBridge
- /Android/Remote Dbus

The Accessory map contains the library for communicating to the remote d-bus. In addition to this it contains the "Remote Dbus" project. This Android application uses the library to send arbitrary d-bus function calls and listen to arbitrary d-bus signals.

test
