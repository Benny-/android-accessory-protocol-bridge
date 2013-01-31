package nl.ict.aapbridge.dbus;

/**
 * <p>All subclasses MUST implement the constructor(String). Subclasses are fetched from d-bus errorname strings using reflection.</p>
 */
public class RemotePayloadException extends RemoteException{

	private static final long serialVersionUID = 6421737925700715692L;
	
	public RemotePayloadException(String type, String errString) {
		super(type, errString);
	}
	
}
