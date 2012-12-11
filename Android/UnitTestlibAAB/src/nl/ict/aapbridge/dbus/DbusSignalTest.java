package nl.ict.aapbridge.dbus;

import nl.ict.aapbridge.bridge.AccessoryBridge;
import nl.ict.aapbridge.test.UsbConnectorService;
import static nl.ict.aapbridge.test.TAG.TAG;

public class DbusSignalTest extends android.test.AndroidTestCase  {
	
	private static AccessoryBridge aab;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		if(aab == null)
		{
			aab = UsbConnectorService.getAAPBridge(getContext());
		}
		
		DbusSignals dbusSignals = new DbusSignals(aab);
		dbusSignals.addWatch("nl.ict.AABUnitTest", "/nl/ict/AABUnitTest/C", "nl.ict.AABUnitTest.Signals", "");
		Thread.sleep(500);
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		
		DbusSignals dbusSignals = new DbusSignals(aab);
		dbusSignals.removeWatch("nl.ict.AABUnitTest", "/nl/ict/AABUnitTest/C", "nl.ict.AABUnitTest.Signals", "");
		Thread.sleep(500);
	}
	
	public void testSignal() throws Exception
	{
		new Dbus(aab).methodCall("nl.ict.AABUnitTest","/nl/ict/AABUnitTest/C" ,"nl.ict.AABUnitTest.Signals" ,"StartEmittingSignals" );
		Thread.sleep(15000);
	}
	
}
