package nl.ict.aapbridge.test;

import java.util.concurrent.Semaphore;

import nl.ict.aapbridge.aap.UsbConnection;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class UsbConnectorService extends Service {
	
	public static UsbConnection usb_connection;
	public static final Semaphore usb_connections = new Semaphore(0);
	
	/**
	 * This class's only purpose is to connect to a accessory.
	 * 
	 * The test cases need a accessory to test the aab.
	 * But a aap can only be started using a pendingIntent, which requires a Service, Activity or Broadcast receiver.
	 */
    public UsbConnectorService() {
    	super();
    	usb_connection = UsbConnection.easyConnect(this);
    	usb_connections.release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
