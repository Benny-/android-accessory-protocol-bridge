package nl.ict.aapbridge.test;

import static nl.ict.aapbridge.test.TAG.TAG;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import org.xmlpull.v1.XmlPullParserException;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

import nl.ict.aapbridge.aap.AccessoryConnection;
import nl.ict.aapbridge.aap.BTConnection;
import nl.ict.aapbridge.aap.UsbConnection;
import nl.ict.aapbridge.bridge.AccessoryBridge;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class BridgeFactoryService extends Service {
	
	public static UsbConnection usb_connection;
	public static final Semaphore usb_connections = new Semaphore(0);
	
	/**
	 * This class's only purpose is to connect to a accessory.
	 * 
	 * The test cases need a accessory to test the aab.
	 * But a aap can only be started using a pendingIntent, which requires a Service, Activity or Broadcast receiver.
	 * @throws IOException 
	 */
    public BridgeFactoryService() throws IOException {
    	super();
    	usb_connection = UsbConnection.easyConnect(this);
    	usb_connections.release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    private static AccessoryBridge aab = null;
    
    public static AccessoryBridge getBtAAPBridge(Context context) throws InterruptedException, IOException, XmlPullParserException
    {
    	if(aab == null)
    	{
    		// The following bluetooth address is from a machine running the aab-bridge program and running the teststub.py.
    		// Modify it if the unit test machine changes.
    		AccessoryConnection connection = new BTConnection(context, "90:21:55:57:08:B6");
			aab = new AccessoryBridge(connection);
    	}
    	return aab;
    }
    
    public static AccessoryBridge getUsbAAPBridge(Context context) throws InterruptedException, IOException
    {
    	if(aab == null)
    	{
    		Log.v(TAG, "Context: "+context);
			UsbManager mUSBManager = UsbManager.getInstance(context);
			UsbAccessory[] accessories = mUSBManager.getAccessoryList();
			UsbAccessory accessory = (accessories == null ? null
					: accessories[0]);
			
			Intent intent = new Intent(context, BridgeFactoryService.class);
			PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
			
			UsbConnection connection;
			if(!mUSBManager.hasPermission(accessory))
			{
				Log.i(TAG, "No permission to operate on accessory, requesting permission");
				mUSBManager.requestPermission(accessory, pendingIntent);
				
				BridgeFactoryService.usb_connections.acquire();
				connection = BridgeFactoryService.usb_connection;
			}
			else
			{
				Log.i(TAG, "Already got permission to operate on accessory");
				connection = UsbConnection.easyConnect(context);
			}
			Log.v(TAG, "" + connection);
			
			aab = new AccessoryBridge(connection);
    	}
    	return aab;
    }
    
    public static AccessoryBridge getAAPBridge(Context context) throws InterruptedException, IOException
    {
    	try
    	{
    		getBtAAPBridge(context);
    	} catch(Exception e)
    	{
    		getUsbAAPBridge(context);
    	}
    	return aab;
    }
}
