package nl.ict.dfinger;

import java.io.IOException;

import nl.ict.aapbridge.aap.UsbConnection;
import nl.ict.aapbridge.bridge.AccessoryBridge;
import nl.ict.aapbridge.dbus.Dbus;
import nl.ict.aapbridge.dbus.DbusHandler;
import nl.ict.aapbridge.dbus.RemoteException;
import nl.ict.aapbridge.dbus.message.DbusMessage;
import nl.ict.aapbridge.dbus.message.types.DbusArray;
import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class BusNameListActivity extends ListActivity {
	
	public static final String TAG = BusNameListActivity.class.getSimpleName();
	public static AccessoryBridge staticBridge;
	
	private AccessoryBridge bridge;
	private Dbus dbus;
	private BusnameAdapter busnameadapter;
	private DbusHandler dbushandler = new DbusHandler() {
		public void handleMessage(android.os.Message msg)
		{
			DbusMessage dbusmsg = (DbusMessage) msg.obj;
			try {
				Object[] objects = dbusmsg.getValues();
				DbusArray dbusArray = (DbusArray) objects[0];
				busnameadapter.setList(dbusArray);
			} catch (RemoteException e) {
				String error_msg = "Something went wrong when trying to get busnames";
				Log.e(TAG, error_msg, e);
				Toast.makeText(BusNameListActivity.this, error_msg, Toast.LENGTH_LONG).show();
			}
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.busnamelistactivity);
		
		busnameadapter = new BusnameAdapter(this);
		setListAdapter(busnameadapter);
		
		if(staticBridge == null || !staticBridge.isOpen())
		{
			try {
				bridge = new AccessoryBridge(UsbConnection.easyConnect(getApplicationContext()));
			} catch (IOException e) {
				String msg = "Could not setup aapbridge: "+e.getLocalizedMessage();
				Log.e(TAG, msg, e);
				Toast.makeText(BusNameListActivity.this, msg, Toast.LENGTH_SHORT).show();
				finish();
			}
		}
		else
		{
			bridge = staticBridge;
		}
		
		try {
			dbus = bridge.createDbus(dbushandler);
			dbus.methodCall("org.freedesktop.DBus", "/org/freedesktop/DBus", "org.freedesktop.DBus", "ListNames");
		} catch (Exception e) {
			String msg = "Could not setup aapbridge: "+e.getLocalizedMessage();
			Log.e(TAG, msg, e);
			Toast.makeText(BusNameListActivity.this, msg, Toast.LENGTH_SHORT).show();
			finish();
		}
	}
	
	@Override
	protected void onDestroy() {
		try {
			dbus.close();
		} catch (Exception e) {
			// Meh..
		}
		super.onDestroy();
	}

}
