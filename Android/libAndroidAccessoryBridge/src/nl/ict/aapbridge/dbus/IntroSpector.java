package nl.ict.aapbridge.dbus;

import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Xml;

import nl.ict.aapbridge.bridge.AccessoryBridge;
import nl.ict.aapbridge.bridge.ServiceRequestException;
import nl.ict.aapbridge.dbus.message.DbusMessage;
import nl.ict.aapbridge.dbus.message.types.DbusArray;
import static nl.ict.aapbridge.TAG.TAG;

public class IntroSpector implements Closeable{
	
	private DbusMethods introSpectionhGetter;
	private DbusMethods busNameGetter;
	private DbusHandler introSpectionHandler = new DbusHandler() {
		@Override
		public void handleMessage(Message msg) {
			DbusMessage dbusMessage = (DbusMessage) msg.obj;
			try {
				String xml_str = dbusMessage.getValues()[0].toString();
				Log.v(TAG, "Got xml: "+xml_str);
				Xml.parse(xml_str, null );
			} catch (Exception e) {
				Log.e(TAG, "", e);
			}
		}
	};
	private SyncDbusHandler busnameHandler = new SyncDbusHandler();
	private ObjectPathHandler objectPathHandler;
	
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
