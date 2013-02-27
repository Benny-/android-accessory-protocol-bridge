package nl.ict.aapbridge.dbus;

import java.util.LinkedList;

import nl.ict.aapbridge.dbus.message.DbusMessage;

import android.os.Handler;
import android.os.Message;

/**
 * The DbusHandler class provides a message based interface to receive dbus messages in a asynchronous way to a event loop.
 * 
 * You should inherit this class and override the {@link #handleMessage(Message)} function to receive d-bus responses.
 * 
 * @see MessageTypes
 * @see DbusMethods
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
	 * <p>Sometime you wish to do a d-bus call, but you are not interested if it fails or succeeds.</p>
	 * 
	 * <p>Our engineers have anticipated this and created the "NullDbusHandler".
	 * It is a one of a kind d-bus handler which throws the received d-bus *woosh*
	 * out of the computer memories. This allows you to have 5 lines of code less in
	 * your code. Amazing!</p>
	 */
	static DbusHandler NullDbusHandler = new DbusHandler() {
		@Override
		public void handleMessage(Message msg) {
			// The nullHandler does nothing.
		}
	};
	
	/**
	 * <p>Override this function to receive d-bus responses and d-bus signals.</p>
	 * 
	 * <p>The following fields are set in the received Message parameter:</p>
	 * <ul>
	 * <li>int {@link Message#what} is a value from {@link MessageTypes}, indicating what kind of dbus message
	 * this is (Return value from d-bus methods or a d-bus signals).</li>
	 * 
	 * <li>int {@link Message#arg1} will be the same as the return value from
	 * {@link DbusMethods#methodCall(String, String, String, String, Object...)}, this value is undefined for signals.</li>
	 * 
	 * <li>int {@link Message#arg2} is always zero.</li>
	 * 
	 * <li>Object {@link Message#obj} is a {@link DbusMessage}, you should cast it to a DbusMessage and extract
	 * the values using {@link DbusMessage#getValues()}.</li>
	 * </ul>
	 * 
	 */
	@Override
	abstract public void handleMessage(Message msg);
}
