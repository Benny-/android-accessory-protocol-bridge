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
 * @see DbusMethods
 * @see DbusHandler
 */
public class DbusSignals implements BridgeService, Closeable {
	public static final Charset utf8 = Charset.forName("UTF-8");
	
	private final Port port;
	private final DbusHandler handler;
	private final ByteBuffer receiveBuffer = ByteBuffer.allocate(4000);
	
	/**
	 * <p>Create a Dbus signal watch on the remote device.</p>
	 * 
	 * <p>Dbus signals will start pouring to the handler once this object is created.</p>
	 * 
	 * <p>You must call {@link #close()} once you are done receiving dbus signals.</p>
	 * 
	 * <p>You can match multiple different signals by passing null for some matching rules.</p>
	 * 
	 * @param dbushandler May not be null
	 * @param bridge May not be null
	 * @param busname Null is allowed. Empty string functions the same as null.
	 * @param objectpath Null is allowed. Empty string functions the same as null.
	 * @param interfaceName Null is allowed. Empty string functions the same as null.
	 * @param signalName Null is allowed. Empty string functions the same as null.
	 * 
	 * @throws IOException If connection to host is lost
	 * @throws BufferOverflowException If the combined byte size of all arguments (except dbushandler) exceed the internal send buffer. The internal send buffer is 3000 bytes.
	 * @throws NullPointerException If dbushandler, busname, objectpath, interfaceName or memberName are null
	 */
	public DbusSignals(
			AccessoryBridge bridge,
			DbusHandler dbushandler,
			String busname,
			String objectpath,
			String interfaceName,
			String signalName) throws IOException, ServiceRequestException
	{
		if(bridge == null)
			throw new NullPointerException("bridge may not be null");
		
		if(busname == null)
			busname = "";
		
		if(objectpath == null)
			objectpath = "";
		
		if(interfaceName == null)
			interfaceName = "";
		
		if(signalName == null)
			signalName = "";
		
		if(dbushandler == null)
			throw new NullPointerException("Handler may not be null");
		
		ByteBuffer bb = ByteBuffer.allocate(3000);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		
		bb.clear();
		bb.put(busname.getBytes(utf8));
		bb.put((byte)0);
		bb.put(objectpath.getBytes(utf8));
		bb.put((byte)0);
		bb.put(interfaceName.getBytes(utf8));
		bb.put((byte)0);
		bb.put(signalName.getBytes(utf8));
		bb.put((byte)0);
		bb.flip();
		
		this.handler = dbushandler;
		this.port = bridge.requestService((byte)3, bb, this);
	}

	@Override
	public void onDataReady(int length) throws IOException {
		receiveBuffer.clear();
		receiveBuffer.limit(length);
		port.readAll(receiveBuffer);
		receiveBuffer.flip();
		DbusMessage dbusMessage = new DbusMessage(receiveBuffer);
		Message.obtain(handler, DbusHandler.MessageTypes.DbusSignals.ordinal(), dbusMessage).sendToTarget();
	}

	@Override
	public Port getPort() {
		return port;
	}

	@Override
	public void close() throws IOException {
		port.close();
		port.eof();
	}

	@Override
	public void onEof() {
		// TODO Auto-generated method stub
	}
}
