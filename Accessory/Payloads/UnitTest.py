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

bus = dbus.SessionBus()

InterfaceA = "nl.ict.AABUnitTest.A"

"""
bus-name   : nl.ict.AABUnitTest
objectpaths: /nl/ict/AABUnitTestB /nl/ict/AABUnitTestC
interfaces : nl.ict.AABUnitTest.B nl.ict.AABUnitTest.C nl.ict.AABUnitTest.A
"""

class AABUnitTestB(dbus.service.Object):
    InterfaceB = "nl.ict.AABUnitTest.B"
    
    def __init__(self, object_path, bus_name):
        dbus.service.Object.__init__(self, bus_name, object_path)
    
    @dbus.service.method(InterfaceA, in_signature='', out_signature='')
    def LocalEcho(self):
        print("Local echo from AABUnitTestB")

class AABUnitTestC(dbus.service.Object):
    InterfaceB = "nl.ict.AABUnitTest.C"
    
    def __init__(self, object_path, bus_name):
        dbus.service.Object.__init__(self, bus_name, object_path)
    
    @dbus.service.method(InterfaceA, in_signature='', out_signature='')
    def LocalEcho(self):
        print("Local echo from AABUnitTestC")

bus_name = dbus.service.BusName('nl.ict.AABUnitTest', bus)
serviceB = AABUnitTestB('/nl/ict/AABUnitTest/B',bus_name)
serviceC = AABUnitTestC('/nl/ict/AABUnitTest/C',bus_name)

print("Starting event loop")
loop = gobject.MainLoop()
loop.run()

