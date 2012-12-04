package nl.ict.aapbridge.dbus.message.types;

import static nl.ict.aapbridge.dbus.message.DbusTypeParser.align;

import java.nio.ByteBuffer;

import nl.ict.aapbridge.dbus.message.DbusType;
import nl.ict.aapbridge.dbus.message.DbusTypeParser;
import nl.ict.aapbridge.dbus.message.DbusTypeParser.DbusExtractor;

public class DbusDouble {
	
	private static class Extractor implements DbusExtractor
	{
		@Override
		public char getSupportedToplevelType() {
			return 'd';
		}

		@Override
		public Object parse(String signature, ByteBuffer bb) {
			align(bb, 8);
			return bb.getDouble();
		}
	}
	
	static {
		DbusTypeParser.registerExtractor(new Extractor());
	}
}
