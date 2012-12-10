package nl.ict.aapbridge.dbus.message.types;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import android.util.Log;

import static nl.ict.aapbridge.TAG.TAG;
import nl.ict.aapbridge.dbus.message.DbusContainerType;
import nl.ict.aapbridge.dbus.message.DbusType;
import nl.ict.aapbridge.dbus.message.DbusTypeParser;
import nl.ict.aapbridge.dbus.message.DbusTypeParser.DbusExtractor;
import static nl.ict.aapbridge.dbus.message.DbusTypeParser.align;

public class DbusArray extends ArrayList<Object> implements DbusContainerType{
	
	private String signature;
	
	private static class Extractor implements DbusExtractor
	{
		@Override
		public char getSupportedToplevelType() {
			return 'a';
		}

		@Override
		public Object parse(String signature, ByteBuffer bb) {
			return new DbusArray(signature, bb);
		}
	}
	
	static {
		DbusTypeParser.registerExtractor(new Extractor());
	}
	
	public DbusArray(String signature, ByteBuffer bb) {
		//Log.v(TAG, "DbusArray ["+signature+"] offset: 0x"+Integer.toHexString(bb.position()));
		align(bb, 4);
		int length = bb.getInt();
		//Log.v(TAG, "DbusArray length: "+length);
		int end = bb.position() + length;
		while(bb.position() < end)
		{
			this.add(DbusTypeParser.extract(signature.substring(1), bb));
			//Log.v(TAG, "DbusArray got another element now at position 0x"+Integer.toHexString(bb.position()));
		}
		
		this.signature = signature.substring(1);
	}

	@Override
	public String getSignature() {
		return signature;
	}

}
