% AAP-Bridge(1) Android accessory server

# NAME

AAP-Bridge - Server allowing communication between a Android device and the local d-bus.

# SYNOPSIS

AAP-Bridge []

# DESCRIPTION

The AAP-Bridge server allows a Android device to communicate to application on the local computer. This is done by allowing the device access to the local d-bus.

The app on the Android device will be called 'manager' in this document. The local application (The one the Android device wishes to communicate with) will be called 'payload' in this document. The program AAP-Bridge will be abridged to 'bridge'.

The manager needs to use the Android libAndroidAccessoryBridge library to correctly communicate to this server. There is no requirement on the payload other then being accessible on the d-bus (Unless the payload/manager wish to use bulk transfer, see down below). In fact, any existing d-bus application can already benefit from the AAP-Bridge and can be controlled by a custom manager app.

Only a subset of d-bus is implemented. The manager is only allowed to invoke d-bus
methods or listen to signals. It is not possible for the manager to have invokable d-bus
methods or emit signals. Please refer to the libAndroidAccessoryBridge documentation for more information about the limits of the implementation and how to invoke d-bus methods or listen to d-bus signals from Android.

Once the bridge is running, it will allow Android device to connect to the bridge using usb or bluetooth. You need to have the proper permissions to have bluetooth work correctly. This might involve adding the user to the bluetooth group. For proper usb communication you need to write a udev rule and place it in */etc/udev/rules.d/*. You can sidestep both permission requirements if you run the program as root. You should ensure the AAP-Bridge connects to the correct d-bus if you run as root.

Example udev rule (This rule might not include all Android devices):

    #Acer
    SUBSYSTEM=="usb", ATTR{idVendor}=="0502", MODE="0666", GROUP="plugdev"
    #Dell
    SUBSYSTEM=="usb", ATTR{idVendor}=="413c", MODE="0666", GROUP="plugdev"
    #Foxconn
    SUBSYSTEM=="usb", ATTR{idVendor}=="0489", MODE="0666", GROUP="plugdev"
    #Garmin-Asus
    SUBSYSTEM=="usb", ATTR{idVendor}=="091E", MODE="0666", GROUP="plugdev"
    #Google
    SUBSYSTEM=="usb", ATTR{idVendor}=="18d1", MODE="0666", GROUP="plugdev"
    #HTC
    SUBSYSTEM=="usb", ATTR{idVendor}=="0bb4", MODE="0666", GROUP="plugdev"
    #Huawei
    SUBSYSTEM=="usb", ATTR{idVendor}=="12d1", MODE="0666", GROUP="plugdev"
    #Kyocera
    SUBSYSTEM=="usb", ATTR{idVendor}=="0482", MODE="0666", GROUP="plugdev"
    #LG
    SUBSYSTEM=="usb", ATTR{idVendor}=="1004", MODE="0666", GROUP="plugdev"
    #Motorola
    SUBSYSTEM=="usb", ATTR{idVendor}=="22b8", MODE="0666", GROUP="plugdev"
    #Nvidia
    SUBSYSTEM=="usb", ATTR{idVendor}=="0955", MODE="0666", GROUP="plugdev"
    #Pantech
    SUBSYSTEM=="usb", ATTR{idVendor}=="10A9", MODE="0666", GROUP="plugdev"
    #Samsung
    SUBSYSTEM=="usb", ATTR{idVendor}=="04e8", MODE="0666", GROUP="plugdev"
    #Sharp
    SUBSYSTEM=="usb", ATTR{idVendor}=="04dd", MODE="0666", GROUP="plugdev"
    #Sony Ericsson
    SUBSYSTEM=="usb", ATTR{idVendor}=="0fce", MODE="0666", GROUP="plugdev"
    #ZTE
    SUBSYSTEM=="usb", ATTR{idVendor}=="19D2", MODE="0666", GROUP="plugdev"

# CONFIGURATION

The server does not accept any command line arguments. Any configuration should be in a configuration file. The program looks for this file in the working directory and */etc/AAP-Bridge/*. The name of the file should be *AAP-Bridge.config* to be properly found.

Here is a example configuration file:

    # This file is used by the AAP-Brdige program.
    # The program searches in the following paths for this
    # configuration file (The first configuration file found is used):
    # 1 ./AAP-Bridge.config
    # 2 /etc/AAP-Bridge/AAP-Bridge.config

    BUS = "DBUS_BUS_SESSION"

    # UUID's are used for bluetooth identification. You could have
    # multiple cascading uuid classes. Most of the time, you will
    # only need one. If you use multiple ones, put the most specific
    # service uuid on top and least specific one last. Don't use these
    # uuid's! Generate your own using the program "uuidgen"
    UUIDS = [
        "681c4035-8ac3-464a-bf5e-8672833ed8b9",
        "562f2ea2-db6b-45b3-9e9a-d5bd5ff01ede",
        "61bb8e93-a18f-4ac3-90bf-e38a7d79a4c1",
    ]

    # The following fields are used for identification on USB.
    # Android will start a application which contains the same
    # information when the Android device is connected to this
    # accessory using usb. You should explicitly set the variables
    # to a empty string if you don't use that variable.Otherwise
    # default values will be used.
    manufacturer    = "Paling & Ko"
    modelName       = "Atromotron 2000"
    description     = "Control this atromotron using your mobile phone"
    version         = "1.0"
    uri             = "http://ict.eu/"
    serialNumber    = ""
    
    # The description variable will be visible to the user when there is
    # no manager for this accessory on the Android phone.
    # The uri must be fully qualified (protocol prepended, http or https for example)

# BULK TRANSFER

Bulk transfer is a additional protocol to allow mass transfer of data. The data is not send over the d-bus and therefore does not suffer from the negative drawbacks of using d-bus for mass transfer of data (context switches and additional copying). Bulk transfer relies on fifo's to transfer the data between the bridge and the payload.

In contract to the d-bus methods and d-bus signals, the payload needs to adhere to a strict protocol to use bulk transfer. Bulk transfer is always initiated from the manager app. The bridge will create a fifo pair and issue a d-bus request to the payload. The d-bus message will contain paths to the fifo's. The payload has at this moment the chance to deny the request for bulk transfer by replying using a error. The payload should open the fifo's AFTER the d-bus method is finished. These fifo's are connected to input and output streams in the manager.

The payload should implement the *nl.ict.aapbridge.bulk* d-bus interface. This interface must have one callable function:

    onBulkRequest(String fifoToPayload, String fifoToAndroid, String requestedBulkData)

The objectpath is not specified. The developer is free to use any valid objectpath.

