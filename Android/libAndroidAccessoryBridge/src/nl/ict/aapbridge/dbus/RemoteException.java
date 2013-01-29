package nl.ict.aapbridge.dbus;

public class RemoteException extends Exception {
	
	private static final long serialVersionUID = -3840812648892065819L;
	
	private final String trueType;
	
	public RemoteException() {
		trueType = null;
	}
	
	public RemoteException(String type) {
		trueType = type;
	}
	
	/**
	 * This function has no javadocs
	 * 
	 * @param type
	 * @param errString
	 */
	public RemoteException(String type, String errString) {
		super(errString);
		trueType = type;
	}
	
	public String getTrueType()
	{
		return trueType;
	}
}
