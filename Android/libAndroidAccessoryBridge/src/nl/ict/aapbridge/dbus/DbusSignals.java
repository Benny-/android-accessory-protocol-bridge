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

public class DbusSignals implements Service{
	public static final Charset utf8 = Charset.forName("UTF-8");
	
	private short receiveLength = 0;
	
	private final Port port;
	private final DbusHandler handler;
	private final ByteBuffer receiveBuffer = ByteBuffer.allocate(8000);
	
	/**
	 * 
	 * @param dbusHandler
	 * @param bridge
	 * @param busname
	 * @param objectpath
	 * @param interfaceName
	 * @param memberName
	 * @throws IOException
	 * @throws BufferOverflowException
	 */
	public DbusSignals(
			DbusHandler dbusHandler,
			AccessoryBridge bridge,
			String busname,
			String objectpath,
			String interfaceName,
			String memberName) throws IOException
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
}
