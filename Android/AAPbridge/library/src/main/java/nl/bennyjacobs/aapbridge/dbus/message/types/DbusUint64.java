package nl.bennyjacobs.aapbridge.dbus.message.types;

import static nl.bennyjacobs.aapbridge.dbus.message.DbusTypeParser.align;

import java.nio.ByteBuffer;

import nl.bennyjacobs.aapbridge.dbus.message.DbusTypeParser;
import nl.bennyjacobs.aapbridge.dbus.message.DbusTypeParser.DbusExtractor;
import nl.bennyjacobs.aapbridge.dbus.message.DbusTypeParser.DbusSerializer;

public class DbusUint64 {
	private static class Extractor implements DbusExtractor
	{
		@Override
		public char getSupportedToplevelType() {
			return 't';
		}

		@Override
		public Object parse(String signature, ByteBuffer bb) {
			align(bb, 8);
			return bb.getLong();
		}
	}
	
	static {
		DbusTypeParser.registerExtractor(new Extractor());
	}
	
	private DbusUint64() {
		// This class only contains static functions.
	}
}
