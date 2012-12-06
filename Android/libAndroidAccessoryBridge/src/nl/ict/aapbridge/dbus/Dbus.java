package nl.ict.aapbridge.dbus;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;

import nl.ict.aapbridge.bridge.AccessoryBridge;
import nl.ict.aapbridge.bridge.MessageHandler;
import nl.ict.aapbridge.bridge.AccessoryMessage.MessageType;
import nl.ict.aapbridge.helper.ExtByteArrayOutputStream;
import nl.ict.aapbridge.helper.IntegerHelper;


/**
 * 
 * @author jurgen
 *
 */

//@todo add broadcast listener
public class Dbus {
	
	public static final Charset utf8 = Charset.forName("UTF-8");
	
	public void methodCall(
			String busname,
			String objectpath,
			String interfaceName,
			String functionName,
			Object... arguments) throws Exception
	{  
		ByteBuffer bb = ByteBuffer.allocate(1024);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.put(busname.getBytes(utf8));
		bb.put((byte)0);
		bb.put(objectpath.getBytes(utf8));
		bb.put((byte)0);
		bb.put(interfaceName.getBytes(utf8));
		bb.put((byte)0);
		bb.put(functionName.getBytes(utf8));
		bb.put((byte)0);
		
		bb.putInt(arguments.length);
		for(Object argument : arguments)
		{
			// TODO: Convert java types to binary.
		}
		
		AccessoryBridge.Write(bb.array(), 0 ,MessageType.DBUS);
		
		// TODO: wait for reply
	}
}