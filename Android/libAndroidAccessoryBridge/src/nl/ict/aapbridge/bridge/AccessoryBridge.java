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

import nl.ict.aapbridge.aap.AccessoryConnection;
import nl.ict.aapbridge.dbus.Dbus;
import nl.ict.aapbridge.dbus.DbusHandler;
import nl.ict.aapbridge.dbus.DbusSignals;
import android.os.RemoteException;
import android.util.Log;

/**
 * Handles the communication with the android accessory bridge.
 * 
 * @author jurgen
 */
public class AccessoryBridge implements Channel
{
	public class Port implements ByteChannel, ReadableByteChannel {
		
		private final ByteBuffer header = ByteBuffer.allocate(4);
		
		private boolean inputOpen = true;
		private boolean outputOpen = true;
		
		private Port(short portNr) {
			header.order(ByteOrder.LITTLE_ENDIAN);
			header.putShort(portNr);
			header.mark();
		}
		
		@Override
		public void close() throws IOException {
			inputOpen = false;
			// TODO: Send close over the wire.
		}
		
		public void eof() throws IOException {
			outputOpen = false;
			// TODO: Send eof over the wire.
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
			header.reset();
			short writeAmount = (short) (buffer.remaining() > 4000 ? 4000 : buffer.remaining());
			header.putShort(writeAmount);
			synchronized (AccessoryBridge.this) {
				outputStream.write(header.array(), header.arrayOffset(), header.capacity());
				outputStream.write(buffer.array(), buffer.arrayOffset(), writeAmount);
			}
			return writeAmount;
		}

		/**
		 * Read a number of bytes and put them in the buffer. This call be only be done if bytes are ready.
		 * 
		 * {@link nl.ict.aapbridge.bridge.AccessoryBridge.Service#onDataReady(int) onDataReady} will be called if bytes are ready. All byte MUST be read before returning
		 * from that function.
		 * 
		 * {@link #read(ByteBuffer)} might not read all bytes in one go. You must keep calling this function until
		 * all you have read all bytes as specified by Service#onDataReady(int).
		 * 
		 * @see #readAll
		 */
		@Override
		public int read(ByteBuffer buffer) throws IOException {
			return inputStream.read(buffer.array(), buffer.arrayOffset(), buffer.remaining());
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
	
	public interface Service {
		/**
		 * Called from the ReceiverThread if bytes must be read.
		 * 
		 * The bytes must be read from the port before this function call returns.
		 * 
		 * @param length
		 * @throws IOException
		 * @see Port#read(ByteBuffer)
		 * @see ReceiverThread
		 */
		void onDataReady(int length) throws IOException;
		Port getPort();
	}
	
	private AccessoryConnection connection;
	private OutputStream outputStream;
	private InputStream inputStream;
	private Map<Short, Service> activeServices = new HashMap<Short, Service>();
	private Port serviceSpawner = new Port( (short) 0 );
	private Port keepalive		= new Port( (short) 1 );
	private Timer pinger		= new Timer("Pinger", true);
	
	private static final ByteBuffer ping = ByteBuffer.allocate(4);
	private static final ByteBuffer portRequest = ByteBuffer.allocate(4);
	
	/**
	 * This method exist for testing purposes only and should never be called.
	 * @throws IOException 
	 * 
	 */
	public void sendKeepalive() throws IOException
	{
		ping.rewind();
		keepalive.write(ping);
	}
	
	static {
		ping.order(ByteOrder.LITTLE_ENDIAN);
		ping.put("ping".getBytes(Charset.forName("utf-8")));
		
		portRequest.order(ByteOrder.LITTLE_ENDIAN);
		portRequest.put((byte)'o');
		portRequest.mark();
	}
	
	/**
	 * Builds a accessory bridge on the android accessory protocol. This will be required before you can use any of the other protocols.
	 * 
	 * @param connection The underlying connection to use. This can be bluetooth, usb or any other implementation
	 * @throws IOException
	 * @see {@link Dbus}
	 * @see {@link DbusSignals}
	 */
	public AccessoryBridge(AccessoryConnection connection) throws IOException {
		this.connection = connection;
		outputStream = this.connection.getOutputStream();
		inputStream = this.connection.getInputStream();
		
		new ReceiverThread().start();
		pinger.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				try {
					sendKeepalive();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}, 0, 10000);
	}
	
	/**
	 * Sends a request to the accessory for the requested service.
	 * 
	 * The accessory
	 * 
	 * @param serviceIdentifier
	 * @param arguments
	 * @param service
	 * @return a {@link Port port} associated with requested port.
	 * @throws IOException
	 * @throws ServiceRequestException
	 */
	public synchronized Port requestService(byte serviceIdentifier, ByteBuffer arguments, Service service) throws IOException, ServiceRequestException
	{
		portRequest.reset();
		portRequest.put(serviceIdentifier);
		portRequest.putShort((short) arguments.remaining());
		portRequest.position(0);
		while(portRequest.hasRemaining())
			serviceSpawner.write(portRequest);
		while(arguments.hasRemaining())
			serviceSpawner.write(arguments);
		
		// TODO: Wait for response.
		return null;
	}
	
	private static final ByteBuffer emptyByteBuffer = ByteBuffer.allocate(0);
	/**
	 * Calls {@link #requestService(byte, ByteBuffer, Service) requestService(byte, ByteBuffer arguments, Service)} } with empty arguments.
	 */
	public synchronized Port requestService(byte serviceIdentifier, Service service) throws IOException, ServiceRequestException
	{
		return requestService(serviceIdentifier, emptyByteBuffer, service);
	}
	
	/**
	 * Thread who receive the data from Android Accessory bus, decodes it and sends the data to the relevant service.
	 * 
	 * @author Jurgen
	 *
	 */
	public class ReceiverThread extends Thread
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
					
					short destinationPort = bb.getShort();
					short dataLength = bb.getShort();
					
					Log.d(TAG, "AAB msg: Port "+destinationPort+" dataLength: "+dataLength);
					Service service = activeServices.get(destinationPort);
					if(service == null)
					{
						int mustSkip = dataLength;
						Log.w(TAG, "Received a message for a port where no service is listening");
						while(mustSkip > 0)
							mustSkip -= inputStream.skip(mustSkip);
					}
					else
						service.onDataReady(dataLength);
				}
			}
			catch (Exception e)
			{
				Log.e(TAG, "Reader thread has stopped", e);
			}
			finally
			{
				try {
					inputStream.close();
				} catch (IOException e) {
					Log.e(TAG, "", e);
				}
			}
		}
	}
	
	/**
	 * Creates a Dbus object. This is the same as new {@link Dbus#Dbus(DbusHandler, AccessoryBridge)}
	 * @throws ServiceRequestException 
	 * 
	 * @see Dbus
	 */
	public Dbus createDbus(DbusHandler dbusHandler) throws IOException, ServiceRequestException
	{
		return new Dbus(dbusHandler, this);
	}
	
	/**
	 * Creates a DbusSignals object. This is the same as new {@link DbusSignals#DbusSignals(DbusHandler, AccessoryBridge, String, String, String, String)}
	 * 
	 * @see DbusSignals
	 */
	public DbusSignals createDbusSignal(
			DbusHandler dbusHandler,
			String busname,
			String objectpath,
			String interfaceName,
			String memberName) throws IOException, ServiceRequestException
	{
		return new DbusSignals(dbusHandler, this, busname, objectpath, interfaceName, memberName);
	}
	
	/**
	 * @TODO: Implement createBulkTransfer
	 */
	public void createBulkTransfer()
	{
		
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