package nl.ict.aapbridge.dbus.introspection;

import java.util.ArrayList;
import java.util.List;

public class ObjectPath {
	
	private String name;
	private List<String> signals = new ArrayList<String>();
	private List<DbusInterface> interfaces = new ArrayList<DbusInterface>();
	
	public ObjectPath(String nodeName) {
		name = nodeName;
	}
	
	public String getName()
	{
		return name;
	}
	
	void addInterface(DbusInterface dbusInterface)
	{
		interfaces.add(dbusInterface);
	}

}
