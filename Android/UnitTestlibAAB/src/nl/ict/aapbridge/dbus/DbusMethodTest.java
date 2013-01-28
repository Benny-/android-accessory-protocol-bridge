package nl.ict.aapbridge.dbus;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import android.os.Message;
import android.util.Log;
import nl.ict.aapbridge.bridge.AccessoryBridge;
import nl.ict.aapbridge.dbus.message.DbusMessage;
import nl.ict.aapbridge.test.UsbConnectorService;
import static nl.ict.aapbridge.test.TAG.TAG;

public class DbusMethodTest extends android.test.AndroidTestCase  {
	
	static class SyncDbusHandler extends DbusHandler{
		
		private Queue<DbusMessage> messages = new LinkedList<DbusMessage>();
		private Semaphore lock = new Semaphore(0);
		
		@Override
		public void handleMessage(Message msg) {
			messages.add((DbusMessage) msg.obj);
			lock.release();
		}
		
		public DbusMessage getDbusMessage() throws InterruptedException
		{
			lock.acquire();
			return messages.poll();
		}
	}
	
	private Dbus dbus;
	private SyncDbusHandler synchandler = new SyncDbusHandler();
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		if(dbus == null)
		{
			dbus = UsbConnectorService.getAAPBridge(getContext()).createDbus(synchandler);
		}
	}
	
	@Override
	protected void tearDown() throws Exception {
		if(dbus != null)
		{
			dbus.close();
			dbus = null;
		}
	}
	
	public void testLocalEchoAABUnitTestB() throws Exception
	{
		dbus.methodCall("nl.ict.AABUnitTest","/nl/ict/AABUnitTest/B" ,"nl.ict.AABUnitTest.A" ,"LocalEcho" );
		synchandler.getDbusMessage().getValues();
	}
	
	public void testLocalEchoAABUnitTestC() throws Exception
	{
		dbus.methodCall("nl.ict.AABUnitTest","/nl/ict/AABUnitTest/C" ,"nl.ict.AABUnitTest.A" ,"LocalEcho" );
		synchandler.getDbusMessage().getValues();
	}
	
	public void testByte() throws Exception
	{
		dbus.methodCall("nl.ict.AABUnitTest","/nl/ict/AABUnitTest/B" ,"nl.ict.AABUnitTest.Methods" ,"ExpectingByte", (byte) 3 );
		Object[] retvals = synchandler.getDbusMessage().getValues();
		assertEquals((byte) 3, retvals[0]);
	}
	
	public void testBoolean() throws Exception
	{
		dbus.methodCall("nl.ict.AABUnitTest","/nl/ict/AABUnitTest/B" ,"nl.ict.AABUnitTest.Methods" ,"ExpectingBoolean", true );
		Object[] retvals = synchandler.getDbusMessage().getValues();
		Log.d(TAG, "testBoolean" + retvals[0]);
		assertEquals(true, retvals[0]);
	}
	
	public void testFalseBoolean() throws Exception
	{
		dbus.methodCall("nl.ict.AABUnitTest","/nl/ict/AABUnitTest/B" ,"nl.ict.AABUnitTest.Methods" ,"ExpectingBoolean", false );
		Object[] retvals = synchandler.getDbusMessage().getValues();
		Log.d(TAG, "testFalseBoolean" + retvals[0]);
		assertEquals(false, retvals[0]);
	}
	
	public void testInt16() throws Exception
	{
		dbus.methodCall("nl.ict.AABUnitTest","/nl/ict/AABUnitTest/B" ,"nl.ict.AABUnitTest.Methods" ,"ExpectingInt16", (short)16 );
		Object[] retvals = synchandler.getDbusMessage().getValues();
		assertEquals((short)16, retvals[0]);
	}
	
	public void testInt32() throws Exception
	{
		dbus.methodCall("nl.ict.AABUnitTest","/nl/ict/AABUnitTest/B" ,"nl.ict.AABUnitTest.Methods" ,"ExpectingInt32", 700 );
		Object[] retvals = synchandler.getDbusMessage().getValues();
		assertEquals(700, retvals[0]);
	}
	
	public void testInt64() throws Exception
	{
		dbus.methodCall("nl.ict.AABUnitTest","/nl/ict/AABUnitTest/B" ,"nl.ict.AABUnitTest.Methods" ,"ExpectingInt64", 798L );
		Object[] retvals = synchandler.getDbusMessage().getValues();
		assertEquals(798L, retvals[0]);
	}
	
	public void testDouble() throws Exception
	{
		dbus.methodCall("nl.ict.AABUnitTest","/nl/ict/AABUnitTest/B" ,"nl.ict.AABUnitTest.Methods" ,"ExpectingDouble", 7.3d );
		Object[] retvals = synchandler.getDbusMessage().getValues();
		assertEquals(7.3d, retvals[0]);
	}
	
	public void testString() throws Exception
	{
		dbus.methodCall("nl.ict.AABUnitTest","/nl/ict/AABUnitTest/B" ,"nl.ict.AABUnitTest.Methods" ,"ExpectingString", "Hello world" );
		Object[] retvals = synchandler.getDbusMessage().getValues();
		assertEquals("Hello world", retvals[0]);
	}
	
	public void testMultiString() throws Exception
	{
		dbus.methodCall("nl.ict.AABUnitTest","/nl/ict/AABUnitTest/B" ,"nl.ict.AABUnitTest.Methods" ,"ExpectingMultiString", "I","dont","care","anymore" );
		Object[] retvals = synchandler.getDbusMessage().getValues();
		assertEquals(4, retvals.length);
		assertEquals("I", retvals[0]);
		assertEquals("dont", retvals[1]);
		assertEquals("care", retvals[2]);
		assertEquals("anymore", retvals[3]);
	}
	
	public void testComplex1() throws Exception
	{
		dbus.methodCall("nl.ict.AABUnitTest","/nl/ict/AABUnitTest/B" ,"nl.ict.AABUnitTest.Methods" ,"ExpectingComplex1", (byte)5, (byte)6, 34, (byte)9, (long)2333223 );
		Object[] retvals = synchandler.getDbusMessage().getValues();
		assertEquals(5, retvals.length);
		assertEquals((byte)5, retvals[0]);
		assertEquals((byte)6, retvals[1]);
		assertEquals(34, retvals[2]);
		assertEquals((byte)9, retvals[3]);
		assertEquals((long)2333223, retvals[4]);
	}
}
