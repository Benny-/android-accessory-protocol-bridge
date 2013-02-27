package nl.ict.aapbridge.dbus;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import android.os.Looper;
import android.os.Message;
import android.util.Log;

import nl.ict.aapbridge.bridge.AccessoryBridge;
import nl.ict.aapbridge.dbus.introspection.IntroSpector;
import nl.ict.aapbridge.dbus.message.types.DbusArray;
import nl.ict.aapbridge.test.BridgeFactoryService;
import static nl.ict.aapbridge.test.TAG.TAG;

public class IntroSpectorTest extends android.test.AndroidTestCase
{
	private AccessoryBridge bridge;
	private Looper looper;
	private IntroSpector introSpector;
	private SyncObjectPathHandler objectPathHandler = new SyncObjectPathHandler();
	
	static class SyncObjectPathHandler extends DbusHandler{
		
		private Queue<Message> messages = new LinkedList<Message>();
		private Semaphore lock = new Semaphore(0, true);
		
		@Override
		public void handleMessage(Message msg) {
			messages.add(Message.obtain(msg));
			lock.release();
		}
		
		public Message getDbusMessage() throws InterruptedException
		{
			lock.acquire();
			return messages.poll();
		}
	}
	
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
							introSpector = new IntroSpector(bridge, null);
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
	
	public void testObjectPathGetter() throws Exception
	{
		introSpector.startIntrospection("nl.ict.AABUnitTest");
		Thread.sleep(1000);
	}
	
}
