package nl.bennyjacobs.aapbridge.dbus.message.types;

import static nl.bennyjacobs.aapbridge.dbus.message.DbusTypeParser.align;

import java.nio.ByteBuffer;

import nl.bennyjacobs.aapbridge.dbus.message.DbusTypeParser;
import nl.bennyjacobs.aapbridge.dbus.message.DbusTypeParser.DbusExtractor;

public class DbusUint16 {
	private static class Extractor implements DbusExtractor
	{
		@Override
		public char getSupportedToplevelType() {
			return 'q';
		}

		@Override
		public Object parse(String signature, ByteBuffer bb) {
			align(bb, 2);
			return bb.getShort();
		}
	}
	
	static {
		DbusTypeParser.registerExtractor(new Extractor());
	}
	
	private DbusUint16() {
		// This class only contains static functions.
	}
}
