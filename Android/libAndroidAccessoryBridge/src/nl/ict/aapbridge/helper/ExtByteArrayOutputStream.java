package nl.ict.aapbridge.helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import nl.ict.aapbridge.bridge.AccessoryMessage;

public class ExtByteArrayOutputStream extends ByteArrayOutputStream {
	public ExtByteArrayOutputStream() {
		super();
	}
	
	/**
	 * write (reversed) int to a outputstream
	 */
	public void write(int value) {
		try {
			super.write(ByteHelper.reverse(IntegerHelper.toByte(value)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * write (reversed array of bytes to outputstream
	 */
	public void write(byte[] value) {
		try {
			super.write(ByteHelper.reverse(value));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * write (enum) accessorymessage messagetype to outputstream
	 * @param value
	 * @throws IOException
	 */
	public void write(AccessoryMessage.MessageType value)
			throws IOException {
		super.write(ByteHelper.reverse( //reverse
					ByteBuffer.allocate(4).putInt( //allocate four bytes and add a nt	
							value.ordinal()).array()));//get number of enum
	}
}