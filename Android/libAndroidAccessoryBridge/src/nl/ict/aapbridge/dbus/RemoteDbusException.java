package nl.ict.aapbridge.dbus;


public class RemoteDbusException extends RemoteException{

	private static final long serialVersionUID = -3165452842240461227L;
	
	public RemoteDbusException(String type, String errString) {
		super(type, errString);
	}
}
