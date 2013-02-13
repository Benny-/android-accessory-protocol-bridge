package nl.ict.aapbridge.bridge;

import static nl.ict.aapbridge.TAG.TAG;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ByteChannel;
import java.nio.channels.Channel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import nl.ict.aapbridge.aap.AccessoryConnection;
import nl.ict.aapbridge.dbus.DbusMethods;
import nl.ict.aapbridge.dbus.DbusHandler;
import nl.ict.aapbridge.dbus.DbusSignals;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

/**
 * <p>Handles the communication with the android accessory bridge.</p>
 * 
 * <p>The AccessoryBridge allows multiple concurrent data streams on top of the android accessory protocol.
 * These datastreams are not directly available to to enduser. On top of these multiplexed streams the bridge
 * also periodically sends messages to the accessory to ensure it is still alive</p>
 * 
 * <p>While you cant use the bridge directly, you can create other services who can make use of the bridge. This
 * include {@link DbusMethods} for invoking d-bus methods and {@link DbusSignals} for listening to d-bus signals. You
 * can do so by calling a factory method (like {@link #createDbus(DbusHandler)}) or invoking the constructor</p>
 * 
 * <p>Calling {@link #close()} will release any resources in use and invalidates the bridge (any operation will throw a io-exception)</p>
 * 
 * @author jurgen
 * @see DbusMethods
 * @see DbusSignals
 */
public class AccessoryBridge implements Channel
{
	/**
	 * NOT part of the public API.
	 * 
	 * The Port class multiplexes all data over the underlying connection by prepending all data with the port number and length of data.
	 * 
	 * Writing to the port is thread-safe and can be done at any time.
	 * 
	 * Reading from the port works different. A service will be notified if data is ready to be received. The service MUST call {@link #readAll(ByteBuffer)} or {@link #skipRead(int)}.
	 */
	public class Port implements ByteChannel, ReadableByteChannel {
		
		private final ByteBuffer header = ByteBuffer.allocate(4);
		
		private short portNr;
		private boolean inputOpen = true;
		private boolean outputOpen = true;
		
		private Port(short portNr) {
			this.portNr = portNr;
			header.order(ByteOrder.LITTLE_ENDIAN);
			header.putShort(portNr);
			header.mark();
		}
		
		@Override
		public void close() throws IOException {
			inputOpen = false;
			portStatusService.close(this);
		}
		
		public void eof() throws IOException {
			outputOpen = false;
			portStatusService.eof(this);
		}

		@Override
		public boolean isOpen() {
			return inputOpen || outputOpen;
		}
		
		public boolean isInputOpen()
		{
			return inputOpen;
		}
		
		public boolean isOutputOpen()
		{
			return outputOpen;
		}

		@Override
		public synchronized int write(ByteBuffer buffer) throws IOException {
			if(!outputOpen)
			{
				throw new IOException("Port output stream is closed");
			}
			header.reset();
			short writeAmount = (short) (buffer.remaining() > 4000 ? 4000 : buffer.remaining());
			header.putShort(writeAmount);
			synchronized (AccessoryBridge.this) {
				outputStream.write(header.array(), header.arrayOffset(), header.capacity());
				outputStream.write(buffer.array(), buffer.arrayOffset(), writeAmount);
				outputStream.flush();
			}
			buffer.position(buffer.position() + writeAmount);
			Log.d(TAG, "PORT "+portNr+": wrote "+writeAmount+" bytes");
			return writeAmount;
		}

		/**
		 * Read a number of bytes and put them in the buffer. This call be only be done if bytes are ready.
		 * 
		 * {@link nl.ict.aapbridge.bridge.AccessoryBridge.BridgeService#onDataReady(int) onDataReady} will be called if bytes are ready. All byte MUST be read before returning
		 * from that function.
		 * 
		 * {@link #read(ByteBuffer)} might not read all bytes in one go. You must keep calling this function until
		 * all you have read all bytes as specified by Service#onDataReady(int).
		 * 
		 * @see #readAll
		 */
		@Override
		public int read(ByteBuffer buffer) throws IOException {
			if(!inputOpen)
				throw new IOException("Port input is closed, this error should never be seen"); // Why should this error never be seen? Well.. The reader thread signals if a port should be read, its his duty to prevent these reads if data is incomming but the port input is closed.
			int read = inputStream.read(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining());
			buffer.position(buffer.position() + read);
			return read;
		}
		
		/**
		 * 
		 * @param i The amount of bytes to skip.
		 * @throws IOException
		 * @see #readAll(ByteBuffer)
		 */
		public void skipRead(int i) throws IOException
		{
			while(i > 0)
			{
				// XXX: This is a bug workaround.
				// i -= inputStream.skip(i); // <- This code seems to break if connected to a usb accessory.
				
				// The skip() function call seems to be broken for usb file descriptors. Skip works fine for bluetooth sockets however.
				// See FileInputStream's skip implementation for more details.
				// Consider to move this workaround code to the underlying implementation.
				// This bug has been seen on a HTC wildfire. Android version 2.3.7. CyanogenMod-7-11162011-NIGHTLY-buzz
				// The following block of code is the workaround.
				{
					inputStream.read();
					i--;
				}
			}
		}
		
		/**
		 * Same as {@link #read}, but guarantees to fill the whole buffer or throw a IOException
		 * 
		 * @throws IOException 
		 */
		public void readAll(ByteBuffer buffer) throws IOException {
			while(buffer.hasRemaining())
				read(buffer);
		}
	}
	
	/**
	 * NOT part of the public API.
	 */
	public interface BridgeService {

		/**
		 * NOT part of the public API.
		 * 
		 * Called from the ReceiverThread if bytes must be read.
		 * 
		 * The bytes MUST be read from the port before this function call returns. Use the read or skip functions on the {@link Port}
		 * 
		 * @param length
		 * @throws IOException
		 * @see Port#read(ByteBuffer)
		 * @see ReceiverThread
		 */
		void onDataReady(int length) throws IOException;
		
		/**
		 * NOT part of public API.
		 * 
		 * @return The Port object associated with this service.
		 */
		Port getPort();

		void onEof();
	}
	
	private AccessoryConnection connection;
	private OutputStream outputStream;
	private InputStream inputStream;
	
	/**
	 * <p>This field is a lookup table. Every created service is stored here.
	 * The index is the same as the port this service is located on. Once a service
	 * is cleaned up, the reference in this table becomes null. There
	 * may be gaps if a service between two other services are cleaned up.</p>
	 */
	private BridgeService activeServices[] = new BridgeService[400];
	private Handler keepAliveFailureHandler = new Handler(){
		@Override
		public void handleMessage(final Message msg) {
			try {
				connection.close();
			} catch (IOException e) {
				Log.e(TAG, "", e);
			}
			finally{
				//msg.recycle(); // XXX: Recycling should not cause a issue here. It however threw a exception some time later: "message already in use".
			}
		}
	};
	
	private class PortStatus implements BridgeService{
		private Port port;
		
		public PortStatus(Port port) {
			this.port = port;
		}

		/**
		 * 
		 * @param targetPort port who promised not to send to the accessory
		 * @throws IOException
		 */
		void eof(Port targetPort) throws IOException
		{
			ByteBuffer bb = ByteBuffer.allocate(4);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.putShort(targetPort.portNr);
			bb.put((byte)3);
			bb.put((byte)0);
			bb.rewind();
			port.write(bb);
			
			if(!targetPort.inputOpen && !targetPort.inputOpen)
				activeServices[targetPort.portNr] = null; // Cleanup
		}
		
		/**
		 * @param targetPort port who is not interested in more bytes from the accessory
		 * @throws IOException
		 */
		void close(Port targetPort) throws IOException
		{
			ByteBuffer bb = ByteBuffer.allocate(4);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			bb.putShort(targetPort.portNr);
			bb.put((byte)4);
			bb.put((byte)0);
			bb.rewind();
			
			port.write(bb);
			
			if(!targetPort.inputOpen && !targetPort.inputOpen)
				activeServices[targetPort.portNr] = null; // Cleanup
		}

		@Override
		public void onDataReady(int length) throws IOException {
			ByteBuffer bb = ByteBuffer.allocate(4);
			bb.order(ByteOrder.LITTLE_ENDIAN);
			if(length != 4)
				throw new Error("Port status messages should be 4 bytes");
			port.readAll(bb);
			bb.rewind();
			
			short port = bb.getShort();
			byte status = bb.get();
			if(activeServices[port] == null)
			{
				Log.w(TAG, "PORT_STATUS ignoring new status for closed port: "+port);
			}
			else
			{
				switch (status)
				{
					case 3: // STREAM_EOF
						Log.i(TAG, "PORT "+port+" received EOF");
						if(activeServices[port].getPort().inputOpen)
							Log.e(TAG, "PORT "+port+" received a double EOF");
						activeServices[port].getPort().inputOpen = false;
						activeServices[port].onEof();
						break;
					
					case 4: // STREAM_CLOSE
						Log.i(TAG, "PORT "+port+" received CLOSE");
						if(activeServices[port].getPort().outputOpen)
							Log.e(TAG, "PORT "+port+" received a double CLOSE");
						activeServices[port].getPort().outputOpen = false;
						break;
						
					default:
						Log.e(TAG, "PORT "+port+" received unknown status "+status);
						break;
				}
				if(!activeServices[port].getPort().inputOpen && !activeServices[port].getPort().inputOpen)
					activeServices[port] = null; // Cleanup
			}
		}

		@Override
		public Port getPort() {
			return port;
		}

		@Override
		public void onEof() {
			// TODO Auto-generated method stub
		}
	}
	
	private PortStatus portStatusService			= new PortStatus	(new Port( (short) 0 ) );
	private ServiceSpawner serviceSpawner	= new ServiceSpawner(new Port( (short) 1 ) );
	private Keepalive keepAlive				= new Keepalive		(new Port( (short) 2 ), keepAliveFailureHandler);
	
	private static final ByteBuffer portRequest = ByteBuffer.allocate(4);
	
	/**
	 * NOT part of public API. This method exist for testing purposes only and should never be called.
	 * @throws IOException 
	 * 
	 */
	public void sendKeepalive() throws IOException
	{
		keepAlive.sendKeepalive();
	}
	
	static {
		portRequest.order(ByteOrder.LITTLE_ENDIAN);
		portRequest.put((byte)'o');
		portRequest.mark();
	}
	
	/**
	 * Builds a accessory bridge on the android accessory protocol. This will be required before you can use any of the other protocols.
	 * 
	 * @param connection The underlying connection to use. This can be bluetooth, usb or any other implementation
	 * @throws IOException
	 * @see DbusMethods
	 * @see DbusSignals
	 * 
	 * @throws NullPointerException If connection is null
	 * @throws IOException If connection is no longer a valid connection (i.e. it has been closed)
	 */
	public AccessoryBridge(AccessoryConnection connection) throws IOException {
		
		if(connection == null)
			throw new NullPointerException("Connection may not be null");
		
		this.connection = connection;
		outputStream = this.connection.getOutputStream();
		inputStream = this.connection.getInputStream();
		
		Log.i(TAG, "Created a new "+AccessoryBridge.class.getSimpleName());
		
		this.activeServices[portStatusService.getPort().portNr		]	= portStatusService;
		this.activeServices[serviceSpawner.getPort().portNr	]	= serviceSpawner;
		this.activeServices[keepAlive.getPort().portNr		]	= keepAlive;
		
		new ReceiverThread().start();
	}
	
	/**
	 * NOT part of the public API.
	 * 
	 * Sends a request to the accessory for the requested service.
	 * 
	 * @param serviceIdentifier
	 * @param arguments
	 * @param service
	 * @return a {@link Port port} associated with requested port.
	 * @throws IOException
	 * @throws ServiceRequestException
	 */
	public Port requestService(byte serviceIdentifier, ByteBuffer arguments, BridgeService service) throws IOException, ServiceRequestException
	{
		short portNr = serviceSpawner.requestService(serviceIdentifier, arguments);
		Port port = new Port(portNr);
		this.activeServices[portNr] = service;
		return port;
	}
	
	private static final ByteBuffer emptyByteBuffer = ByteBuffer.allocate(0);
	
	/**
	 * NOT part of public API.
	 * 
	 * Calls {@link #requestService(byte, ByteBuffer, BridgeService) requestService(byte, ByteBuffer arguments, Service)} } with empty arguments.
	 */
	public Port requestService(byte serviceIdentifier, BridgeService service) throws IOException, ServiceRequestException
	{
		return requestService(serviceIdentifier, emptyByteBuffer, service);
	}
	
	/**
	 * <p>Thread who receive the data from Android Accessory bus, decodes it and sends the data to the relevant service.</p>
	 * 
	 * <p>The received data is a multiplexed message. The header is 4 bytes.
	 * The first two bytes desginate the port it should arrive on
	 * and the last two indicate the length of the data which follows.</p>
	 * 
	 * <p>It uses the {@link AccessoryBridge#activeServices} to lookup the service which should
	 * receive the data within the multiplexed message. It calls the {@link BridgeService#onDataReady(int)}
	 * function on the service who should receive the data. The called service should read the amount of bytes
	 * ready from the port using {@link Port#readAll(ByteBuffer)} or {@link Port#skipRead(int)}. The 
	 * called port should not block under any circumstances, as the reader thread is the only thread who
	 * passes data around to all the services.</p>
	 * 
	 * @author Jurgen
	 */
	class ReceiverThread extends Thread
	{
		private ByteBuffer bb = ByteBuffer.allocate(4);
		
		public ReceiverThread() {
			bb.order(ByteOrder.LITTLE_ENDIAN);
		}
		
		public void run()
		{
			try
			{
				while(true)
				{
					bb.rewind();
					Log.d(TAG, "Reading inputStream");
					while (bb.hasRemaining())
					{
						int read = inputStream.read(bb.array(),bb.arrayOffset() + bb.position(),bb.remaining());
						if(read == -1)
							throw new IOException("End of file");
						bb.position(bb.position() + read);
					}
					bb.rewind();
					
					short port = bb.getShort();
					short dataLength = bb.getShort();
					
					Log.d(TAG, "PORT "+port+": received: "+dataLength+" bytes");
					BridgeService service = activeServices[port];
					if(service == null)
					{
						int mustSkip = dataLength;
						Log.w(TAG, "Received a message for a port where no service is listening");
						while(mustSkip > 0)
							mustSkip -= inputStream.skip(mustSkip);
					}
					else
					{
						if(service.getPort().inputOpen)
						{
							try
							{
								service.onDataReady(dataLength);
							}
							catch (Exception e)
							{
								Log.e(TAG, "", e);
								// TODO: Think about what should happen here.
							}
						}
						else
						{
							Log.w(TAG, "PORT "+port+" received data while port is closed");
						}
					}
				}
			}
			catch (Exception e)
			{
				Log.e(TAG, "Reader thread has stopped", e);
			}
			finally
			{
				try {
					close();
				} catch (IOException e) {
					Log.e(TAG, "", e);
				}
			}
		}
	}
	
	/**
	 * <p>Creates a Dbus object. This is the same as <code>new {@link DbusMethods#DbusMethods(DbusHandler, AccessoryBridge)}</code></p>
	 * 
	 * @throws IOException If the connection to the accessory is severed
	 * @throws ServiceRequestException If the remote host could not start the requested service
	 * @see DbusMethods
	 */
	public DbusMethods createDbus(DbusHandler dbushandler) throws IOException, ServiceRequestException
	{
		return new DbusMethods(this, dbushandler);
	}
	
	/**
	 * <p>Creates a DbusSignals object. This is the same as
	 * <code>new {@link DbusSignals#DbusSignals(DbusHandler, AccessoryBridge, String, String, String, String)}</code></p>
	 * 
	 * @throws IOException If the connection to the accessory is severed
	 * @throws ServiceRequestException If the remote host could not start the requested service
	 * @see DbusSignals
	 */
	public DbusSignals createDbusSignal(
			DbusHandler dbusHandler,
			String busname,
			String objectpath,
			String interfaceName,
			String memberName) throws IOException, ServiceRequestException
	{
		return new DbusSignals(this, dbusHandler, busname, objectpath, interfaceName, memberName);
	}
	
	/**
	 * TODO: Implement createBulkTransfer
	 * 
	 * @throws IOException If the connection to the accessory is severed
	 * @throws ServiceRequestException If the remote host could not start the requested service
	 */
	public void createBulkTransfer() throws IOException, ServiceRequestException
	{
		throw new ServiceRequestException("Bulk transfer not yet implemented");
	}

	@Override
	public void close() throws IOException {
		connection.close();
	}

	@Override
	public boolean isOpen() {
		return !connection.disconnected();
	}

}