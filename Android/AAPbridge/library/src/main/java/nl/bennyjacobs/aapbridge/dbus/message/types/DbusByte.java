package nl.bennyjacobs.aapbridge.dbus.message.types;

import java.nio.ByteBuffer;

import nl.bennyjacobs.aapbridge.dbus.message.DbusType;
import nl.bennyjacobs.aapbridge.dbus.message.DbusTypeParser;
import nl.bennyjacobs.aapbridge.dbus.message.DbusTypeParser.DbusExtractor;
import nl.bennyjacobs.aapbridge.dbus.message.DbusTypeParser.DbusSerializer;
import static nl.bennyjacobs.aapbridge.dbus.message.DbusTypeParser.align;

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
	
	private static class Serializer implements DbusSerializer
	{

		@Override
		public Class getSupportedJavaType() {
			return Byte.class;
		}

		@Override
		public void serialize(Object object, ByteBuffer bb) {
			bb.put((byte)'y');
			bb.put((Byte) object);
		}
	}
	
	static {
		DbusTypeParser.registerExtractor(new Extractor());
		DbusTypeParser.registerSerialiser(new Serializer());
	}
	
	@Override
	public String getSignature() {
		return "y";
	}
}
