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
    
    @dbus.service.method(InterfaceB, in_signature='y', out_signature='y')
    def ExpectingByte(self, val):
        print(str(datetime.now()) + " Expecting: y Got: "+repr(val) )
        return val;
    
    @dbus.service.method(InterfaceB, in_signature='b', out_signature='b')
    def ExpectingBoolean(self, val):
        print(str(datetime.now()) + " Expecting: b Got: "+repr(val) )
        return val;
    
    @dbus.service.method(InterfaceB, in_signature='n', out_signature='n')
    def ExpectingInt16(self, val):
        print(str(datetime.now()) + " Expecting: n Got: "+repr(val) )
        return val;
    
    @dbus.service.method(InterfaceB, in_signature='q', out_signature='q')
    def ExpectingUint16(self, val):
        print(str(datetime.now()) + " Expecting: q Got: "+repr(val) )
        return val;
    
    @dbus.service.method(InterfaceB, in_signature='i', out_signature='i')
    def ExpectingInt32(self, val):
        print(str(datetime.now()) + " Expecting: i Got: "+repr(val) )
        return val;
    
    @dbus.service.method(InterfaceB, in_signature='u', out_signature='u')
    def ExpectingUint32(self, val):
        print(str(datetime.now()) + " Expecting: u Got: "+repr(val) )
        return val;
    
    @dbus.service.method(InterfaceB, in_signature='x', out_signature='x')
    def ExpectingInt64(self, val):
        print(str(datetime.now()) + " Expecting: x Got: "+repr(val) )
        return val;

    @dbus.service.method(InterfaceB, in_signature='t', out_signature='t')
    def ExpectingUint64(self, val):
        print(str(datetime.now()) + " Expecting: t Got: "+repr(val) )
        return val;

    @dbus.service.method(InterfaceB, in_signature='d', out_signature='d')
    def ExpectingDouble(self, val):
        print(str(datetime.now()) + " Expecting: d Got: "+repr(val) )
        return val;

    @dbus.service.method(InterfaceB, in_signature='s', out_signature='s')
    def ExpectingString(self, val):
        print(str(datetime.now()) + " Expecting: s Got: "+repr(val) )
        return val;

    @dbus.service.method(InterfaceB, in_signature='o', out_signature='o')
    def ExpectingObjectPath(self, val):
        print(str(datetime.now()) + " Expecting: o Got: "+repr(val) )
        return val;

    @dbus.service.method(InterfaceB, in_signature='g', out_signature='g')
    def ExpectingSignature(self, val):
        print(str(datetime.now()) + " Expecting: g Got: "+repr(val) )
        return val;

    @dbus.service.method(InterfaceB, in_signature='ai', out_signature='ai')
    def ExpectingArrayInt32(self, val):
        print(str(datetime.now()) + " Expecting: ai Got: "+repr(val) )
        return val;

    @dbus.service.method(InterfaceB, in_signature='(isi)', out_signature='(isi)')
    def ExpectingStruct1(self, val):
        print(str(datetime.now()) + " Expecting: (isi) Got: "+repr(val) )
        return val;

    @dbus.service.method(InterfaceB, in_signature='a{si}', out_signature='a{si}')
    def ExpectingDict(self, val):
        print(str(datetime.now()) + " Expecting: a{si} Got: "+repr(val) )
        return val;

    @dbus.service.method(InterfaceB, in_signature='h', out_signature='h')
    def ExpectingFd(self, val):
        print(str(datetime.now()) + " Expecting: h Got: "+repr(val) )
        return val;

    @dbus.service.method(InterfaceB, in_signature='ssss', out_signature='ssss')
    def ExpectingMultiString(self, uno, duo, tres, dos ):
        print(str(datetime.now()) + " Expecting: ssss Got: "+repr( (uno, duo, tres, dos) ))
        return (uno, duo, tres, dos);

    @dbus.service.method(InterfaceB, in_signature='yyiyx', out_signature='yyiyx')
    def ExpectingComplex1(self, byte1,byte2,i,byte3,x ):
        print(str(datetime.now()) + " Expecting: yyiyx Got: "+repr( (byte1,byte2,i,byte3,x) ))
        return (byte1,byte2,i,byte3,x);

class AABUnitTestC(dbus.service.Object):
    InterfaceC = "nl.ict.AABUnitTest.Signals"
    
    def __init__(self, object_path, bus_name):
        dbus.service.Object.__init__(self, bus_name, object_path)
    
    @dbus.service.method(InterfaceA, in_signature='', out_signature='')
    def LocalEcho(self):
        print(str(datetime.now()) + " Local echo from AABUnitTestC")

    @dbus.service.signal(InterfaceC, signature='y')
    def Byte(self,val):
        pass
    
    @dbus.service.signal(InterfaceC, signature='b')
    def Boolean(self,val):
        pass
    
    @dbus.service.signal(InterfaceC, signature='n')
    def Int16(self,val):
        pass
    
    @dbus.service.signal(InterfaceC, signature='q')
    def Uint32(self,val):
        pass
    
    @dbus.service.signal(InterfaceC, signature='i')
    def Int32(self,val):
        pass
    
    @dbus.service.signal(InterfaceC, signature='d')
    def Double(self,val):
        pass
    
    @dbus.service.signal(InterfaceC, signature='s')
    def String(self,val):
        pass
    
    @dbus.service.signal(InterfaceC, signature='sd')
    def Sensor(self,name,value):
        pass
    
    @dbus.service.signal(InterfaceC, signature='ysdyi')
    def Complex1(self,var1,var2,var3,var4,var5):
        pass
    
    def Emit(self):
        time.sleep(1)
        self.Byte(2)
        time.sleep(1)
        self.Boolean(True)
        time.sleep(1)
        self.Int32(3)
        time.sleep(1)
        self.String("The only real advantage to punk music is that nobody can whistle it.")
        time.sleep(1)
        self.Double(5.5)
        time.sleep(1)
        self.Sensor("humidity1",9.923)
        time.sleep(1)
        self.Complex1(8,"Never do today what you can put off until tomorrow.",45.00000003,9,9084)
    
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

