package nl.ict.aapbridge.dbus;


import java.io.IOException;
import java.util.ArrayList;

import nl.ict.aapbridge.bridge.AccessoryBridge;
import nl.ict.aapbridge.bridge.MessageHandler;
import nl.ict.aapbridge.bridge.AccessoryMessage.MessageType;
import nl.ict.aapbridge.helper.ExtByteArrayOutputStream;
import nl.ict.aapbridge.helper.IntegerHelper;


/**
 * 
 * @author jurgen
 *
 */

//@todo add broadcast listener
public class dbus {
	public void methodCall(String name, Integer... vars) throws Exception
	{  
		DbusMessage dMessage;
		if(vars.length > 4) {
			throw new Exception("Too manny arguments, not implemented yet");
		}

		dMessage = new DbusMessage(name);
		
		for(Integer var : vars) {
			dMessage.addVar(var);
		}
		
		//make the struct
		int methodcallid = MessageHandler.Generateid();

		AccessoryBridge.Write(dMessage.getbytes(), methodcallid ,MessageType.DBUS);
		
//		while(!found) {
//			//re
//			
//		}
		//@todo wait for reply
	}
	
	public static class DbusMessage {
		
		/**
		 * @return the mName
		 */
		public String getName() {
			return mName;
		}

		/**
		 * @param mName the mName to set
		 */
		public void setName(String mName) {
			this.mName = mName;
		}

		/**
		 * @return the mReturnValue
		 */
		public int getReturnValue() {
			return mReturnValue;
		}

		/**
		 * @param mReturnValue the mReturnValue to set
		 */
		public void setReturnValue(int mReturnValue) {
			this.mReturnValue = mReturnValue;
		}

		/**
		 * @return the mPrevId
		 */
		public int getPrevId() {
			return mPrevId;
		}

		/**
		 * @param mPrevId the mPrevId to set
		 */
		public void setPrevId(int mPrevId) {
			this.mPrevId = mPrevId;
		}

		private String mName;
		private int mReturnValue;
		private int mPrevId;
		
		ArrayList<Integer> vars = new ArrayList<Integer>();
		
		public DbusMessage(String name) {
			if(name.length() > 64) {
				throw new NumberFormatException("maximum length of the name argument is 64");
			}
			
			this.mName = name;
		}
		
		/**
		 * convert array of bytes to a dbusmesage
		 */
		public static DbusMessage parseFrom(byte[] buffer) {
			int byteLocationpointer = 0;
			
			byte[] tmpbuffer = new byte[64]; //64 counting name
			System.arraycopy(buffer, byteLocationpointer, tmpbuffer, 0, 64);
			DbusMessage message = new DbusMessage(new String(tmpbuffer));
			
			byteLocationpointer += 64;
			
			//todo add variables 
			
			byteLocationpointer += 8 * 4; //add four int values
			
			tmpbuffer = new byte[4];
			System.arraycopy(buffer, byteLocationpointer, tmpbuffer, 0, 4);
			message.setPrevId(IntegerHelper.toInt(tmpbuffer));
			
			tmpbuffer = new byte[4];
			System.arraycopy(buffer, byteLocationpointer, tmpbuffer, 0, 4);
			message.setReturnValue(IntegerHelper.toInt(tmpbuffer));
			
			return message;
		}
		
		public void addVar(Integer var) {
			vars.add(var);
		}
		
		/**
		 * convert the dbusmessage to array of bytes
		 * @return array of bytes
		 * @throws IOException
		 */
		public byte[] getbytes() throws IOException {
			ExtByteArrayOutputStream s = new ExtByteArrayOutputStream();
			//write name 
			s.write(mName.getBytes()); //buildfiller
			
			for (Object var : vars.toArray()) {
				int type = 1; //integer type
				s.write(type);
				s.write((int)((Integer)var));//convert from object to int
				s.close();
			}
			return mName.getBytes();
		}

	}
}