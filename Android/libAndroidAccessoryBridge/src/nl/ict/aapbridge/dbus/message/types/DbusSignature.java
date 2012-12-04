package nl.ict.aapbridge.dbus.message.types;

import java.nio.ByteBuffer;

import nl.ict.aapbridge.dbus.message.DbusContainerType;
import nl.ict.aapbridge.dbus.message.DbusTypeParser;
import nl.ict.aapbridge.dbus.message.DbusTypeParser.DbusExtractor;
import static nl.ict.aapbridge.dbus.message.DbusTypeParser.align;

public class DbusSignature implements DbusContainerType{
	
	String signatureString;
	
	private static class Extractor implements DbusExtractor
	{
		@Override
		public char getSupportedToplevelType() {
			return 'g';
		}

		@Override
		public Object parse(String signature, ByteBuffer bb) {
			return new DbusSignature(bb);
		}
	}
	
	static {
		DbusTypeParser.registerExtractor(new Extractor());
	}
	
	public DbusSignature(ByteBuffer bb) {
		byte length = bb.get();
		StringBuilder sb = new StringBuilder();
		while(length > 0)
		{
			sb.append((char)bb.get());
			length--;
		}
		bb.get(); // D-bus specs append a null byte.
		signatureString = sb.toString();
	}

	@Override
	public String getSignature() {
		return "g";
	}
	
	@Override
	public String toString() {
		return signatureString;
	}

	public String getSignatureString() {
		return signatureString;
	}
}
