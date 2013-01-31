package nl.ict.aapbridge.dbus.message;

import nl.ict.aapbridge.dbus.RemoteDbusException;

/**
 * <p>Despite its derived class, this class is not actually a remote exception related
 * to d-bus. The bridge library simply decided to use exceptions to denote empty return values</p>
 */
public class NoValues extends RemoteDbusException{

	private static final long serialVersionUID = 699889449263355785L;

	public NoValues() {
		super(NoValues.class.getName(), "There are no values in this d-bus message");
	}

}
