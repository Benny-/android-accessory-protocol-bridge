#!/usr/bin/env python

import time
from threading import Timer
from pprint import pprint
from datetime import datetime
import gobject
import glob
import dbus
import dbus.service
from dbus.mainloop.glib import DBusGMainLoop

DBusGMainLoop(set_as_default=True)
gobject.threads_init() # Multithreaded python programs must call this before using threads.

bus = dbus.SessionBus()

InterfaceA = "nl.ict.AABUnitTest.A"

"""
bus-name   : nl.ict.AABUnitTest
objectpaths: /nl/ict/AABUnitTestB /nl/ict/AABUnitTestC
interfaces : nl.ict.AABUnitTest.B nl.ict.AABUnitTest.Methods nl.ict.AABUnitTest.Signals
"""

class AABUnitTestB(dbus.service.Object):
    InterfaceB = "nl.ict.AABUnitTest.Methods"
    
    def __init__(self, object_path, bus_name):
        dbus.service.Object.__init__(self, bus_name, object_path)
    
    @dbus.service.method(InterfaceA, in_signature='', out_signature='')
    def LocalEcho(self):
        print(str(datetime.now()) + " Local echo from AABUnitTestB")
    
    @dbus.service.method(InterfaceB, in_signature='y', out_signature='')
    def ExpectingByte(self, y):
        print(str(datetime.now()) + " Expecting: y Got: "+repr(y) )
        
    @dbus.service.method(InterfaceB, in_signature='s', out_signature='')
    def ExpectingString(self, y):
        print(str(datetime.now()) + " Expecting: s Got: "+repr(y) )

class AABUnitTestC(dbus.service.Object):
    InterfaceC = "nl.ict.AABUnitTest.Signals"
    
    def __init__(self, object_path, bus_name):
        dbus.service.Object.__init__(self, bus_name, object_path)
    
    @dbus.service.method(InterfaceA, in_signature='', out_signature='')
    def LocalEcho(self):
        print(str(datetime.now()) + " Local echo from AABUnitTestC")
    
    @dbus.service.signal(InterfaceC, signature='d')
    def Double(self,d):
        pass
        
    @dbus.service.signal(InterfaceC, signature='i')
    def Int32(self,i):
        pass
    
    @dbus.service.signal(InterfaceC, signature='sd')
    def Sensor(self,name,value):
        pass
    
    def Emit(self):
        time.sleep(1)
        self.Double(5.5)
        time.sleep(1)
        self.Int32(7)
        time.sleep(1)
        self.Sensor("humidity1",9.923)
    
    @dbus.service.method(InterfaceC, in_signature='', out_signature='')
    def StartEmittingSignals(self):
        print("Starting to emit signals")
        emitter = Timer(0, AABUnitTestC.Emit, [self])
        emitter.start()

bus_name = dbus.service.BusName('nl.ict.AABUnitTest', bus)
serviceB = AABUnitTestB('/nl/ict/AABUnitTest/B',bus_name)
serviceC = AABUnitTestC('/nl/ict/AABUnitTest/C',bus_name)

print("Starting event loop")
loop = gobject.MainLoop()
loop.run()

