package nl.ict.aapbridge.dbus;

public class RemoteException extends Exception {
	
	private final String trueType;
	
	public RemoteException() {
		trueType = null;
	}
	
	public RemoteException(String type) {
		trueType = type;
	}
	
	public RemoteException(String type, String errString) {
		super(errString);
		trueType = type;
	}
	
	public String getTrueType()
	{
		return trueType;
	}
}
