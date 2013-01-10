package nl.ict.aapbridge.dbus;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import nl.ict.aapbridge.bridge.AccessoryBridge;
import nl.ict.aapbridge.bridge.AccessoryMessage.MessageType;

public class DbusSignals {
	public static final Charset utf8 = Charset.forName("UTF-8");
	
	private final AccessoryBridge bridge;
	
	public DbusSignals(AccessoryBridge bridge) {
		this.bridge = bridge;
	}
	
	public void addWatch(
			String busname,
			String objectpath,
			String interfaceName,
			String memberName) throws Exception
	{
		ByteBuffer bb = ByteBuffer.allocate(1024);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		
		bb.put((byte)1); // First byte is if we wish to register or unregister the watch.
		
		bb.put(busname.getBytes(utf8));
		bb.put((byte)0);
		bb.put(objectpath.getBytes(utf8));
		bb.put((byte)0);
		bb.put(interfaceName.getBytes(utf8));
		bb.put((byte)0);
		bb.put(memberName.getBytes(utf8));
		bb.put((byte)0);
		
		bridge.Write(
				bb.array(),
				0,
				MessageType.SIGNAL);
	}
	
	public void removeWatch(
			String busname,
			String objectpath,
			String interfaceName,
			String memberName) throws Exception
	{
		ByteBuffer bb = ByteBuffer.allocate(1024);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		
		bb.put((byte)0); // First byte is if we wish to register or unregister the watch.
		
		bb.put(busname.getBytes(utf8));
		bb.put((byte)0);
		bb.put(objectpath.getBytes(utf8));
		bb.put((byte)0);
		bb.put(interfaceName.getBytes(utf8));
		bb.put((byte)0);
		bb.put(memberName.getBytes(utf8));
		bb.put((byte)0);
		
		bridge.Write(
				bb.array(),
				0,
				MessageType.SIGNAL);
	}
}
