package nl.ict.aapbridge.dbus.introspection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ObjectPath {
	
	private String name;
	private ArrayList<DbusInterface> interfaces = new ArrayList<DbusInterface>();
	
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
	
	public List<DbusInterface> getInterfaces()
	{
		return Collections.unmodifiableList(interfaces);
	}

	public boolean isEmpty() {
		return interfaces.isEmpty();
	}
	
	void compact()
	{
		for(DbusInterface dbusInterface : interfaces)
		{
			dbusInterface.compact();
		}
		interfaces.trimToSize();
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(ObjectPath.class.getSimpleName());
		sb.append(" name: " +name+ " containing "+interfaces.size()+" interfaces");
		for(DbusInterface dbusInterface : interfaces)
		{
			sb.append("\n  "+dbusInterface.toString());
		}
		return sb.toString();
	}
}
