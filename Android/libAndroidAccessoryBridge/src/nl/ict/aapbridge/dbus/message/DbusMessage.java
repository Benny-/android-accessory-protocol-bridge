package nl.ict.aapbridge.dbus.message;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import nl.ict.aapbridge.dbus.RemoteException;
import nl.ict.aapbridge.dbus.message.types.DbusArray;
import nl.ict.aapbridge.dbus.message.types.DbusObjectPath;
import nl.ict.aapbridge.dbus.message.types.DbusSignature;
import nl.ict.aapbridge.dbus.message.types.DbusStruct;
import nl.ict.aapbridge.dbus.message.types.DbusVariant;

/**
 * Represents a DbusMessage and may contain a array of d-bus values converted to java types.
 */
public class DbusMessage {
	
	private DbusObjectPath path; // Object path
	private String interfaceName;
	private String member;
	private String errorName;
	private int reply_serial;
	private String destination;
	private String sender;
	private DbusSignature Signature;
	private int unix_fds;
	private DbusStruct arguments;
	
	private void parseHeader(DbusArray arr)
	{
		for(Object o : arr)
		{
			DbusStruct struct = (DbusStruct) o;
			byte headertype = (Byte) struct.getContent()[0];
			DbusVariant variant = (DbusVariant) struct.getContent()[1];
			switch(headertype){
				case 1:
					this.path = (DbusObjectPath) variant.getEmbeddedThing();
					break;
				case 2:
					this.interfaceName = (String) variant.getEmbeddedThing();
					break;
				case 3:
					this.member = (String) variant.getEmbeddedThing();
					break;
				case 4:
					this.errorName = (String) variant.getEmbeddedThing();
					break;
				case 5:
					this.reply_serial = (Integer) variant.getEmbeddedThing();
					break;
				case 6:
					this.destination = (String) variant.getEmbeddedThing();
					break;
				case 7:
					this.sender = (String) variant.getEmbeddedThing();
					break;
				case 8:
					this.Signature = (DbusSignature) variant.getEmbeddedThing();
					break;
				case 9:
					this.unix_fds = (Integer) variant.getEmbeddedThing();
					break;
				default:
					System.err.println("Warning unknwon dbus message header "+headertype+" :"+variant);
					;
			}
		}
	}
	
	public DbusMessage(ByteBuffer bb) {
		ByteOrder endian = null;
		{
			byte endian_marshalled = bb.get();
			if(endian_marshalled == 'l')
			{
				endian = ByteOrder.LITTLE_ENDIAN;
			}
			else if(endian_marshalled == 'B')
			{
				endian = ByteOrder.BIG_ENDIAN;
			}
			else
			{
				throw new Error("Invalid endian: "+endian_marshalled);
			}
			bb.order(endian);
		}
		
		byte msg_type = bb.get();
		byte flags = bb.get();
		byte protocol_version = bb.get();
		int msg_size = bb.getInt();
		int serial = bb.getInt();
		
		DbusArray arr = new DbusArray("a(yv)",bb);
		parseHeader(arr);
		arguments = new DbusStruct("("+Signature.getSignatureString()+")",bb);
	}
	
	/**
	 * <p>
	 * The d-bus data are converted to java data types. This is done arcording to the following table:
	 * </p>
	 * 
	 * <table>
	 * <tr><th>D-bus type</th><th>Java type</th></tr>
	 * <tr><td>Byte</td><td>{@link java.lang.Byte}</td></tr>
	 * <tr><td>Boolean</td><td>{@link java.lang.Boolean}</td></tr>
	 * <tr><td>INT16</td><td>{@link java.lang.Short}</td></tr>
	 * <tr><td>UINT16</td><td><code>null</code></td></tr>
	 * <tr><td>INT32</td><td>{@link java.lang.Integer}</td></tr>
	 * <tr><td>UINT32</td><td><code>null</code></td></tr>
	 * <tr><td>INT64</td><td>{@link java.lang.Long}</td></tr>
	 * <tr><td>UINT64</td><td><code>null</code></td></tr>
	 * <tr><td>DOUBLE</td><td>{@link java.lang.Double}</td></tr>
	 * <tr><td>STRING</td><td>{@link java.lang.String}</td></tr>
	 * <tr><td>OBJECT_PATH</td><td><code>null</code></td></tr>
	 * <tr><td>SIGNATURE</td><td><code>null</code></td></tr>
	 * <tr><td>ARRAY</td><td><code>null</code></td></tr>
	 * <tr><td>STRUCT</td><td><code>null</code></td></tr>
	 * <tr><td>VARIANT</td><td><code>null</code></td></tr>
	 * <tr><td>DICT</td><td><code>null</code></td></tr>
	 * <tr><td>UNIX_FD</td><td><code>null</code></td></tr>
	 * </table>
	 * 
	 * <p>
	 * The type is not known at compile time. You should cast the values to the correct type at runtime
	 * </p>
	 * 
	 * @return The top level d-bus values
	 * @throws RemoteException
	 */
	public Object[] getValues() throws RemoteException
	{
		if(errorName != null)
			throw new RemoteException(errorName, null);
		return arguments != null ? arguments.getContent() : null;
	}
	
	DbusStruct getValuesStruct()
	{
		return arguments;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(path); sb.append('\n');
		sb.append(interfaceName); sb.append('\n');
		sb.append(member); sb.append('\n');
		sb.append(errorName); sb.append('\n');
		sb.append(reply_serial); sb.append('\n');
		sb.append(destination); sb.append('\n');
		sb.append(sender); sb.append('\n');
		sb.append(Signature); sb.append('\n');
		sb.append(unix_fds); sb.append('\n');
		
		sb.append(arguments.toString()); sb.append('\n');
		return sb.toString();
	}
}
