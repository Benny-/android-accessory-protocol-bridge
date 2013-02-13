#!/usr/bin/env python

from threading import Timer
from pprint import pprint
import gobject
import glob
import dbus
import dbus.service
from dbus.mainloop.glib import DBusGMainLoop

DBusGMainLoop(set_as_default=True)
gobject.threads_init() # Multithreaded python programs must call this before using threads.

sensors_paths = glob.glob('/sys/devices/platform/omap/omap_i2c.3/i2c-3/*/*_input')
# bus = dbus.SystemBus()
bus = dbus.SessionBus()

class SensorService(dbus.service.Object):
    """
        The SensorService emits sensor data to the d-bus.
        
        This program is designed to be run on a beaglebone with a weathercape.
        
        Dependencies:
        1. dbus for python (Already installed on beaglebone)
        2. gobject for python (opkg install python-pygobject)
        
        bus-name:   nl.ict.sensors
        objectpath: /nl/ict/sensors
        interface:  nl.ict.sensors
        
        Methods:
            AdjustSignalFrequency
            ReadSensors
            
        Signals:
            Sensor
        
        It is possible to adjust the emit frequency using a d-bus call.
        The following command line will set emit frequency to 3 seconds.
        dbus-send --system --print-reply --dest=nl.ict.sensors /nl/ict/sensors nl.ict.sensors.AdjustSignalFrequency double:3
        
        It is possible to manually request sensor data:
        dbus-send --system --print-reply --dest=nl.ict.sensors /nl/ict/sensors nl.ict.sensors.ReadSensors
    """
    def __init__(self, object_path, bus_name):
        dbus.service.Object.__init__(self, bus_name, object_path)
        self.frequency = 1.5
        self.Emit()
    
    def getSensorName(self,sensor):
        return sensor.split("/")[-1].split("_input")[0] # example_list[-1] will return the last item in example_list
    
    def ReadAllSensors(self):
        sensor_results = []
        for sensor in sensors_paths:
            sensor_name = self.getSensorName(sensor)
            sensor_value = None
            try:
                with open(sensor) as file:
                    sensor_value = float(file.read().strip())
                
                sensor_results.append((sensor_name,sensor_value))
            except:
                print("Could not read sensor data from sensor "+sensor_name)
        return sensor_results;
    
    @dbus.service.method('nl.ict.sensors', in_signature='', out_signature='a(sd)')
    def ReadSensors(self):
        print("Some d-bus user requested all sensor data:")
        sensors = self.ReadAllSensors()
        for sensor in sensors:
            self.Sensor(sensor[0],sensor[1])
        print("")
        
        return sensors
        
    @dbus.service.method('nl.ict.sensors', in_signature='d', out_signature='')
    def AdjustSignalFrequency(self, frequency):
        if(frequency < 0.5):
            raise Exception("You may not set emit frequency below 0.5")
        if(frequency > 20):
            raise Exception("You may not set emit frequency above 20.0")
        print("Adjusting emit frequency from "+str(self.frequency)+" to "+str(frequency))
        self.frequency = frequency

    def Emit(self):
        print("Reading all sensors for a signal emit")
        
        sensors = self.ReadAllSensors()
        for sensor in sensors:
            self.Sensor(sensor[0],sensor[1])
        print("")
        
        self.emitter = Timer(self.frequency, SensorService.Emit, [self])
        self.emitter.start()
        
    @dbus.service.signal(dbus_interface='nl.ict.sensors', signature='sd')
    def Sensor(self,name,value):
        print("Signal send -> name:"+name+" value:"+str(value))

bus_name = dbus.service.BusName('nl.ict.sensors', bus)
sensor_service = SensorService('/nl/ict/sensors',bus_name)

print("Starting event loop")
loop = gobject.MainLoop()
loop.run()

