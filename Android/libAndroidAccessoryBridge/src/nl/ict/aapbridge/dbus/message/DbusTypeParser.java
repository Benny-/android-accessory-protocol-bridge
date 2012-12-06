package nl.ict.aapbridge.dbus.message;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Set;

import nl.ict.aapbridge.dbus.message.DbusTypeParser.DbusExtractor;
import nl.ict.aapbridge.dbus.message.types.DbusArray;
import nl.ict.aapbridge.dbus.message.types.DbusByte;
import nl.ict.aapbridge.dbus.message.types.DbusDictEntry;
import nl.ict.aapbridge.dbus.message.types.DbusDouble;
import nl.ict.aapbridge.dbus.message.types.DbusInt16;
import nl.ict.aapbridge.dbus.message.types.DbusInt32;
import nl.ict.aapbridge.dbus.message.types.DbusObjectPath;
import nl.ict.aapbridge.dbus.message.types.DbusSignature;
import nl.ict.aapbridge.dbus.message.types.DbusString;
import nl.ict.aapbridge.dbus.message.types.DbusStruct;
import nl.ict.aapbridge.dbus.message.types.DbusVariant;

public class DbusTypeParser {
	
	/**
	 * Align the byte buffer to specified alignment
	 * 
	 * http://en.wikipedia.org/wiki/Data_structure_alignment#Computing_padding
	 * 
	 * @param bb
	 * @param alignment
	 */
	public static void align(ByteBuffer bb, int alignment)
	{
		int padding = (alignment - (bb.position() % alignment)) % alignment;
		bb.position(bb.position() + padding);
	}
	
	/**
	 * A DbusExtractor is something which can convert a bytestream into a DbusType object or native java object.
	 *
	 */
	public static interface DbusExtractor {
		abstract char getSupportedToplevelType();
		abstract Object parse(String signature, ByteBuffer bb);
	}
	
	/**
	 * DbusSerialiser is the opposite of a DbusExtractor and converts java types to a byte array.
	 *
	 */
	public static interface DbusSerialiser {
		abstract Class getSupportedJavaType();
		abstract byte[] serialise(Object object);
	}
	
	private static final HashMap<Character, DbusExtractor> extractors = new HashMap<Character, DbusExtractor>();
	private static final HashMap<Class, DbusSerialiser> serialisers = new HashMap<Class, DbusTypeParser.DbusSerialiser>();
	
	public static void registerExtractor(DbusExtractor extractor) {
		extractors.put(extractor.getSupportedToplevelType(), extractor);
	}
	
	public static void registerSerialiser(DbusSerialiser dbusSerialiser) {
		serialisers.put(dbusSerialiser.getSupportedJavaType(), dbusSerialiser);
	}
	
	public static Object extract(String signature, ByteBuffer bb)
	{
		DbusExtractor extractor = extractors.get(signature.charAt(0));
		if(extractor == null)
		{
			throw new RuntimeException("Could not find a extractor for signature "+signature.charAt(0));
		}
		return extractor.parse(signature, bb);
	}
	
	private DbusTypeParser() {
		// Constructor not used.
	}
	
	static {
		// Load all class files.
		try {
			Class.forName(DbusArray.class.getCanonicalName());
			Class.forName(DbusByte.class.getCanonicalName());
			Class.forName(DbusDictEntry.class.getCanonicalName());
			Class.forName(DbusDouble.class.getCanonicalName());
			Class.forName(DbusInt16.class.getCanonicalName());
			Class.forName(DbusInt32.class.getCanonicalName());
			Class.forName(DbusObjectPath.class.getCanonicalName());
			Class.forName(DbusSignature.class.getCanonicalName());
			Class.forName(DbusString.class.getCanonicalName());
			Class.forName(DbusStruct.class.getCanonicalName());
			Class.forName(DbusVariant.class.getCanonicalName());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
