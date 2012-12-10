package nl.ict.aapbridge.dbus;

import nl.ict.aapbridge.bridge.AccessoryBridge;
import nl.ict.aapbridge.test.UsbConnectorService;
import static nl.ict.aapbridge.test.TAG.TAG;

public class DbusMethodTest extends android.test.AndroidTestCase  {
	
	private AccessoryBridge aab;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		if(aab == null)
		{
			aab = UsbConnectorService.getAAPBridge(getContext());
		}
	}
	
	public void testLocalEchoAABUnitTestB() throws Exception
	{
		new Dbus(aab).methodCall("nl.ict.AABUnitTest","/nl/ict/AABUnitTest/B" ,"nl.ict.AABUnitTest.A" ,"LocalEcho" );
	}
	
	public void testLocalEchoAABUnitTestC() throws Exception
	{
		new Dbus(aab).methodCall("nl.ict.AABUnitTest","/nl/ict/AABUnitTest/C" ,"nl.ict.AABUnitTest.A" ,"LocalEcho" );
	}
	
	public void testByte() throws Exception
	{
		new Dbus(aab).methodCall("nl.ict.AABUnitTest","/nl/ict/AABUnitTest/B" ,"nl.ict.AABUnitTest.Methods" ,"ExpectingByte", (byte) 3 );
	}
	
	public void testString() throws Exception
	{
		new Dbus(aab).methodCall("nl.ict.AABUnitTest","/nl/ict/AABUnitTest/B" ,"nl.ict.AABUnitTest.Methods" ,"ExpectingString", "Hello world" );
	}
}
