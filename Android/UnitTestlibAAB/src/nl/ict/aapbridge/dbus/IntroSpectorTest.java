package nl.ict.aapbridge.dbus;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import android.os.Looper;
import android.os.Message;
import android.util.Log;

import nl.ict.aapbridge.bridge.AccessoryBridge;
import nl.ict.aapbridge.dbus.introspection.IntroSpector;
import nl.ict.aapbridge.dbus.introspection.ObjectPath;
import nl.ict.aapbridge.dbus.message.types.DbusArray;
import nl.ict.aapbridge.test.BridgeFactoryService;
import static nl.ict.aapbridge.test.TAG.TAG;

public class IntroSpectorTest extends android.test.AndroidTestCase
{
	private AccessoryBridge bridge;
	private Looper looper;
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
			// You might be thinking what might be happening here.
			// IntroSpector uses a internal Handler. It will use the
			// event loop of the thread who created the Handler.
			// In the unit test we block a lot. This is bad for the
			// event loop. To ensure we dont wait on the event loop and cause a
			// deathlock we start the IntroSpector in its own event loop thread.
			new Thread(new Runnable() {
				public void run() {
					Looper.prepare();
					looper = Looper.myLooper();
					IntroSpector localRef = null;
					try {
						synchronized (IntroSpectorTest.this) {
							introSpector = new IntroSpector(bridge, objpathhandler);
							localRef = introSpector;
							IntroSpectorTest.this.notifyAll();
						}
					} catch (Exception e) {
						Log.e(TAG, "", e);
					}
					Looper.loop();
					try {
						localRef.close();
					} catch (Exception e) {
						Log.e(TAG, "", e);
					}
				}
			}).start();
			synchronized (this) {
				while(introSpector == null)
					wait();
			}
		}
	}
	
	@Override
	protected void tearDown() throws Exception {
		if(introSpector != null)
		{
			looper.quit();
			introSpector = null;
			looper = null;
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
		introSpector.waitForIntrospection();
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
		
	}
	
}
