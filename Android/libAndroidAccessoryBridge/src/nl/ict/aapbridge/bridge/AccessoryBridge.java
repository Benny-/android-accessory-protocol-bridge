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
import android.util.Log;


/**
 * The class that handles the communication with Android Accessory
 * @author jurgen
 */
public class AccessoryBridge implements Channel
{
	public class Port implements ByteChannel, ReadableByteChannel {
		
		ByteBuffer header = ByteBuffer.allocate(4);
		
		private boolean inputOpen = true;
		private boolean outputOpen = true;
		
		private Port(int portNr) {
			header.order(ByteOrder.LITTLE_ENDIAN);
			header.putShort((short) portNr);
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
		public int write(ByteBuffer buffer) throws IOException {
			header.reset();
			short writeAmount = (short) (buffer.remaining() > 4000 ? 4000 : buffer.remaining());
			header.putShort(writeAmount);
			header.position(0);
			synchronized (outputStream) {
				outputStream.write(header.array(), 0, header.remaining());
				outputStream.write(buffer.array(), buffer.arrayOffset(), writeAmount);
			}
			return writeAmount;
		}

		@Override
		public int read(ByteBuffer buffer) throws IOException {
			return inputStream.read(buffer.array(), buffer.arrayOffset(), buffer.remaining());
		}
	}
	
	public interface Service {
		void onDataReady(int length) throws IOException;
		Port getPort();
	}
	
	private AccessoryConnection connection;
	private OutputStream outputStream;
	private InputStream inputStream;
	private Map<Short, Service> activeServices = new HashMap<Short, Service>();
	private Port serviceSpawner = new Port(0);
	private Port keepalive = new Port(1);
	private Timer pinger = new Timer("Pinger", true);
	
	private static final ByteBuffer ping = ByteBuffer.allocate(4);
	private static final ByteBuffer portRequest = ByteBuffer.allocate(4);
	
	static {
		ping.order(ByteOrder.LITTLE_ENDIAN);
		ping.put("ping".getBytes(Charset.forName("utf-8")));
		
		portRequest.order(ByteOrder.LITTLE_ENDIAN);
		portRequest.put((byte)'o');
		portRequest.mark();
	}
	
	public AccessoryBridge(AccessoryConnection connection) throws IOException {
		this.connection = connection;
		outputStream = this.connection.getOutputStream();
		inputStream = this.connection.getInputStream();
		new ReceiverThread().start();
		pinger.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				try {
					ping.rewind();
					keepalive.write(ping);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}, 0, 10000);
	}
	
	public synchronized Port requestService(byte serviceIdentifier, ByteBuffer arguments, Service service) throws IOException
	{
		portRequest.reset();
		portRequest.put(serviceIdentifier);
		portRequest.putShort((short) arguments.remaining());
		portRequest.position(0);
		while(portRequest.hasRemaining())
			serviceSpawner.write(portRequest);
		while(arguments.hasRemaining())
			serviceSpawner.write(arguments);
		return null;
	}
	
	private static final ByteBuffer emptyByteBuffer = ByteBuffer.allocate(0);
	public synchronized Port requestService(byte serviceIdentifier, Service service) throws IOException
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
		
		public void run()
		{
			try {
				while(true)
				{
					bb.rewind();
					Log.d(TAG, "Reading inputStream");
					while (bb.remaining() < 4)
					{
						if(inputStream.read(bb.array(),bb.arrayOffset(),bb.remaining()) == -1)
							throw new IOException("End of file");
						bb.position(bb.position()+1);
					}
					short destinationPort = bb.getShort();
					short dataLength = bb.getShort();
					
					Log.d(TAG, "AAB msg: Port "+destinationPort+" dataLength:"+dataLength);
					Service service = activeServices.get(destinationPort);
					if(service == null)
						Log.w(TAG, "Received a message for a port where no service is listening");
					else
						service.onDataReady(dataLength);
				}
				} catch (Exception e)
				{
					Log.e(TAG, "Reader thread has stopped", e);
				} finally
				{
					try {
						inputStream.close();
					} catch (IOException e) {
						Log.e(TAG, "", e);
					}
			}
			} catch (Exception e)
			{
				Log.e(TAG, "Reader thread has stopped", e);
			} finally
			{
				try {
					inputStream.close();
				} catch (IOException e) {
					Log.e(TAG, "", e);
				}
			}
		}
	}
	
	public Dbus createDbus(DbusHandler dbusHandler) throws IOException
	{
		return new Dbus(dbusHandler, this);
	}
	
	public DbusSignals createDbusSignal(
			DbusHandler dbusHandler,
			String busname,
			String objectpath,
			String interfaceName,
			String memberName) throws IOException
	{
		return new DbusSignals(dbusHandler, this, busname, objectpath, interfaceName, memberName);
	}
	
	public void createBulkTransfer()
	{
		// TODO: Implement createBulkTransfer.
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