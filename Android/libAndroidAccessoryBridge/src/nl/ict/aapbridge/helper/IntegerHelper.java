package nl.ict.aapbridge.helper;

import java.nio.ByteBuffer;

public class IntegerHelper {
	private IntegerHelper() {

	}
	//convert array of bytes to int
	public static int toInt(byte[] bytes) {
		return ByteBuffer.wrap(bytes).getInt();
	}
	//convert int to array of bytes
	public static byte[] toByte(int integer) {
		return ByteBuffer.allocate(4).putInt(integer).array(); // quick and
																// dirty!
	}
}