package nl.ict.aapbridge.dbus.message.types;

import static nl.ict.aapbridge.dbus.message.DbusTypeParser.align;

import java.nio.ByteBuffer;

import nl.ict.aapbridge.dbus.message.DbusTypeParser;
import nl.ict.aapbridge.dbus.message.DbusTypeParser.DbusExtractor;
import nl.ict.aapbridge.dbus.message.DbusTypeParser.DbusSerializer;

public class DbusInt16 {
	private static class Extractor implements DbusExtractor
	{
		@Override
		public char getSupportedToplevelType() {
			return 'n';
		}

		@Override
		public Object parse(String signature, ByteBuffer bb) {
			align(bb, 2);
			return bb.getShort();
		}
	}
	
	private static class Serializer implements DbusSerializer
	{

		@Override
		public Class getSupportedJavaType() {
			return Short.class;
		}

		@Override
		public void serialize(Object object, ByteBuffer bb) {
			bb.put((byte)'n');
			bb.putShort((Short) object);
		}
	}
	
	static {
		DbusTypeParser.registerExtractor(new Extractor());
		DbusTypeParser.registerSerialiser(new Serializer());
	}
	
	private DbusInt16() {
		// This class only contains static functions.
	}

}
