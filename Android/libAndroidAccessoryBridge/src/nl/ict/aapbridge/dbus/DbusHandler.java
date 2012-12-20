package nl.ict.aapbridge.dbus;

import android.os.Handler;

public abstract class DbusHandler extends Handler{
	
	/**
	 * 
	 * The different MessageTypes subclasses of this Handler will receive.
	 * 
	 * The ordinal value of these enums is stored in the Message 'what' variable.
	 *
	 */
	enum MessageTypes{
		DbusMethods,
		DbusSignals
	}
}
