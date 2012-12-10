package nl.ict.aapbridge.dbus.message.types;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import nl.ict.aapbridge.dbus.message.DbusContainerType;
import nl.ict.aapbridge.dbus.message.DbusTypeParser;
import nl.ict.aapbridge.dbus.message.DbusTypeParser.DbusExtractor;
import nl.ict.aapbridge.dbus.message.DbusTypeParser.DbusSerializer;
import static nl.ict.aapbridge.dbus.message.DbusTypeParser.align;

public class DbusString implements DbusContainerType{
	
	public static final Charset dbusStringEncoding = Charset.forName("UTF-8");
	
	String string;
	
	public static Object parse(String signature, ByteBuffer bb){
		align(bb, 4);
		int length = bb.getInt();
		StringBuilder sb = new StringBuilder();
		while(length > 0)
		{
			sb.append((char)bb.get()); // TODO: Properly decode the utf-8.
			length--;
		}
		bb.get(); // D-bus specs append a null byte.
		return sb.toString();
	}
	
	private static class Extractor implements DbusExtractor
	{
		@Override
		public char getSupportedToplevelType() {
			return 's';
		}		
		
		@Override
		public Object parse(String signature, ByteBuffer bb) {
			return DbusString.parse(signature, bb);
		}
	}
	
	private static class Serializer implements DbusSerializer
	{

		@Override
		public Class getSupportedJavaType() {
			return String.class;
		}

		@Override
		public void serialize(Object object, ByteBuffer bb) {
			bb.put((byte)'s');
			bb.put( ((String) object).getBytes(dbusStringEncoding) );
			bb.put((byte) 0);
		}
	}
	
	static {
		DbusTypeParser.registerExtractor(new Extractor());
		DbusTypeParser.registerSerialiser(new Serializer());
	}
	
	private DbusString(ByteBuffer bb) {
		// DbusString maps to java String's so no DbusString object will be created. Hence the private constructor.
	}

	@Override
	public String getSignature() {
		return "s";
	}
	
	@Override
	public String toString() {
		return "String "+string;
	}
}
