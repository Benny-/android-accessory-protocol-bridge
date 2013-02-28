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
import android.os.Looper;
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
	
	private boolean introspecting = false;
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
	private DbusHandler introSpectionHandler;
	
	private class IntroSpectionDbusHandler extends DbusHandler {
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
			else
			{
				synchronized (IntroSpector.this)
				{
					introspecting = false;
					IntroSpector.this.notifyAll();
				}
			}
		}
	};
	private Looper busnameHandlerLoop;
	private SyncDbusHandler busnameHandler;
	private ObjectPathHandler objectPathHandler;
	
	private class DbusIntrospectionContentHandler extends DefaultHandler {
		
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
		DbusInterface dbusInterface = null;
		String member = null;
		
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
					objectPath = new ObjectPath( nodeName.equals("")?"/":nodeName );
				
				dbusInterface = new DbusInterface(interfaceName);
				objectPath.addInterface(dbusInterface);
			}
			
			if(localName.equals("method"))
			{
				member = attributes.getValue("name");
			}
			
			if(localName.equals("signal"))
			{
				member = attributes.getValue("name");
			}
			
			if(localName.equals("property"))
			{
				member = attributes.getValue("name");
			}
			
			// The arg tag can appear on methods and signals.
			if(localName.equals("arg"))
			{
				member = member + " " + attributes.getValue("name") + " " + attributes.getValue("type") + " " + attributes.getValue("direction");
			}
		}
		
		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if (localName.equals("node")) {
				depth--;
				
				if(objectPath != null && !objectPath.isEmpty())
				{
					Message.obtain(objectPathHandler, 0, objectPath).sendToTarget();
				}
			}
			
			if(localName.equals("method"))
			{
				dbusInterface.addMethod(member);
			}
			
			if(localName.equals("signal"))
			{
				dbusInterface.addSignal(member);
			}
			
			if(localName.equals("property"))
			{
				dbusInterface.addProperty(member);
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
	
	abstract public static class ObjectPathHandler extends Handler
	{
		@Override
		abstract public void handleMessage(Message msg);
	}
	
	static final ObjectPathHandler nullHandler = new ObjectPathHandler() {
		@Override
		public void handleMessage(Message msg) {
			// This handler does nothing at all.
		}
	};
	
	/**
	 * <p>Use this constructor if you are not interested in any introspection data,
	 * only in the busnames.</p>
	 * 
	 * <p>The function {@link #startIntrospection(String)} should not be called on
	 * a IntroSpector created this way.</p>
	 * 
	 * @param bridge
	 * @throws IOException
	 * @throws ServiceRequestException
	 */
	public IntroSpector(AccessoryBridge bridge) throws IOException, ServiceRequestException
	{
		this(bridge, nullHandler);
	}
	
	public IntroSpector(AccessoryBridge bridge, ObjectPathHandler objectPathHandler) throws IOException, ServiceRequestException {
		if(objectPathHandler == null)
			throw new NullPointerException("objectPathHandler may not be null");
		
		// Here we start all the handlers in there own event loops.
		new Thread(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				synchronized (IntroSpector.this) {
					busnameHandlerLoop = Looper.myLooper();
					busnameHandler = new SyncDbusHandler();
					introSpectionHandler = new IntroSpectionDbusHandler();
					IntroSpector.this.notifyAll();
				}
				Looper.loop();
			}
		}).start();
		
		synchronized (this) {
			while(introSpectionHandler == null)
			{
				try {
					wait();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
		
		this.objectPathHandler = objectPathHandler;
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
		if(busname == null)
			throw new NullPointerException("busname may not be null");
		
		this.introspecting = true;
		this.busname = busname;
		this.nodeName = "";
		objectPathsToIntrospect.add(nodeName);
		introSpectionhGetter.methodCall(
				busname,
				"/",
				"org.freedesktop.DBus.Introspectable",
				"Introspect");
	}
	
	public boolean introspecting()
	{
		return introspecting;
	}
	
	/**
	 * Blocks until the introspection is done.
	 * 
	 * @throws InterruptedException 
	 */
	synchronized public void waitForIntrospection() throws InterruptedException
	{
		while(introspecting())
			wait();
	}
	
	@Override
	public void close() throws IOException {
		busnameHandlerLoop.quit();
		introSpectionhGetter.close();
		busNameGetter.close();
	}

}
