package nl.ict.aapbridge.dbus;

import java.io.Closeable;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import android.os.Message;
import android.os.RemoteException;

import nl.ict.aapbridge.bridge.AccessoryBridge;
import nl.ict.aapbridge.bridge.ServiceRequestException;
import nl.ict.aapbridge.bridge.AccessoryBridge.Port;
import nl.ict.aapbridge.bridge.AccessoryBridge.BridgeService;
import nl.ict.aapbridge.dbus.message.DbusMessage;

/**
 * Functionality for communicating to a remote d-bus (signals only).
 * 
 * DbusSignals object will send all signals to a handler.
 * 
 * You should however always keep a reference to the DbusSignals
 * around to call the {@link #close()} method once you are done receiving.
 * 
 * @see Dbus
 */
public class DbusSignals implements BridgeService, Closeable {
	public static final Charset utf8 = Charset.forName("UTF-8");
	
	private short receiveLength = 0;
	
	private final Port port;
	private final DbusHandler handler;
	private final ByteBuffer receiveBuffer = ByteBuffer.allocate(8000);
	
	/**
	 * Create a Dbus signal watch on the remote device.
	 * 
	 * Dbus signals will start pouring to the handler once this object is created.
	 * 
	 * You must call {@link #close()} once you are done receiving dbus signals.
	 * 
	 * @param dbusHandler
	 * @param bridge
	 * @param busname
	 * @param objectpath
	 * @param interfaceName
	 * @param memberName
	 * 
	 * @throws IOException
	 * @throws BufferOverflowException
	 */
	public DbusSignals(
			DbusHandler dbusHandler,
			AccessoryBridge bridge,
			String busname,
			String objectpath,
			String interfaceName,
			String memberName) throws IOException, ServiceRequestException
	{
		receiveBuffer.limit(2);
		
		ByteBuffer bb = ByteBuffer.allocate(8000);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		
		bb.put(busname.getBytes(utf8));
		bb.put((byte)0);
		bb.put(objectpath.getBytes(utf8));
		bb.put((byte)0);
		bb.put(interfaceName.getBytes(utf8));
		bb.put((byte)0);
		bb.put(memberName.getBytes(utf8));
		bb.put((byte)0);
		
		this.handler = dbusHandler;
		this.port = bridge.requestService((byte)3, bb, this);
	}

	@Override
	public void onDataReady(int length) throws IOException {
		while(length > 0)
		{
			length -= port.read(receiveBuffer);
			if(!receiveBuffer.hasRemaining())
			{
				if(receiveLength == 0)
				{
					receiveBuffer.rewind();
					receiveBuffer.order(ByteOrder.LITTLE_ENDIAN);
					receiveLength = receiveBuffer.getShort();
					receiveBuffer.rewind();
					receiveBuffer.limit(receiveLength);
				}
				else
				{
					receiveBuffer.rewind();
					DbusMessage dbusMessage = new DbusMessage(receiveBuffer);
					Message.obtain(handler, DbusHandler.MessageTypes.DbusSignals.ordinal(), dbusMessage);
					receiveBuffer.rewind();
					receiveBuffer.limit(2);
					receiveLength = 0;
				}
			}
		}
	}

	@Override
	public Port getPort() {
		return port;
	}

	@Override
	public void close() throws IOException {
		port.close();
	}
}
