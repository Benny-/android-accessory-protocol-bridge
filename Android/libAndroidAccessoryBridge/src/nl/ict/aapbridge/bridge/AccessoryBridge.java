package nl.ict.aapbridge.bridge;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;

import static nl.ict.aapbridge.TAG.TAG;
import nl.ict.aapbridge.SystemHolder;
import nl.ict.aapbridge.aap.AccessoryConnection;
import nl.ict.aapbridge.bridge.AccessoryMessage.MessageType;

import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;


/**
 * The class that handles the communication with Android Accessory
 * @author jurgen
 * @todo rewrite to singleton
 */
public class AccessoryBridge
{
	private AccessoryConnection connection;
	private static OutputStream mFops;
	public static Queue<AccessoryMessage> messages;
	public static Handler handler = new Handler();
	
	public AccessoryBridge(AccessoryConnection connection) throws IOException {
		this.connection = connection;
		AccessoryBridge.mFops = this.connection.getOutputStream();
		new UsbListener(this.connection.getInputStream()).start();
		messages = new LinkedList<AccessoryMessage>();
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
	public static void Write(byte[] message, int id, AccessoryMessage.MessageType type) throws Exception {

		byte[] buffer = MessageHandler.encode(message, id, type); //encode message
		
		if(buffer != null) { //if message is null, there's a segmented message. 
			mFops.write(buffer);
		}  else {
			//TODO handle segmented message
		}
	}
	
	/**
	 * Thread who receive the data from Android Accessory bus, decodes it and its to the queue
	 * @author Jurgen
	 *
	 */
	public class UsbListener extends Thread{
		private InputStream mFips;
		
		public UsbListener(InputStream inputStream) {
			this.mFips = inputStream;
		} 
		
		public void run() {
			byte[] buffer = new byte[Config.MESSAGEMAX];
			int ret = 1;
			try {
			while(ret >= 0)
			{
					Log.d(TAG, "Reading mFips");
					ret = mFips.read(buffer);
					
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
						Log.d(TAG, "Message received: "+message);
						
						if(message.getType() == MessageType.KEEPALIVE && message.toString().length() > 1)
						{
							handler.post(new Runnable() {
								public void run() {
									Toast.makeText(SystemHolder.getContext(), "Received a "+message.getType().toString()+": "+message, Toast.LENGTH_SHORT).show();
								}
							});
						}
						
						if(message.getType() == MessageType.SIGNAL)
						{
							ByteBuffer dbusSignal = ByteBuffer.wrap(message.getData());
							ByteOrder byteOrder;
							if (dbusSignal.get() == 'l')
							{
								byteOrder = ByteOrder.LITTLE_ENDIAN;
							}
							else if (dbusSignal.get() == 'B')
							{
								byteOrder = ByteOrder.BIG_ENDIAN;
							} else
							{
								throw new Error("Unknown endian");
							}
							dbusSignal.order(byteOrder);
							
							dbusSignal.get(); // Message type
							dbusSignal.get(); // flags
							dbusSignal.get(); // Major protocol version
							int size = dbusSignal.getInt(); // Length in bytes of the message body. XXX: Conversion from unsigned to signed.
							dbusSignal.getInt(); // The serial of this message, used as a cookie by the sender to identify the reply corresponding to this request.
							
							CharSequence signature = null;
							
							byte headerFieldsSize = dbusSignal.get();
							while(dbusSignal.position() < size)
							{
								byte headertype = dbusSignal.get();
								if(headertype > 9)
									throw new Error("Unknown header field");
							}
							//dbusSignal.asCharBuffer().subSequence(0, 6);
							
							final CharSequence finalSignature = signature;
							handler.post(new Runnable() {
								public void run() {
									Toast.makeText(SystemHolder.getContext(), "Signature: "+finalSignature+": "+message, Toast.LENGTH_SHORT).show();
								}
							});
						}
					}
			}
			} catch (Exception e)
			{
				Log.e(TAG, "Reader thread has stopped", e);
			} finally
			{
				try {
					mFips.close();
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