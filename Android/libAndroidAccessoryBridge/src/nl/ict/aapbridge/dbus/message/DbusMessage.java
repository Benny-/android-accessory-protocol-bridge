package nl.ict.aapbridge.dbus.message;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.util.Log;

import nl.ict.aapbridge.dbus.RemoteException;
import nl.ict.aapbridge.dbus.message.types.DbusArray;
import nl.ict.aapbridge.dbus.message.types.DbusObjectPath;
import nl.ict.aapbridge.dbus.message.types.DbusSignature;
import nl.ict.aapbridge.dbus.message.types.DbusStruct;
import nl.ict.aapbridge.dbus.message.types.DbusVariant;
import static nl.ict.aapbridge.TAG.TAG;

/**
 * <p>Represents a DbusMessage and may contain a array of d-bus values converted to java types.</p>
 * 
 * <p>You can access the values by calling {@link #getValues()} and casting the Objects to the expected type</p>
 */
public class DbusMessage {
	
	private DbusObjectPath objectPath;
	private String interfaceName;
	private String member;
	private String errorName;
	private int reply_serial = -1;
	private String destination;
	private String sender;
	private DbusSignature signature;
	private int unix_fds = -1;
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
					this.objectPath = (DbusObjectPath) variant.getEmbeddedThing();
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
					this.signature = (DbusSignature) variant.getEmbeddedThing();
					break;
				case 9:
					this.unix_fds = (Integer) variant.getEmbeddedThing();
					break;
				default:
					Log.w(TAG, "Warning unknwon dbus message header "+headertype+" : "+variant);
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
		if(signature != null)
			arguments = new DbusStruct("("+signature.getSignatureString()+")",bb);
	}
	
	/**
	 * <p>
	 * The d-bus data is converted to java data types. This is done according to the following table:
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
	 * Only you know what return values a method might return. You should cast the values to the correct type during runtime.
	 * </p>
	 * 
	 * @return The top level d-bus values
	 * @throws RemoteException
	 */
	public Object[] getValues() throws RemoteException
	{
		if(errorName != null)
			throw new RemoteException(errorName, null);
		if(arguments == null)
			throw new RemoteException("There are not values in this d-bus message");
		return arguments != null ? arguments.getContent() : null;
	}
	
	/**
	 * NOT part of public API.
	 * 
	 * @return
	 */
	DbusStruct getValuesStruct()
	{
		return arguments;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(objectPath != null)
		{
			sb.append("Object path         : "); sb.append(objectPath);          sb.append('\n');
		}
		if(interfaceName != null)
		{
			sb.append("Interface name      : "); sb.append(interfaceName); sb.append('\n');
		}
		if(member != null)
		{
			sb.append("Member name         : "); sb.append(member);        sb.append('\n');
		}
		if(errorName != null)
		{
			sb.append("Error name          : "); sb.append(errorName);     sb.append('\n');
		}
		if(reply_serial != -1)
		{
			sb.append("Reply serial        : "); sb.append(reply_serial);  sb.append('\n');
		}
		if(destination != null)
		{
			sb.append("Destination busname : "); sb.append(destination);   sb.append('\n');
		}
		if(sender != null)
		{
			sb.append("Sender busname      : "); sb.append(sender);        sb.append('\n');
		}
		if(signature != null)
		{
			sb.append("Signature           : "); sb.append(signature);     sb.append('\n');
		}
		if(unix_fds != -1)
		{
			sb.append("Unix FD             : "); sb.append(unix_fds);      sb.append('\n');
		}
		
		sb.append(arguments.toString()); sb.append('\n');
		return sb.toString();
	}
}
