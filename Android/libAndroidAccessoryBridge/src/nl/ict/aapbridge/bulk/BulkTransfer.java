package nl.ict.aapbridge.bulk;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import android.util.Log;

import nl.ict.aapbridge.bridge.AccessoryBridge;
import nl.ict.aapbridge.bridge.ServiceRequestException;
import nl.ict.aapbridge.bridge.AccessoryBridge.BridgeService;
import nl.ict.aapbridge.bridge.AccessoryBridge.Port;
import static nl.ict.aapbridge.TAG.TAG;

public class BulkTransfer implements BridgeService{
	public static final Charset utf8 = Charset.forName("UTF-8");
	
	private final Port port;
	private BulkInput bulkInput = new BulkInput();
	private BulkOutput bulkOutput = new BulkOutput();
	private PipedOutputStream outputStream = new PipedOutputStream(bulkInput);
	/**
	 * Maximum size port message is 4000 bytes, so we match that.
	 */
	private ByteBuffer receiverBuffer = ByteBuffer.allocate(4000);
	
	/**
	 * <p>The Port class already flushes every message. So we dont have to implement that.
	 * It is a implementation detail, so it may be changed.</p>
	 */
	class BulkOutput extends OutputStream
	{
		private ByteBuffer one = ByteBuffer.allocate(1);
		
		@Override
		public void write(int oneByte) throws IOException {
			one.clear();
			one.put((byte)oneByte);
			one.flip();
			port.write(one);
		}
		
		@Override
		public void write(byte[] buffer, int offset, int count)
				throws IOException {
			ByteBuffer bb = ByteBuffer.wrap(buffer, offset, count);
			port.write(bb);
		}
		
		@Override
		public void close() throws IOException {
			port.eof();
		}
	}
	
	class BulkInput extends PipedInputStream
	{
		@Override
		public synchronized void close() throws IOException {
			try
			{
				port.close();
			}
			finally
			{
				super.close();
			}
		}
	}
	
	/**
	 * <p></p>
	 * 
	 * @param bridge May not be null
	 * @param arguments Null is allowed
	 * @throws IOException
	 * @throws ServiceRequestException
	 */
	public BulkTransfer(AccessoryBridge bridge, String objectPath, String arguments) throws IOException, ServiceRequestException
	{
		if(bridge == null)
			throw new NullPointerException("bridge may not be null");
		
		if(arguments == null)
			arguments = "";
		
		ByteBuffer bb = ByteBuffer.allocate(3000);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		
		bb.clear();
		bb.put(objectPath.getBytes(utf8));
		bb.put((byte)0);
		bb.put(arguments.getBytes(utf8));
		bb.put((byte)0);
		bb.flip();
		
		this.port = bridge.requestService((byte)1, bb, this);
	}
	
	public OutputStream getOutput()
	{
		return this.bulkOutput;
	}
	
	public InputStream getInput()
	{
		return this.bulkInput;
	}

	@Override
	public void onDataReady(int length) throws IOException {
		receiverBuffer.clear();
		receiverBuffer.limit(length);
		port.readAll(receiverBuffer);
		receiverBuffer.flip();
		this.outputStream.write(receiverBuffer.array(), receiverBuffer.arrayOffset() + receiverBuffer.position(), receiverBuffer.remaining());
	}

	@Override
	public Port getPort() {
		return port;
	}

	@Override
	public void onEof() {
		try {
			this.outputStream.close();
		} catch (IOException e) {
			Log.e(TAG, "");
		}
	}

}
