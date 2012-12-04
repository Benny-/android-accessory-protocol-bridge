package nl.ict.aapbridge.dbus.message.types;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import nl.ict.aapbridge.dbus.message.DbusContainerType;
import nl.ict.aapbridge.dbus.message.DbusTypeParser;
import nl.ict.aapbridge.dbus.message.DbusTypeParser.DbusExtractor;
import static nl.ict.aapbridge.dbus.message.DbusTypeParser.align;

public class DbusStruct implements DbusContainerType{
	
	private String signature;
	Object tuple[];
	
	private static class Extractor implements DbusExtractor
	{
		@Override
		public char getSupportedToplevelType() {
			return '(';
		}

		@Override
		public Object parse(String signature, ByteBuffer bb) {
			return new DbusStruct(signature,bb);
		}
	}
	
	static {
		DbusTypeParser.registerExtractor(new Extractor());
	}
	
	public DbusStruct(String signature, ByteBuffer bb) {
		align(bb, 8); // Warning: Specs say it must be aligned to 8 byte. But this does not appear to be so.
		
		ArrayList list = new ArrayList();
		int i = 0;
		int depth = 0;
		do{
			if(signature.charAt(i) == '(' )
				depth++;
			else if(signature.charAt(i) == ')' )
				depth--;
			else {
				list.add(DbusTypeParser.extract(signature.substring(i), bb));
			}
			i++;
		} while(depth > 0);
		this.signature = signature.substring(0, i);
		this.tuple = list.toArray();
	}

	@Override
	public String getSignature() {
		return this.signature;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Struct"+this.signature+" ");
		for(Object o : this.tuple)
		{
			sb.append(' ');
			sb.append(o);
			sb.append(' ');
		}
		return sb.toString();
	}
	
	public Object[] getContent()
	{
		return tuple;
	}
}