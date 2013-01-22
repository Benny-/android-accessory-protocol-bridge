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
	 * @return The top level d-bus values
	 * @throws RemoteException
	 */
	public DbusStruct getArguments() throws RemoteException
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
