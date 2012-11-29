package nl.ict.aapbridge.helper;

public class ByteHelper {
	private ByteHelper() {
	}
	
	/**
	 * reverse byte order
	 * @param buffer bytes to be reversed
	 * @return
	 */
	public static byte[] reverse(byte[] buffer) {
		byte[] tmp = new byte[buffer.length];
		int j = buffer.length -1;
		for (int i = 0; i < buffer.length; i++) {
			tmp[i] = buffer[j--];
		}
		return tmp;
	}
}