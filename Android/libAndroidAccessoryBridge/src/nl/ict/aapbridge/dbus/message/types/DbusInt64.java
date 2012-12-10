package nl.ict.aapbridge.dbus.message.types;

import static nl.ict.aapbridge.dbus.message.DbusTypeParser.align;

import java.nio.ByteBuffer;

import nl.ict.aapbridge.dbus.message.DbusTypeParser;
import nl.ict.aapbridge.dbus.message.DbusTypeParser.DbusExtractor;
import nl.ict.aapbridge.dbus.message.DbusTypeParser.DbusSerializer;

public class DbusInt64 {
	private static class Extractor implements DbusExtractor
	{
		@Override
		public char getSupportedToplevelType() {
			return 'x';
		}

		@Override
		public Object parse(String signature, ByteBuffer bb) {
			align(bb, 8);
			return bb.getLong();
		}
	}
	
	private static class Serializer implements DbusSerializer
	{

		@Override
		public Class getSupportedJavaType() {
			return Long.class;
		}

		@Override
		public void serialize(Object object, ByteBuffer bb) {
			bb.put((byte)'x');
			bb.put((byte) 0);
			bb.putLong((Long) object);
		}
	}
	
	static {
		DbusTypeParser.registerExtractor(new Extractor());
		DbusTypeParser.registerSerialiser(new Serializer());
	}
	
	private DbusInt64() {
		// This class only contains static functions.
	}

}
