package nl.ict.aapbridge.dbus.message.types;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import nl.ict.aapbridge.dbus.message.DbusContainerType;
import nl.ict.aapbridge.dbus.message.DbusTypeParser;
import nl.ict.aapbridge.dbus.message.DbusTypeParser.DbusExtractor;
import static nl.ict.aapbridge.dbus.message.DbusTypeParser.align;

public class DbusString implements DbusContainerType{
	
	String string;
	
	private static class Extractor implements DbusExtractor
	{
		@Override
		public char getSupportedToplevelType() {
			return 's';
		}

		@Override
		public Object parse(String signature, ByteBuffer bb) {
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
	}
	
	static {
		DbusTypeParser.registerExtractor(new Extractor());
	}
	
	public static final Charset dbusStringEncoding = Charset.forName("UTF-8");
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
