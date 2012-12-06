package nl.ict.aapbridge.dbus;

import java.io.IOException;

import android.app.Instrumentation;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

import nl.ict.aapbridge.aap.UsbConnection;
import nl.ict.aapbridge.bridge.AccessoryBridge;
import nl.ict.aapbridge.test.UsbConnectorService;

public class DbusTest extends android.test.AndroidTestCase  {
	
	public static final String TAG = "AABunitTest";
	private static AccessoryBridge aab;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		if(aab == null)
		{
			Context context = getContext();
			Log.v(TAG, "Context: "+context);
			UsbManager mUSBManager = UsbManager.getInstance(context);
			UsbAccessory[] accessories = mUSBManager.getAccessoryList();
			UsbAccessory accessory = (accessories == null ? null
					: accessories[0]);
			
			Intent intent = new Intent(context, UsbConnectorService.class);
			PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
			
			UsbConnection connection;
			if(!mUSBManager.hasPermission(accessory))
			{
				Log.i(TAG, "No permission to operate on accessory, requesting permission");
				mUSBManager.requestPermission(accessory, pendingIntent);
				
				UsbConnectorService.usb_connections.acquire();
				connection = UsbConnectorService.usb_connection;
			}
			else
			{
				Log.i(TAG, "Already got permission to operate on accessory");
				connection = UsbConnection.easyConnect(context);
			}
			Log.v(TAG, "" + connection);
			
			aab = new AccessoryBridge(connection);
		}
	}
	
//	public void testLock() throws Exception
//	{
//		new Dbus().methodCall("org.gnome.ScreenSaver","/org/gnome/ScreenSaver" ,"org.gnome.ScreenSaver" ,"Lock" );
//	}
	
	public void testLocalEchoAABUnitTestB() throws Exception
	{
		new Dbus(aab).methodCall("nl.ict.AABUnitTest","/nl/ict/AABUnitTest/B" ,"nl.ict.AABUnitTest.A" ,"LocalEcho" );
	}
	
	public void testLocalEchoAABUnitTestC() throws Exception
	{
		new Dbus(aab).methodCall("nl.ict.AABUnitTest","/nl/ict/AABUnitTest/C" ,"nl.ict.AABUnitTest.A" ,"LocalEcho" );
	}
}
