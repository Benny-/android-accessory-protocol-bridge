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
	
	public void testBoolean() throws Exception
	{
		new Dbus(aab).methodCall("nl.ict.AABUnitTest","/nl/ict/AABUnitTest/B" ,"nl.ict.AABUnitTest.Methods" ,"ExpectingBoolean", true );
	}
	
	public void testInt16() throws Exception
	{
		new Dbus(aab).methodCall("nl.ict.AABUnitTest","/nl/ict/AABUnitTest/B" ,"nl.ict.AABUnitTest.Methods" ,"ExpectingInt16", (short)16 );
	}
	
	public void testInt32() throws Exception
	{
		new Dbus(aab).methodCall("nl.ict.AABUnitTest","/nl/ict/AABUnitTest/B" ,"nl.ict.AABUnitTest.Methods" ,"ExpectingInt32", 700 );
	}
	
	public void testInt64() throws Exception
	{
		new Dbus(aab).methodCall("nl.ict.AABUnitTest","/nl/ict/AABUnitTest/B" ,"nl.ict.AABUnitTest.Methods" ,"ExpectingInt64", 798L );
	}
	
	public void testDouble() throws Exception
	{
		new Dbus(aab).methodCall("nl.ict.AABUnitTest","/nl/ict/AABUnitTest/B" ,"nl.ict.AABUnitTest.Methods" ,"ExpectingDouble", 7.3d );
	}
	
	public void testString() throws Exception
	{
		new Dbus(aab).methodCall("nl.ict.AABUnitTest","/nl/ict/AABUnitTest/B" ,"nl.ict.AABUnitTest.Methods" ,"ExpectingString", "Hello world" );
	}
	
	public void testMultiString() throws Exception
	{
		new Dbus(aab).methodCall("nl.ict.AABUnitTest","/nl/ict/AABUnitTest/B" ,"nl.ict.AABUnitTest.Methods" ,"ExpectingMultiString", "I","dont","care","anymore" );
	}
	
	public void testComplex1() throws Exception
	{
		new Dbus(aab).methodCall("nl.ict.AABUnitTest","/nl/ict/AABUnitTest/B" ,"nl.ict.AABUnitTest.Methods" ,"ExpectingComplex1", (byte)5, (byte)6, 34, (byte)9, (long)2333223 );
	}
}
