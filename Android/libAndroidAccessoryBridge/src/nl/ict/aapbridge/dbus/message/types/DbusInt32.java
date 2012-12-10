package nl.ict.aapbridge.dbus.message.types;

import static nl.ict.aapbridge.dbus.message.DbusTypeParser.align;

import java.nio.ByteBuffer;

import nl.ict.aapbridge.dbus.message.DbusTypeParser;
import nl.ict.aapbridge.dbus.message.DbusTypeParser.DbusExtractor;
import nl.ict.aapbridge.dbus.message.DbusTypeParser.DbusSerializer;

public class DbusInt32 {
	private static class Extractor implements DbusExtractor
	{
		@Override
		public char getSupportedToplevelType() {
			return 'i';
		}

		@Override
		public Object parse(String signature, ByteBuffer bb) {
			align(bb, 4);
			return bb.getInt();
		}
	}
	
	private static class Serializer implements DbusSerializer
	{

		@Override
		public Class getSupportedJavaType() {
			return Integer.class;
		}

		@Override
		public void serialize(Object object, ByteBuffer bb) {
			bb.put((byte)'i');
			bb.putInt((Integer) object);
		}
	}
	
	static {
		DbusTypeParser.registerExtractor(new Extractor());
		DbusTypeParser.registerSerialiser(new Serializer());
	}
	
	private DbusInt32() {
		// This class only contains static functions.
	}

}
