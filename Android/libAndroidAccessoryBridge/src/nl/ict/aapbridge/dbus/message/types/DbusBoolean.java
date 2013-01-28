package nl.ict.aapbridge.dbus.message.types;

import static nl.ict.aapbridge.dbus.message.DbusTypeParser.align;

import java.nio.ByteBuffer;

import nl.ict.aapbridge.dbus.message.DbusType;
import nl.ict.aapbridge.dbus.message.DbusTypeParser;
import nl.ict.aapbridge.dbus.message.DbusTypeParser.DbusExtractor;
import nl.ict.aapbridge.dbus.message.DbusTypeParser.DbusSerializer;

public class DbusBoolean {
	
	private static class Extractor implements DbusExtractor
	{
		@Override
		public char getSupportedToplevelType() {
			return 'b';
		}

		@Override
		public Object parse(String signature, ByteBuffer bb) {
			align(bb, 4); // Yes. A dbus boolean is a 4 byte aligned to a 4 byte block.
			return bb.getInt()==0 ? false : true;
		}
	}
	
	private static class Serializer implements DbusSerializer
	{

		@Override
		public Class getSupportedJavaType() {
			return Boolean.class;
		}

		@Override
		public void serialize(Object object, ByteBuffer bb) {
			bb.put((byte)'b');
			bb.put((byte) (((Boolean) object) ? 1:0) );
		}
	}
	
	static {
		DbusTypeParser.registerExtractor(new Extractor());
		DbusTypeParser.registerSerialiser(new Serializer());
	}
	
	private DbusBoolean() {
		// This class only contains static functions.
	}
}
