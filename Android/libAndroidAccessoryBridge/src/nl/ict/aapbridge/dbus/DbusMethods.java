package nl.ict.aapbridge.dbus;


import java.io.Closeable;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.AsynchronousCloseException;
import java.nio.charset.Charset;

import android.os.Message;
import android.util.Log;

import nl.ict.aapbridge.bridge.AccessoryBridge;
import nl.ict.aapbridge.bridge.AccessoryBridge.Port;
import nl.ict.aapbridge.bridge.AccessoryBridge.BridgeService;
import nl.ict.aapbridge.bridge.ServiceRequestException;
import nl.ict.aapbridge.dbus.message.DbusMessage;
import nl.ict.aapbridge.dbus.message.DbusTypeParser;

/**
 * Functionality for communicating to a remote d-bus (methods only). Dbus method is invoked Asynchronous. The reply is send to the handler.
 * 
 * <p>You should call the {@link #close()} method once you are done.</p>
 * 
 * @author jurgen
 * @see DbusSignals
 * @see DbusHandler
 */
public class DbusMethods implements BridgeService, Closeable
{
	private static final Charset utf8 = Charset.forName("UTF-8");
	
	private int call_id = 0;
	private int return_id = 0;
	
	/**
	 * <p>Used for the convenience functions</p>
	 * 
	 * @see #methodCall(String, String, String, Object...)
	 */
	private String busname;
	
	/**
	 * <p>Used for the convenience functions</p>
	 * 
	 * @see #methodCall(String, String, Object...)
	 */
	private String objectpath;
	
	/**
	 * <p>Used for the convenience functions</p>
	 * 
	 * @see #methodCall(String, Object...)
	 */
	private String interfaceName;
	
	/**
	 * <p>Used for the convenience functions</p>
	 * 
	 * @see #methodCall(Object...)
	 */
	private String functionName;
	
	private final ByteBuffer receiveBuffer = ByteBuffer.allocate(4000);
	private final ByteBuffer sendBuffer = ByteBuffer.allocate(4000);
	private final DbusHandler handler;
	private final Port port;
	
	/**
	 * Create a object which can communicate to a remote d-bus.
	 * 
	 * It has the ability to do method calls and receive the return values.
	 * 
	 * The return values are received asynchronous and posted as messages to the handler.
	 * 
	 * @param dbushandler The handler who will receive Dbus messages
	 * @param bridge The communication multiplexer
	 * @throws IOException
	 * @throws ServiceRequestException 
	 * @see {@link DbusHandler}
	 * @see #methodCall(String, String, String, String, Object...)
	 */
	public DbusMethods(DbusHandler dbushandler, AccessoryBridge bridge) throws IOException, ServiceRequestException {
		if(dbushandler == null)
			throw new NullPointerException("Handler may not be null");
		
		this.handler = dbushandler;
		this.port = bridge.requestService((byte)2, this);
		sendBuffer.order(ByteOrder.LITTLE_ENDIAN);
	}
	
	/**
	 * <p>Performs a remote dbus call. The dbus call will be performed on the session bus
	 * or system bus depending on the accessory's implementation.</p>
	 * 
	 * <p>Throws a BufferOverflowException if the combined arguments byte length exceeds the
	 * internal {@link #sendBuffer} size.</p>
	 * 
	 * @param busname
	 * @param objectpath
	 * @param interfaceName
	 * @param functionName
	 * @param arguments
	 * 
	 * @return Unique id for this request. This value will be the same as Message.arg1 in the DbusHandler message handler for the return value.
	 * 
	 * @throws IOException If connection to the remote port is lost
	 * @throws BufferOverflowException If the byte size of all combined parameters exceed 4000 bytes
	 * @throws NullPointerException If busname, objectpath, interfaceName or functionName are null
	 */
	public synchronized int methodCall(
			String busname,
			String objectpath,
			String interfaceName,
			String functionName,
			Object... arguments) throws IOException
	{
		if(busname == null)
			throw new NullPointerException("Busname may not be null");
		
		if(objectpath == null)
			throw new NullPointerException("Objectpath may not be null");
		
		if(interfaceName == null)
			throw new NullPointerException("Interface name may not be null");
		
		if(functionName == null)
			throw new NullPointerException("Function name may not be null");
		
		sendBuffer.clear();
		sendBuffer.put(busname.getBytes(utf8));
		sendBuffer.put((byte)0);
		sendBuffer.put(objectpath.getBytes(utf8));
		sendBuffer.put((byte)0);
		sendBuffer.put(interfaceName.getBytes(utf8));
		sendBuffer.put((byte)0);
		sendBuffer.put(functionName.getBytes(utf8));
		sendBuffer.put((byte)0);
		
		sendBuffer.putInt(arguments.length);
		for(Object argument : arguments)
		{
			DbusTypeParser.serialise(argument,sendBuffer);
		}
		
		sendBuffer.flip();
		while(sendBuffer.hasRemaining())
			port.write(sendBuffer);
		
		return call_id++;
	}
	
	/**
	 * <p>Convenience function for repeated calls to the same busname.</p>
	 * 
	 * <p>The busname must be set beforehand using {@link #setBusname(String)}</p>
	 * 
	 * @param interfaceName
	 * @param functionName
	 * @param arguments
	 * @return
	 * @throws IOException
	 * @throws NullPointerException If busname, objectpath, interfaceName or functionName are null
	 */
	public int methodCall(
			String objectpath,
			String interfaceName,
			String functionName,
			Object... arguments) throws IOException
	{
		return methodCall(getBusname(), objectpath, interfaceName, functionName, arguments);
	}
	
	/**
	 * <p>Convenience function for repeated calls to the same objectpath.</p>
	 * 
	 * <p>The busname and objectpath must be set beforehand using {@link #setBusname(String)} and {@link #setObjectpath(String) }</p>
	 * 
	 * @param functionName
	 * @param arguments
	 * @return
	 * @throws IOException
	 * @throws NullPointerException If busname, objectpath, interfaceName or functionName are null
	 */
	public int methodCall(
			String interfaceName,
			String functionName,
			Object... arguments) throws IOException
	{
		return methodCall(getBusname(), getObjectpath(), interfaceName, functionName, arguments);
	}
	
	/**
	 * <p>Convenience function for repeated calls to the same interface.</p>
	 * 
	 * <p>The busname, objectpath and interface must be set beforehand using {@link #setBusname(String)}, {@link #setObjectpath(String) } and {@link #setInterfaceName(String) }</p>
	 * 
	 * @param arguments
	 * @return
	 * @throws IOException
	 * @throws NullPointerException If busname, objectpath, interfaceName or functionName are null
	 */
	public int methodCall(
			String functionName,
			Object... arguments) throws IOException
	{
		return methodCall(getBusname(), getObjectpath(), getInterfaceName(), functionName, arguments);
	}
	
	/**
	 * <p>Convenience function for repeated calls to the same method.</p>
	 * 
	 * <p>The busname, objectpath, interface and method must be set beforehand using 
	 * {@link #setBusname(String)}, {@link #setObjectpath(String) },
	 * {@link #setInterfaceName(String) } and {@link #setFunctionName(String) }</p>
	 * 
	 * @param arguments
	 * @return
	 * @throws IOException
	 * @throws NullPointerException If busname, objectpath, interfaceName or functionName are null
	 */
	public int methodCall(
			Object... arguments) throws IOException
	{
		return methodCall(getBusname(), getObjectpath(), getInterfaceName(), getFunctionName(), arguments);
	}

	@Override
	public void onDataReady(int length) throws IOException {
		receiveBuffer.rewind();
		receiveBuffer.limit(length);
		port.readAll(receiveBuffer);
		receiveBuffer.rewind();
		DbusMessage dbusMessage = new DbusMessage(receiveBuffer);
		Message.obtain(handler, DbusHandler.MessageTypes.DbusMethods.ordinal(), return_id++, 0, dbusMessage).sendToTarget();
	}

	@Override
	public Port getPort() {
		return port;
	}
	
	/**
	 * This will start the procedure to close this dbus connection.
	 * 
	 * You will no longer be able to send any dbus messages to the accessory using this dbus object.
	 * 
	 * Any pending responses will still be received and send to the Handler. The dbus connection will terminate once the last pending response is received.
	 */
	@Override
	public void close() throws IOException {
		port.close();
		port.eof();
	}

	@Override
	public void onEof() {
		// TODO Auto-generated method stub
	}

	/**
	 * @return the busname
	 */
	public String getBusname() {
		return busname;
	}

	/**
	 * @param busname the busname to set
	 * 
	 * @see #methodCall(String, String, String, Object...)
	 */
	public void setBusname(String busname) {
		this.busname = busname;
	}

	/**
	 * @return the objectpath
	 */
	public String getObjectpath() {
		return objectpath;
	}

	/**
	 * @param objectpath the objectpath to set
	 * 
	 * @see #methodCall(String, String, Object...)
	 */
	public void setObjectpath(String objectpath) {
		this.objectpath = objectpath;
	}

	/**
	 * @return the interfaceName
	 */
	public String getInterfaceName() {
		return interfaceName;
	}

	/**
	 * @param interfaceName the interfaceName to set
	 * 
	 * @see #methodCall(String, Object...)
	 */
	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}

	/**
	 * @return the functionName
	 */
	public String getFunctionName() {
		return functionName;
	}

	/**
	 * @param functionName the functionName to set
	 * 
	 * @see #methodCall(Object...)
	 */
	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}
}
