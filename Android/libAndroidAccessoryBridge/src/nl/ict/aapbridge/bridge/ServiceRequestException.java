package nl.ict.aapbridge.bridge;

public class ServiceRequestException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8004337640066585648L;
	
	public ServiceRequestException() {
		// TODO Auto-generated constructor stub
	}
	
	public ServiceRequestException(String detailMessage)
	{
		super(detailMessage);
	}
	
	public ServiceRequestException(int errorcode)
	{
		super("Remote host returned error code "+errorcode);
	}
}
