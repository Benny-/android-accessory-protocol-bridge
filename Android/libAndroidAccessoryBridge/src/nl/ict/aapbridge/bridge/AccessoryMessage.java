package nl.ict.aapbridge.bridge;

import java.io.IOException;
import java.nio.charset.Charset;

import android.provider.ContactsContract.Contacts.Data;
import android.util.Log;

import nl.ict.aapbridge.helper.ByteHelper;
import nl.ict.aapbridge.helper.ExtByteArrayOutputStream;
import nl.ict.aapbridge.helper.IntegerHelper;


public class AccessoryMessage {
	
	public static final String TAG = AccessoryMessage.class.getName();
	
	public enum MessageType {
		BULK, DBUS, KEEPALIVE, SIGNAL, OTHER
	};

	private byte[] mData;
	private MessageType mType;
	private int mId;
	private int mNumberofmessages;
	private int mTotalsize;
	private int mTotalmessages;
	
	public AccessoryMessage(byte[] data, MessageType type, int id,
			int numberofmessages, int totalsize, int totalmessages) {
		this.mData = data;
		this.mType = type;
		this.mId = id;
		this.mNumberofmessages = numberofmessages;
		this.mTotalsize = totalsize;
		this.mTotalmessages = totalmessages;
	}

	public AccessoryMessage(byte[] message) {
		parsefrom(message);
	}

	public int getId() {
		return mId;
	}

	public void setId(int id) {
		this.mId = id;
	}

	public int getNumberofmessages() {
		return mNumberofmessages;
	}

	public void setNumberofmessages(int numberofmessages) {
		this.mNumberofmessages = numberofmessages;
	}

	public int getTotalsize() {
		return mTotalsize;
	}

	public void setTotalsize(int totalsize) {
		this.mTotalsize = totalsize;
	}

	public int getTotalmessages() {
		return mTotalmessages;
	}

	public void setTotalmessages(int totalmessages) {
		this.mTotalmessages = totalmessages;
	}

	public MessageType getType() {
		return mType;
	}

	public void setType(MessageType type) {
		this.mType = type;
	}

	public byte[] getData() {
		return mData;
	}

	public void setData(byte[] data) {
		this.mData = data;
	}

	@SuppressWarnings("static-access")
	private void parsefrom(byte[] buffer) {
		final int INTSIZE = 4;
		int byteLocationpointer = 0;

		//convert first 4 byte to int
		byte[] tmpbuffer = new byte[INTSIZE];
		System.arraycopy(buffer, 0, tmpbuffer, 0, INTSIZE);
		this.mId = IntegerHelper.toInt(tmpbuffer);

		byteLocationpointer += INTSIZE;

		tmpbuffer = new byte[INTSIZE];
		System.arraycopy(buffer, byteLocationpointer, tmpbuffer, 0, INTSIZE); // +4
		tmpbuffer = ByteHelper.reverse(tmpbuffer);
		this.mNumberofmessages = IntegerHelper.toInt(tmpbuffer);

		byteLocationpointer += INTSIZE; 

		tmpbuffer = new byte[INTSIZE];
		System.arraycopy(buffer, byteLocationpointer, tmpbuffer, 0, INTSIZE); // +4
		tmpbuffer = ByteHelper.reverse(tmpbuffer);
		this.mTotalmessages = IntegerHelper.toInt(tmpbuffer);

		byteLocationpointer += INTSIZE;

		tmpbuffer = new byte[INTSIZE];
		System.arraycopy(buffer, byteLocationpointer, tmpbuffer, 0, INTSIZE); // +4
		tmpbuffer = ByteHelper.reverse(tmpbuffer);
		this.mTotalsize = IntegerHelper.toInt(tmpbuffer);

		byteLocationpointer += INTSIZE;

		tmpbuffer = new byte[INTSIZE];
		System.arraycopy(buffer, byteLocationpointer, tmpbuffer, 0, INTSIZE);
		this.mType = mType.values()[tmpbuffer[0]];

		byteLocationpointer += INTSIZE;
		
		this.mData = new byte[mTotalsize];
		System.arraycopy(buffer, byteLocationpointer, this.mData, 0, this.mTotalsize);
	}

	public byte[] tobytes() {
		ExtByteArrayOutputStream o = new ExtByteArrayOutputStream();
		try {
			o.write(mId);
			o.write(mNumberofmessages);
			o.write(mTotalmessages);
			o.write(mTotalsize);
			o.write(mType);
			o.write(mData);
			o.close();
		} catch (IOException e) {
			Log.wtf(TAG, "This should never happen", e);
		}
		return o.toByteArray();
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for(int i =0; i<mTotalsize; i++)
		{
			sb.append(Integer.toHexString(getData()[i]));
		}
		return sb.toString();
	}
}
