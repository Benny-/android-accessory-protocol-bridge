package nl.ict.aapbridge.dbus.message.types;

import java.nio.ByteBuffer;

import nl.ict.aapbridge.dbus.message.DbusType;
import nl.ict.aapbridge.dbus.message.DbusTypeParser;
import nl.ict.aapbridge.dbus.message.DbusTypeParser.DbusExtractor;
import static nl.ict.aapbridge.dbus.message.DbusTypeParser.align;

public class DbusByte implements DbusType{
	private static class Extractor implements DbusExtractor
	{
		@Override
		public char getSupportedToplevelType() {
			return 'y';
		}

		@Override
		public Object parse(String signature, ByteBuffer bb) {
			return new Byte(bb.get());
		}
	}
	
	static {
		DbusTypeParser.registerExtractor(new Extractor());
	}
	
	@Override
	public String getSignature() {
		return "y";
	}
}
