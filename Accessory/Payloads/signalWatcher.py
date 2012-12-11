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

"""
Shows all the signals going over the dbus
"""

DBusGMainLoop(set_as_default=True)
bus = dbus.SessionBus()

def handler(*args, **kwargs):
    print( str(datetime.now()) + " : " + repr(args) )

signalMatch = bus.add_signal_receiver(handler_function=handler)

print("Starting event loop")
loop = gobject.MainLoop()
loop.run()

