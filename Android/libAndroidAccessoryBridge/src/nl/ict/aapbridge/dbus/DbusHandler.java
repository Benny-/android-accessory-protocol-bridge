package nl.ict.aapbridge.dbus;

import java.util.LinkedList;

import nl.ict.aapbridge.dbus.message.DbusMessage;

import android.os.Handler;
import android.os.Message;

/**
 * The DbusHandler class provides a message based interface to receive dbus messages in a asynchronous way to a event loop.
 * 
 * You should inherit this class and override the {@link Handler#handleMessage(Message)} function to receive d-bus responses.
 * 
 * @see MessageTypes For possible "what" values in the received messages
 * @see Dbus
 * @see DbusSignals
 */
public abstract class DbusHandler extends Handler{
	
	/**
	 * 
	 * The different MessageTypes subclasses of this Handler will receive.
	 * 
	 * The ordinal value of these enums is stored in the Message 'what' variable.
	 *
	 */
	public enum MessageTypes{
		DbusMethods,
		DbusSignals
	}
	
	/**
	 * <p>Override this function to receive d-bus responses and d-bus signals.</p>
	 * 
	 * <p>{@link Message#arg1} is a value from {@link MessageTypes}, indicating what kind of dbus message it is.</p>
	 * <p>{@link Message#arg2} will be the same as the return value from {@link Dbus#methodCall(String, String, String, String, Object...)}, this value is undefined for signals.</p>
	 * <p>{@link Message#obj} is a {@link DbusMessage}, you should cast it to a DbusMessage.</p>
	 */
	@Override
	abstract public void handleMessage(Message msg);
}
