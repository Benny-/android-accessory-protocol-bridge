package nl.ict.aapbridge.bridge;

import static nl.ict.aapbridge.TAG.TAG;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import nl.ict.aapbridge.SystemHolder;
import nl.ict.aapbridge.aap.AccessoryConnection;
import nl.ict.aapbridge.bridge.AccessoryMessage.MessageType;
import nl.ict.aapbridge.dbus.message.DbusMessage;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;


/**
 * The class that handles the communication with Android Accessory
 * @author jurgen
 */
public class AccessoryBridge
{
	private AccessoryConnection connection;
	private OutputStream mFops;
	public static Queue<AccessoryMessage> messages;
	public static Handler handler = new Handler();
	private Timer pinger = new Timer("Pinger", true);
	
	public AccessoryBridge(AccessoryConnection connection) throws IOException {
		this.connection = connection;
		mFops = this.connection.getOutputStream();
		new UsbListener(this.connection.getInputStream()).start();
		messages = new LinkedList<AccessoryMessage>();
		pinger.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				try {
					AccessoryBridge.this.Write("Ping".getBytes(), 0, MessageType.KEEPALIVE);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}, 0, 10000);
	}
	
	public void Disconnect() throws IOException
	{
		connection.close();
	}
	
	/**
	 * Write to the Android Accessory bus
	 * @param message
	 * @param id
	 * @param type
	 * @throws Exception
	 */
	public void Write(byte[] message, int id, AccessoryMessage.MessageType type) throws Exception {

		byte[] buffer = MessageHandler.encode(message, id, type); //encode message
		
		if(buffer != null) { //if message is null, there's a segmented message. 
			mFops.write(buffer);
		}  else {
			//TODO handle segmented message
		}
	}
	
	/**
	 * Thread who receive the data from Android Accessory bus, decodes it and sends it to the queue
	 * @author Jurgen
	 *
	 */
	public class UsbListener extends Thread{
		private InputStream inputStream;
		
		public UsbListener(InputStream inputStream) {
			this.inputStream = inputStream;
		} 
		
		public void run() {
			byte[] buffer = new byte[Config.MESSAGEMAX];
			int ret = 1;
			try {
			while(ret >= 0)
			{
					Log.d(TAG, "Reading mFips");
					ret = inputStream.read(buffer);
					
					// The following block will crash the next "mFips.read(buffer);"
					// IDK what is going on.
					{
//						StringBuilder strbuilder = new StringBuilder("Read from buffer (size "+ret+"): ");
//						for(int i = 0; i<ret; i++)
//						{
//							strbuilder.append(String.format("%02x", buffer[i]));
//						}
//						Log.d(TAG, strbuilder.toString());
					}
					
					Log.d(TAG, "Decoding the bytes received from the accessory");
					MessageHandler.decode(buffer);
					
					final AccessoryMessage message = AccessoryBridge.messages.peek();
					if(message != null)
					{
						AccessoryBridge.messages.remove();
						Log.v(TAG, "Message received: "+message);
						
						if(message.getType() == MessageType.KEEPALIVE && message.toString().length() > 1)
						{
							handler.post(new Runnable() {
								public void run() {
									Toast.makeText(SystemHolder.getContext(), "Received a "+message.getType().toString()+": "+message, Toast.LENGTH_SHORT).show();
								}
							});
						} else if(message.getType() == MessageType.SIGNAL)
						{
							Log.i(TAG, "Received a SIGNAL");
							try{
								DbusMessage dbusmessage = new DbusMessage(message.getData());
								Log.d(TAG, dbusmessage.getArguments().toString());
							}
							catch(RuntimeException ex)
							{
								Log.e(TAG, "", ex);
							}
						} else if(message.getType() == MessageType.DBUS)
						{
							Log.i(TAG, "Received a DBUS response");
							try{
								DbusMessage dbusmessage = new DbusMessage(message.getData());
								Log.d(TAG, dbusmessage.getArguments().toString());
							}
							catch(RuntimeException ex)
							{
								Log.w(TAG, "Cant read this DBUS response yet.");
							}
						} else
						{
							Log.w(TAG, "Received unknown type: "+message.getType().ordinal());
						}
						
					}
			}
			} catch (Exception e)
			{
				Log.e(TAG, "Reader thread has stopped", e);
			} finally
			{
				try {
					inputStream.close();
				} catch (IOException e) {
					Log.e(TAG, "", e);
				}
			}
		}
	}
	
	public boolean disconnected()
	{
		return connection.disconnected();
	}

}