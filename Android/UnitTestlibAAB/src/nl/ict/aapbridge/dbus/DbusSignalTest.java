package nl.ict.aapbridge.dbus;

import nl.ict.aapbridge.bridge.AccessoryBridge;
import nl.ict.aapbridge.dbus.DbusMethodTest.SyncDbusHandler;
import nl.ict.aapbridge.dbus.message.DbusMessage;
import nl.ict.aapbridge.test.UsbConnectorService;
import static nl.ict.aapbridge.test.TAG.TAG;

public class DbusSignalTest extends android.test.AndroidTestCase  {
	
	private AccessoryBridge bridge;
	private SyncDbusHandler synchandler = new SyncDbusHandler();
	private DbusSignals signals;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		if(bridge == null)
		{
			bridge = UsbConnectorService.getAAPBridge(getContext());
		}
	}
	
	@Override
	protected void tearDown() throws Exception {
		if(signals != null)
		{
			signals.close();
			signals = null;
		}
	}
	
	public void testSignal() throws Exception
	{
		signals = bridge.createDbusSignal(synchandler, "nl.ict.AABUnitTest","/nl/ict/AABUnitTest/C" ,"nl.ict.AABUnitTest.Signals" ,"StartEmittingSignals");
		DbusMessage dbusmsg = synchandler.getDbusMessage();
		synchandler.getDbusMessage().getValues();
	}
	
}
