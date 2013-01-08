package nl.ict.aapbridge.dbus.message.types;

import static nl.ict.aapbridge.dbus.message.DbusTypeParser.align;

import java.nio.ByteBuffer;

import nl.ict.aapbridge.dbus.message.DbusTypeParser;
import nl.ict.aapbridge.dbus.message.DbusTypeParser.DbusExtractor;
import nl.ict.aapbridge.dbus.message.DbusTypeParser.DbusSerializer;

public class DbusUint32 {
	private static class Extractor implements DbusExtractor
	{
		@Override
		public char getSupportedToplevelType() {
			return 'u';
		}

		@Override
		public Object parse(String signature, ByteBuffer bb) {
			align(bb, 4);
			return bb.getInt();
		}
	}
	
	static {
		DbusTypeParser.registerExtractor(new Extractor());
	}
	
	private DbusUint32() {
		// This class only contains static functions.
	}
}
