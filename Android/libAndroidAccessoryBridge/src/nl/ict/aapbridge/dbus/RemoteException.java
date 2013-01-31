package nl.ict.aapbridge.dbus;

abstract public class RemoteException extends Exception {
	
	private static final long serialVersionUID = -3840812648892065819L;
	
	private final String trueType;
	
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
	
	/**
	 * <p>Return the true class-name if a class could not be found using reflection. This fails if
	 * there does not exist a class with this class-name</p>
	 * 
	 * <p>Return the same class-name as the child class if the correct class could be found using reflection</p>
	 * 
	 * @return A fully qualified class-name
	 */
	public String getTrueType()
	{
		return trueType;
	}
}
