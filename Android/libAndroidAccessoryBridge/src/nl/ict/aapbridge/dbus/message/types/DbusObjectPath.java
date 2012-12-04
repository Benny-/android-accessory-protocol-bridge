package nl.ict.aapbridge.dbus.message.types;

import static nl.ict.aapbridge.dbus.message.DbusTypeParser.align;

import java.nio.ByteBuffer;

import nl.ict.aapbridge.dbus.message.DbusContainerType;
import nl.ict.aapbridge.dbus.message.DbusTypeParser;
import nl.ict.aapbridge.dbus.message.DbusTypeParser.DbusExtractor;

public class DbusObjectPath implements DbusContainerType{
	
	private String string;
	
	private static class Extractor implements DbusExtractor
	{
		@Override
		public char getSupportedToplevelType() {
			return 'o';
		}

		@Override
		public Object parse(String signature, ByteBuffer bb) {
			return new DbusObjectPath(signature,bb);
		}
	}
	
	static {
		DbusTypeParser.registerExtractor(new Extractor());
	}
	
	public DbusObjectPath(String signature, ByteBuffer bb) {
		align(bb, 4);
		int length = bb.getInt();
		StringBuilder sb = new StringBuilder();
		while(length > 0)
		{
			sb.append((char)bb.get());
			length--;
		}
		bb.get(); // D-bus specs append a null byte.
		string = sb.toString();
	}

	@Override
	public String getSignature() {
		return "o";
	}
	
	@Override
	public String toString() {
		return "Objectpath " +this.string;
	}
}