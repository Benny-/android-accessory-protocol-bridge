package nl.bennyjacobs.aapbridge.dbus.message.types;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import android.util.Log;

import nl.bennyjacobs.aapbridge.dbus.message.DbusContainerType;
import nl.bennyjacobs.aapbridge.dbus.message.DbusTypeParser;
import nl.bennyjacobs.aapbridge.dbus.message.DbusTypeParser.DbusExtractor;
import static nl.bennyjacobs.aapbridge.dbus.message.DbusTypeParser.align;

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
		align(bb, 8);
		
		ArrayList<Object> list = new ArrayList<Object>();
		int i = 0;
		int depth = 0;
		try
		{
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
		}
		catch (Exception e)
		{
			Log.w(DbusStruct.class.getSimpleName(), "Internal error, returning incomplete struct");
		}
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