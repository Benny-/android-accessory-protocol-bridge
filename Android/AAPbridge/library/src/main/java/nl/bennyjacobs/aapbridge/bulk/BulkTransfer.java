package nl.bennyjacobs.aapbridge.bulk;

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
import android.widget.Toast;

import nl.bennyjacobs.aapbridge.bridge.AccessoryBridge;
import nl.bennyjacobs.aapbridge.bridge.ServiceRequestException;
import nl.bennyjacobs.aapbridge.bridge.AccessoryBridge.BridgeService;
import nl.bennyjacobs.aapbridge.bridge.AccessoryBridge.Port;
import static nl.bennyjacobs.aapbridge.TAG.TAG;

/**
 * <p>Bulk transfer is more efficient compared to d-bus for sending a lot of data. Bulk
 * transfer uses fifo's on the remote accessory to transfer data between the bridge and the
 * payload.</p>
 * 
 * <p>Bulk transfer requires the payload to adhere to a protocol before bulk data can transfer.
 * The payload needs to implement a d-bus method:</p>
 * 
 * <pre>onBulkRequest(String fifoToPayload, String fifoToAndroid, String requestedBulkData)}</pre>
 * 
 * <p>This function belongs to the interface:</p>
 * 
 * <pre>nl.bennyjacobs.aapbridge.bulk</pre>
 * 
 * <p>The objectpath is not specified, this is for the developer to decide.</p>
 * 
 * <p>The bulk transfer is on the payload side visible as two fifo's. On the Android side
 * this is visible as a input/output stream pair. Both streams must be closed for all resources
 * related to the bulk transfer to clean up. The inputStream will be automatically closed
 * once it receives a end of file from the payload. The payload can send a end of file by
 * closing the output fifo.</p>
 * 
 * <p>Example:</p>
 * 
 * <pre>
 * {@code 
InputStream input;
OutputStream output;
{
	BulkTransfer transfer = bridge.createBulkTransfer("nl.ict.AABUnitTest", "/nl/ict/AABUnitTest/bulk/echo1", "Memory/File/Anything");
	input = transfer.getInput();
	output = transfer.getOutput();
}

output.write('h');
output.write('e');
output.write('l');
output.write('l');
output.write('o');
output.close();
assertEquals('h', input.read());
assertEquals('e', input.read());
assertEquals('l', input.read());
assertEquals('l', input.read());
assertEquals('o', input.read());
assertEquals(-1, input.read());
input.close();
}
 * </pre>
 * 
 */
public class BulkTransfer implements BridgeService{
	public static final Charset utf8 = Charset.forName("UTF-8");
	
	private final Port port;
	
	/**
	 * <p>{@link #bulkInput} and {@link #toApp} are connected. The accessory bridge feeds data
	 * into toApp and comes out of bulkInput. The user can obtain bulkInput by calling {@link #getInput()}</p>
	 */
	private PipedBulkInput bulkInput = new PipedBulkInput();
	private PipedOutputStream toApp = new PipedOutputStream(bulkInput);
	
	/**
	 * <p>{@link #bulkOutput} and {@link #toPayload} are connected. bulkOutput is visible to the
	 * use by calling {@link #getOutput()}. toPayload has a overriden write() method which sends
	 * the data directly to the accessory.</p>
	 */
	private BulkOutput toPayload = new BulkOutput();
	private BufferedOutputStream bulkOutput = new BufferedOutputStream(toPayload);
	
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
			while (bb.hasRemaining())
				port.write(bb);
		}
		
		@Override
		public void close() throws IOException {
			port.eof();
		}
	}
	
	class PipedBulkInput extends PipedInputStream
	{
		@Override
		public synchronized void close() throws IOException {
			try
			{
				port.close();
			}
			catch (IOException e)
			{
				// Meh. port is already closed. Who cares?
			}
			finally
			{
				super.close();
			}
		}
	}
	
	/**
	 * <p>Request a bulk transfer to a remote payload.</p>
	 * 
	 * <p>The argument 'arguments' is passed to the payload.</p>
	 * 
	 * @param bridge May not be null
	 * @param busname May not be null
	 * @param objectPath May not be null
	 * @param arguments Null is allowed
	 * @throws IOException If connnection to the remote accessory has been severed
	 * @throws ServiceRequestException If the payload refused the bulk transfer or if the busname didden't exist or if something horrible happened to the d-bus
	 */
	public BulkTransfer(AccessoryBridge bridge, String busName, String objectPath, String arguments) throws IOException, ServiceRequestException
	{
		if(bridge == null)
			throw new NullPointerException("bridge may not be null");
		
		if(busName == null)
			throw new NullPointerException("busname may not be null");
		
		if(objectPath == null)
			throw new NullPointerException("objectPath may not be null");
		
		if(arguments == null)
			arguments = "";
		
		Log.v(TAG, "BulkTransfer requested for busname "+busName+" objectpath "+objectPath+" amd arguments "+arguments);
		
		ByteBuffer bb = ByteBuffer.allocate(3000);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		
		bb.clear();
		bb.put(busName.getBytes(utf8));
		bb.put((byte)0);
		bb.put(objectPath.getBytes(utf8));
		bb.put((byte)0);
		bb.put(arguments.getBytes(utf8));
		bb.put((byte)0);
		bb.flip();
		
		this.port = bridge.requestService((byte)1, bb, this);
	}
	
	/**
	 * Returns a buffered outputstream.
	 * 
	 * @return
	 */
	public OutputStream getOutput()
	{
		return this.bulkOutput;
	}
	
	/**
	 * Returns a buffered inputsteam.
	 * 
	 * @return
	 */
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
		this.toApp.write(receiverBuffer.array(), receiverBuffer.arrayOffset() + receiverBuffer.position(), receiverBuffer.remaining());
	}

	@Override
	public Port getPort() {
		return port;
	}

	@Override
	public void onEof() {
		try {
			this.toApp.close();
		} catch (IOException e) {
			Log.e(TAG, "", e);
		}
	}

}
