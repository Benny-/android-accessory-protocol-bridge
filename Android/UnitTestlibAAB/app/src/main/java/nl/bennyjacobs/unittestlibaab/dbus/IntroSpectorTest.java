package nl.bennyjacobs.unittestlibaab.dbus;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import android.os.Looper;
import android.os.Message;
import android.util.Log;

import nl.bennyjacobs.aapbridge.bridge.AccessoryBridge;
import nl.bennyjacobs.aapbridge.dbus.introspection.DbusInterface;
import nl.bennyjacobs.aapbridge.dbus.introspection.IntroSpector;
import nl.bennyjacobs.aapbridge.dbus.introspection.ObjectPath;
import nl.bennyjacobs.aapbridge.dbus.message.types.DbusArray;
import nl.bennyjacobs.unittestlibaab.test.BridgeFactoryService;
import static nl.bennyjacobs.unittestlibaab.test.TAG.TAG;

public class IntroSpectorTest extends android.test.AndroidTestCase
{
	private AccessoryBridge bridge;
	private IntroSpector introSpector;
	private collectingObjectPathHandler objpathhandler = new collectingObjectPathHandler();
	
	private static class collectingObjectPathHandler extends IntroSpector.ObjectPathHandler
	{
		
		public List<ObjectPath> objectpaths = new ArrayList<ObjectPath>();
		
		@Override
		public void handleMessage(Message msg) {
			objectpaths.add((ObjectPath) msg.obj);
		}
		
		List<ObjectPath> flush()
		{
			List<ObjectPath> tmp = objectpaths;
			objectpaths = new ArrayList<ObjectPath>();
			return tmp;
		}
	};
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		if(bridge == null)
		{
			bridge = BridgeFactoryService.getAAPBridge(getContext());
		}
		
		if(introSpector == null)
		{
			introSpector = new IntroSpector(bridge, objpathhandler);
		}
	}
	
	@Override
	protected void tearDown() throws Exception {
		if(introSpector != null)
		{
			introSpector.close();
			introSpector = null;
		}
		super.tearDown();
	}
	
	public void testBusnames() throws Exception
	{
		DbusArray array = introSpector.getBusnames();
		assertTrue(array.size() > 5); // There should be at least 5 busnames for this test to succeed.
		for(int i = 0; i<5; i++)
		{
			Log.d(TAG, array.get(i).toString());
		}
	}
	
	public void testIntrospection() throws Exception
	{
		introSpector.startIntrospection("nl.ict.AABUnitTest");
		Thread.sleep(1000);
		List<ObjectPath> objectpaths = objpathhandler.flush();
		assertTrue(objectpaths.size() >= 4);
		ObjectPath echo1 = null;
		for(ObjectPath object : objectpaths)
		{
			Log.v(TAG, object.toString());
			if(object.getName().equals("/nl/ict/AABUnitTest/bulk/echo1"))
			{
				echo1 = object;
			}
		}
		assertNotNull(echo1);
		List<DbusInterface> interfaces = echo1.getInterfaces();
		assertTrue(interfaces.size() > 0);
		DbusInterface dbusInterface = null;
		for(DbusInterface iter : interfaces)
		{
			if(iter.getName().equals("nl.ict.aapbridge.bulk"))
			{
				dbusInterface = iter;
			}
		}
		assertNotNull(dbusInterface);
		assertTrue(dbusInterface.getMethods().size() > 0);
		assertTrue(dbusInterface.getMethods().get(0).startsWith("onBulkRequest"));
	}
	
}
