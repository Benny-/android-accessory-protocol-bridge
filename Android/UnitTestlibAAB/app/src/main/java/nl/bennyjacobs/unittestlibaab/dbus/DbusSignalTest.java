package nl.bennyjacobs.unittestlibaab.dbus;

import android.R.integer;
import android.util.Log;
import nl.bennyjacobs.aapbridge.bridge.AccessoryBridge;
import nl.bennyjacobs.unittestlibaab.dbus.DbusMethodTest.SyncDbusHandler;
import nl.bennyjacobs.aapbridge.dbus.message.DbusMessage;
import nl.bennyjacobs.unittestlibaab.test.BridgeFactoryService;
import static nl.bennyjacobs.unittestlibaab.test.TAG.TAG;
import nl.bennyjacobs.aapbridge.dbus.DbusMethods;
import nl.bennyjacobs.aapbridge.dbus.DbusHandler;
import nl.bennyjacobs.aapbridge.dbus.DbusSignals;

public class DbusSignalTest extends android.test.AndroidTestCase  {
	
	private AccessoryBridge bridge;
	private SyncDbusHandler synchandler = new SyncDbusHandler();
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		if(bridge == null)
		{
			bridge = BridgeFactoryService.getAAPBridge(getContext());
		}
	}
	
	public void testSignal() throws Exception
	{
		DbusSignals signals = null;
		DbusMethods methods = null;
		try
		{
			signals = new DbusSignals(bridge,
					synchandler,
					"nl.ict.AABUnitTest",
					"/nl/ict/AABUnitTest/C",
					"nl.ict.AABUnitTest.Signals",
					null);
			
			methods = new DbusMethods(bridge, synchandler);
			methods.methodCall("nl.ict.AABUnitTest",
					"/nl/ict/AABUnitTest/C",
					"nl.ict.AABUnitTest.Signals",
					"StartEmittingSignals");
			
			Object[] values;
			
			// This first message is the reply to the remote d-bus call.
			// This is a sanity check. It will throw a exception if the teststub is not running.
			DbusMessage dbusmsg;
			do{
			dbusmsg = synchandler.getDbusMessage();
			Log.d(TAG, dbusmsg.toString());
			}
			while(dbusmsg.getInterfaceName() != null && !dbusmsg.getInterfaceName().equals("nl.ict.AABUnitTest.Signals"));
			values = dbusmsg.getValues();
			assertEquals(null,values);
			
			// Now all the signals will follow. We all values in a array and assert it matches the expected values.
			
			dbusmsg = synchandler.getDbusMessage();
			values = synchandler.getDbusMessage().getValues();
			assertEquals((byte)2, values[0]);
			
			dbusmsg = synchandler.getDbusMessage();
			values = synchandler.getDbusMessage().getValues();
			assertEquals(true, values[0]);
			
			dbusmsg = synchandler.getDbusMessage();
			values = synchandler.getDbusMessage().getValues();
			assertEquals((Integer)3, values[0]);
			
			dbusmsg = synchandler.getDbusMessage();
			values = synchandler.getDbusMessage().getValues();
			assertEquals("The only real advantage to punk music is that nobody can whistle it.", values[0]);
			
			dbusmsg = synchandler.getDbusMessage();
			values = synchandler.getDbusMessage().getValues();
			assertEquals(5.5d, values[0]);
			
			dbusmsg = synchandler.getDbusMessage();
			values = synchandler.getDbusMessage().getValues();
			assertEquals("humidity1", values[0]);
			assertEquals(9.923d, values[1]);
			
			dbusmsg = synchandler.getDbusMessage();
			values = synchandler.getDbusMessage().getValues();
			assertEquals((Integer)3, values[0]);
		} finally
		{
			try
			{
				signals.close();
				methods.close();
			}
			catch(Exception e)
			{
				// Meh.
			}
		}
	}
	
}
