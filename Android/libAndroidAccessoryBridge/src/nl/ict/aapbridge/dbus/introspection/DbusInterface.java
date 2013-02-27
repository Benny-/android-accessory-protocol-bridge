package nl.ict.aapbridge.dbus.introspection;

public class DbusInterface {
	
	private String name;
	
	public DbusInterface(String interfaceName) {
		name = interfaceName;
	}
	
	public String getName()
	{
		return name;
	}
}
