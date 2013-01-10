package nl.ict.aapbridge.dbus;


import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import android.os.Message;

import nl.ict.aapbridge.bridge.AccessoryBridge;
import nl.ict.aapbridge.bridge.AccessoryBridge.Port;
import nl.ict.aapbridge.bridge.AccessoryBridge.Service;
import nl.ict.aapbridge.dbus.message.DbusMessage;
import nl.ict.aapbridge.dbus.message.DbusTypeParser;

/**
 * 
 * @author jurgen
 *
 */
public class Dbus implements Service{
	
	private static final Charset utf8 = Charset.forName("UTF-8");
	
	private short receiveLength = 0;
	
	private int call_id = 0;
	private int return_id = 0;
	
	private final ByteBuffer receiveBuffer = ByteBuffer.allocate(8000);
	private final ByteBuffer sendBuffer = ByteBuffer.allocate(8000);
	private final DbusHandler handler;
	private final Port port;
	
	public Dbus(DbusHandler handler, AccessoryBridge bridge) throws IOException {
		this.handler = handler;
		this.port = bridge.requestService((byte)2, this);
		sendBuffer.order(ByteOrder.LITTLE_ENDIAN);
	}
	
	/**
	 * Performs a remote dbus call. The dbus call will be performed on the session bus
	 * or system bus depending on the accessory's implementation.
	 * 
	 * Throws a BufferOverflowException if the combined arguments byte length exceeds the
	 * internal sendBuffer size.
	 * 
	 * @param busname
	 * @param objectpath
	 * @param interfaceName
	 * @param functionName
	 * @param arguments
	 * @return Unique id for this request. This value will be the same as Message.arg1 in the DbusHandler message handler for the return value.
	 * @throws IOException
	 * @throws BufferOverflowException
	 */
	public synchronized int methodCall(
			String busname,
			String objectpath,
			String interfaceName,
			String functionName,
			Object... arguments) throws IOException
	{
		sendBuffer.clear();
		sendBuffer.put(busname.getBytes(utf8));
		sendBuffer.put((byte)0);
		sendBuffer.put(objectpath.getBytes(utf8));
		sendBuffer.put((byte)0);
		sendBuffer.put(interfaceName.getBytes(utf8));
		sendBuffer.put((byte)0);
		sendBuffer.put(functionName.getBytes(utf8));
		sendBuffer.put((byte)0);
		
		sendBuffer.putInt(arguments.length);
		for(Object argument : arguments)
		{
			DbusTypeParser.serialise(argument,sendBuffer);
		}
		
		sendBuffer.flip();
		while(sendBuffer.hasRemaining())
			port.write(sendBuffer);
		
		return call_id++;
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
					Message.obtain(handler, DbusHandler.MessageTypes.DbusMethods.ordinal(), return_id++, 0, dbusMessage);
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
}
