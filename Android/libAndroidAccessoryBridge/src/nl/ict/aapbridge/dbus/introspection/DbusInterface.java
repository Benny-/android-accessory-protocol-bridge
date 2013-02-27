package nl.ict.aapbridge.dbus.introspection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DbusInterface {
	
	private String name;
	private ArrayList<String> methods = new ArrayList<String>();
	private ArrayList<String> signals = new ArrayList<String>();
	private ArrayList<String> properties = new ArrayList<String>();
	
	public DbusInterface(String interfaceName) {
		name = interfaceName;
	}
	
	public String getName()
	{
		return name;
	}
	
	void addMethod(String method)
	{
		methods.add(method);
	}
	
	/**
	 * Return a read-only list
	 */
	public List<String> getMethods()
	{
		return Collections.unmodifiableList(methods);
	}
	
	void addSignal(String signal)
	{
		signals.add(signal);
	}
	
	/**
	 * Return a read-only list
	 */
	public List<String> getSignals()
	{
		return Collections.unmodifiableList(signals);
	}
	
	void addProperty(String property) {
		properties.add(property);
	}
	
	/**
	 * Return a read-only list
	 */
	public List<String> getProperties()
	{
		return Collections.unmodifiableList(properties);
	}
	
	void compact()
	{
		methods.trimToSize();
		signals.trimToSize();
		properties.trimToSize();
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(DbusInterface.class.getSimpleName() + " name: " + name);
		for(String method : methods)
			sb.append("\n    Method: "+method);
		for(String signal : signals)
			sb.append("\n    Signal: "+signal);
		for(String property : properties)
			sb.append("\n    Property: "+property);
		return sb.toString();
	}
}
