package nl.ict.aapbridge.dbus;

import java.io.IOException;

/**
 * <p>This is a convenience class so you dont have to type the busname, objectpath, interfacename and methodname all over again</p>
 */
public class Method {
	
	private DbusMethods methods;
	private String busname;
	private String objectpath;
	private String interfaceName;
	private String methodName;
	
	public Method(DbusMethods methods, String busname, String objectpath, String interfacename, String method) {
		this.methods = methods;
		this.busname = busname;
		this.objectpath = objectpath;
		this.interfaceName = interfacename;
		this.methodName = method;
	}

	public int invoke(Object[] arguments) throws IOException
	{
		return methods.methodCall(busname, objectpath, interfaceName, methodName, arguments);
	}
}
