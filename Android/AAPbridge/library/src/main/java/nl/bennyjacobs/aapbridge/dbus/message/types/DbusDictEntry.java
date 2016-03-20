package nl.bennyjacobs.aapbridge.dbus.message.types;

import java.nio.ByteBuffer;

import nl.bennyjacobs.aapbridge.dbus.message.DbusContainerType;
import nl.bennyjacobs.aapbridge.dbus.message.DbusTypeParser;
import nl.bennyjacobs.aapbridge.dbus.message.DbusTypeParser.DbusExtractor;
import static nl.bennyjacobs.aapbridge.dbus.message.DbusTypeParser.align;

public class DbusDictEntry implements DbusContainerType{
	
	String signature;
	
	Object key;
	Object value;
	
	private static class Extractor implements DbusExtractor
	{
		@Override
		public char getSupportedToplevelType() {
			return '{';
		}

		@Override
		public Object parse(String signature, ByteBuffer bb) {
			return new DbusDictEntry(signature,bb);
		}
	}
	
	static {
		DbusTypeParser.registerExtractor(new Extractor());
	}
	
	public DbusDictEntry(String signature, ByteBuffer bb) {
		align(bb, 8);
		this.signature = signature;
		key = DbusTypeParser.extract(signature.substring(1), bb);
		value = DbusTypeParser.extract(signature.substring(2), bb);
	}

	@Override
	public String getSignature() {
		return signature;
	}
}
