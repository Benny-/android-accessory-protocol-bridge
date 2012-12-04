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

class AABUnitTest(dbus.service.Object):
    """
        
    """
    dbusInterface = "nl.ict.AABUnitTest"
    
    def __init__(self, object_path, bus_name):
        dbus.service.Object.__init__(self, bus_name, object_path)
    
    @dbus.service.method(dbusInterface, in_signature='', out_signature='')
    def Nop(self):
        pass

bus_name = dbus.service.BusName('nl.ict.AABUnitTest', bus)
service = AABUnitTest('/nl/ict/AABUnitTest',bus_name)

print("Starting event loop")
loop = gobject.MainLoop()
loop.run()

