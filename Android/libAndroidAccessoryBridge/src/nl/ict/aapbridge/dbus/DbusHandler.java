package nl.ict.aapbridge.dbus;

import android.os.Handler;

/**
 * The DbusHandler class provides a message based interface to
 * receive dbus messages in a asynchronous way to a event loop.
 * 
 * @see MessageTypes
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
}
