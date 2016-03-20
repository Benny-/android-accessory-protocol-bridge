package nl.bennyjacobs.aapbridge.dbus.message.types;

import java.nio.ByteBuffer;

import nl.bennyjacobs.aapbridge.dbus.message.DbusContainerType;
import nl.bennyjacobs.aapbridge.dbus.message.DbusType;
import nl.bennyjacobs.aapbridge.dbus.message.DbusTypeParser;
import nl.bennyjacobs.aapbridge.dbus.message.DbusTypeParser.DbusExtractor;
import static nl.bennyjacobs.aapbridge.dbus.message.DbusTypeParser.align;

public class DbusVariant implements DbusContainerType{
	
	private String embeddedSignature;
	private Object embeddedThing;
	
	private static class Extractor implements DbusExtractor
	{
		@Override
		public char getSupportedToplevelType() {
			return 'v';
		}

		@Override
		public Object parse(String signature, ByteBuffer bb) {
			return new DbusVariant(signature,bb);
		}
	}
	
	static {
		DbusTypeParser.registerExtractor(new Extractor());
	}
	
	public DbusVariant(String signature, ByteBuffer bb) {
		DbusSignature sig = new DbusSignature(bb);
		embeddedSignature = sig.getSignatureString();
		embeddedThing = DbusTypeParser.extract(sig.toString(), bb);
	}
	
	public Object getEmbeddedThing()
	{
		return embeddedThing;
	}

	@Override
	public String getSignature() {
		return embeddedSignature;
	}
	
	@Override
	public String toString() {
		return "Variant["+this.embeddedSignature+"] "+this.embeddedThing;
	}
}
