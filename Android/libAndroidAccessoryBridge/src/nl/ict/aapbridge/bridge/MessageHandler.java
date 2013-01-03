package nl.ict.aapbridge.bridge;

import java.io.IOException;

import nl.ict.aapbridge.SystemHolder;

/**
 * handles conversion of bytearrays to accessory message and vice versa
 * @author jurgen
 *
 */
public class MessageHandler {

	private static byte[] tmpMessage;
	
	/**
	 * Creates a Accessorymessage object from a byte array
	 * @param message the byte to be convert to accessorymessage
	 * @throws InterruptedException
	 */
	public static void decode(byte[] message) throws InterruptedException {
		AccessoryMessage accessoryMessage = new AccessoryMessage(message);
		
		if(accessoryMessage.getTotalmessages() > 1) { //appending messages together
			if(accessoryMessage.getNumberofmessages() == 1) {
				tmpMessage = new byte[accessoryMessage.getTotalsize()];
				tmpMessage = accessoryMessage.getData();
			} else {
				System.arraycopy(accessoryMessage.getData(), 0, tmpMessage, tmpMessage.length, accessoryMessage.getData().length);
			}
			
			if(accessoryMessage.getNumberofmessages() == accessoryMessage.getTotalmessages()) {
				AccessoryBridge.messages.add(accessoryMessage);
			}
			
		} else { //no appending this time
			AccessoryBridge.messages.add(accessoryMessage);
		}
	}
	
	/**
	 * creates a accessorymessage array of bytes  
	 * 
	 * @param message bytearray of the bytes
	 * @param unique messageid
	 * @param type of the accesssorymessage(DBUS, KEEPALIVE, OTHER, BULK)
	 * @return
	 * @throws IOException
	 */
	public static byte[] encode(byte[] message, int id, AccessoryMessage.MessageType type) throws IOException {
		if(message.length > Config.MESSAGEMAX) { //segmented message are not handled in 
			return null;
		} else {
			 return new AccessoryMessage(message, type, id, 1, message.length, 1).tobytes(); //9 to unique id
		}
	}
	
	public static byte[] encode(byte[] message, AccessoryMessage.MessageType type) throws IOException {
		if(message.length > Config.MESSAGEMAX) { //segmented message are not handled in 
			return null;
		} else {
			 return new AccessoryMessage(message, type, Generateid(), 1, message.length, 1).tobytes(); //9 to unique id
		}
	}
	
	/*
	 * get the lastes id
	 */
	public static int Generateid() {
		return SystemHolder.mId++;
	}
}
