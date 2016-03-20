package nl.bennyjacobs.aapbridge.bridge;

/**
 * <p>Exception will be thrown from the {@link AccessoryBridge} if a service could not be started</p>
 * 
 * <p>The reason is most likely on the accessory's part.</p>
 */
public class ServiceRequestException extends Exception
{

	private static final long serialVersionUID = -8004337640066585648L;
	
	public ServiceRequestException() {
		super();
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
