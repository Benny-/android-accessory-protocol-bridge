package nl.bennyjacobs.aapbridge.dbus.message.types;

import static nl.bennyjacobs.aapbridge.dbus.message.DbusTypeParser.align;

import java.nio.ByteBuffer;

import nl.bennyjacobs.aapbridge.dbus.message.DbusContainerType;
import nl.bennyjacobs.aapbridge.dbus.message.DbusTypeParser;
import nl.bennyjacobs.aapbridge.dbus.message.DbusTypeParser.DbusExtractor;

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
		string = (String) DbusString.parse(signature, bb);
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