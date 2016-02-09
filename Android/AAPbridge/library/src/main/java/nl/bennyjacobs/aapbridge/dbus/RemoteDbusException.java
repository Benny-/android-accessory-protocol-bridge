package nl.bennyjacobs.aapbridge.dbus;

/**
 * <p>All subclasses MUST implement the constructor(String). Subclasses are fetched from d-bus errorname strings using reflection.</p>
 */
public class RemoteDbusException extends RemoteException{

	private static final long serialVersionUID = -3165452842240461227L;
	
	public RemoteDbusException(String type, String errString) {
		super(type, errString);
	}
}
