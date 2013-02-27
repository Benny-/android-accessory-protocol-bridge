package nl.ict.aapbridge.dbus.introspection;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Xml;

import nl.ict.aapbridge.bridge.AccessoryBridge;
import nl.ict.aapbridge.bridge.ServiceRequestException;
import nl.ict.aapbridge.dbus.DbusHandler;
import nl.ict.aapbridge.dbus.DbusMethods;
import nl.ict.aapbridge.dbus.RemoteDbusException;
import nl.ict.aapbridge.dbus.RemotePayloadException;
import nl.ict.aapbridge.dbus.message.DbusMessage;
import nl.ict.aapbridge.dbus.message.types.DbusArray;
import static nl.ict.aapbridge.TAG.TAG;

public class IntroSpector implements Closeable{
	
	// The following 3 variables are used to store data between the recursive introspection calls
	private String busname;
	/**
	 * The current nodename (objectpath). This changes as we recursively introspect all objects.
	 */
	private String nodeName;
	private List<String> objectPathsToIntrospect = new ArrayList<String>();
	
	private DbusMethods introSpectionhGetter;
	private DbusMethods busNameGetter;
	/**
	 * We are doing only one introspection call at a time. This might be sped up
	 * if we do them concurrently.
	 * 
	 * The response does not contain the original node (or full object path). So we
	 * need to keep track of it. One will need to keep this in mind if introspection
	 * needs to happen concurrently.
	 */
	private DbusHandler introSpectionHandler = new DbusHandler() {
		@Override
		public void handleMessage(Message msg) {
			DbusMessage dbusMessage = (DbusMessage) msg.obj;
			try {
				String xml_str = dbusMessage.getValues()[0].toString();
				Log.v(TAG, "Got xml: "+xml_str);
				Xml.parse(xml_str, new DbusIntrospectionContentHandler() );
			} catch (Exception e) {
				Log.e(TAG, "", e);
			}
			objectPathsToIntrospect.remove(nodeName);
			if(!objectPathsToIntrospect.isEmpty())
			{
				try {
					nodeName = objectPathsToIntrospect.get(0);
					Log.v(TAG, "Recursive async introspection on "+nodeName);
					introSpectionhGetter.methodCall(
							busname,
							nodeName,
							"org.freedesktop.DBus.Introspectable",
							"Introspect");
				} catch (IOException e) {
					Log.e(TAG, "", e);
				}
			}
		}
	};
	private SyncDbusHandler busnameHandler = new SyncDbusHandler();
	private ObjectPathHandler objectPathHandler;
	
	class DbusIntrospectionContentHandler extends DefaultHandler {
		
		/**
		 * There are only 2 node depths.
		 * 
		 * 1 is the root node. 2 is a subnode.
		 */
		int depth = 0;
		
		/**
		 * This variable will only be used if the node contains a interface.
		 */
		ObjectPath objectPath = null;
		
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if (localName.equals("node")) {
				depth++;
				
				if(depth == 2)
				{
					String subnode = attributes.getValue("name");
					Log.v(TAG, "Found subnode: "+subnode);
					String fullPathSubnode = nodeName+"/"+subnode;
					objectPathsToIntrospect.add(fullPathSubnode);
					Log.v(TAG, "Added "+fullPathSubnode+" for recursive introspection");
				}
			}
			
			if(localName.equals("interface"))
			{
				assert(depth == 1);
				String interfaceName = attributes.getValue("name");
				Log.v(TAG, "Found interface: "+interfaceName);
				
				if (objectPath == null)
					objectPath = new ObjectPath(nodeName);
				
				objectPath.addInterface(new DbusInterface(interfaceName));
			}
		}
		
		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if (localName.equals("node")) {
				depth--;
			}
		}
	}
	
	static class SyncDbusHandler extends DbusHandler{
		private Queue<DbusMessage> messages = new LinkedList<DbusMessage>();
		private Semaphore lock = new Semaphore(0, true);
		
		@Override
		public void handleMessage(Message msg) {
			messages.add((DbusMessage) msg.obj);
			lock.release();
		}
		
		public DbusMessage getDbusMessage()
		{
			try {
				lock.acquire();
			} catch (InterruptedException e) {
				Log.e(TAG, "", e);
			}
			return messages.poll();
		}
	}
	
	abstract static class ObjectPathHandler extends Handler
	{
		@Override
		abstract public void handleMessage(Message msg);
	}
	
	public IntroSpector(AccessoryBridge bridge, ObjectPathHandler handler) throws IOException, ServiceRequestException {
		objectPathHandler = handler;
		introSpectionhGetter = bridge.createDbus(introSpectionHandler);
		busNameGetter = bridge.createDbus(busnameHandler);
	}
	
	synchronized public DbusArray getBusnames() throws IOException, RemotePayloadException, RemoteDbusException
	{
		busNameGetter.methodCall("org.freedesktop.DBus", "/org/freedesktop/DBus", "org.freedesktop.DBus", "ListNames");
		DbusMessage dbusMessage = busnameHandler.getDbusMessage();
		DbusArray dbusArray = (DbusArray) dbusMessage.getValues()[0];
		return dbusArray;
	}
	
	/**
	 * <p>This object can only introspect one busname at a time. If you
	 * need to introspect more busnames at the same time, please create another
	 * IntroSpector object.</p>
	 * 
	 * @param busname
	 * @throws IOException 
	 */
	synchronized public void startIntrospection(String busname) throws IOException
	{
		this.busname = busname;
		this.nodeName = "";
		objectPathsToIntrospect.add(nodeName);
		introSpectionhGetter.methodCall(
				busname,
				"/",
				"org.freedesktop.DBus.Introspectable",
				"Introspect");
	}
	
	@Override
	public void close() throws IOException {
		introSpectionhGetter.close();
		busNameGetter.close();
	}

}
