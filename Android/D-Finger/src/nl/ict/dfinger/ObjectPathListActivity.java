package nl.ict.dfinger;

import java.io.IOException;

import nl.ict.aapbridge.aap.UsbConnection;
import nl.ict.aapbridge.bridge.AccessoryBridge;
import nl.ict.aapbridge.bridge.ServiceRequestException;
import nl.ict.aapbridge.dbus.Dbus;
import nl.ict.aapbridge.dbus.DbusHandler;
import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

public class ObjectPathListActivity extends ListActivity
{
	public static final String TAG = ObjectPathListActivity.class.getSimpleName();
	public static final String EXTRA_BUSNAME = "EXTRA_BUSNAME";
	
	private String busname;
	private AccessoryBridge bridge;
	private Dbus dbus;
	private ObjectPathAdapter objectpathadapter;
	private DbusHandler dbushandler = new DbusHandler() {
		public void handleMessage(android.os.Message msg)
		{
			Log.d(TAG, msg.toString());
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_object_path_list);
		
		objectpathadapter = new ObjectPathAdapter(this);
		setListAdapter(objectpathadapter);
		
		busname = getIntent().getStringExtra(EXTRA_BUSNAME);
		
		if(BusNameListActivity.staticBridge == null || !BusNameListActivity.staticBridge.isOpen())
		{
			String msg = "Could not start ObjectPath lister, accessory bridge down";
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
			finish();
		}
		else
		{
			bridge = BusNameListActivity.staticBridge;
		}
		
		try {
			dbus = bridge.createDbus(dbushandler);
			// dbus.methodCall(busname, "/", "org.freedesktop.DBus.ObjectManager", "GetManagedObjects");
			dbus.methodCall(busname, "/", "org.freedesktop.DBus.Introspectable", "Introspect");
		} catch (Exception e) {
			String msg = "Could not setup dbus service: "+e.getLocalizedMessage();
			Log.e(TAG, msg, e);
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
			finish();
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_object_path_list, menu);
		return true;
	}
	
	@Override
	protected void onDestroy() {
		try {
			dbus.close();
		} catch (Exception e) {
			// Meh.
		}
		super.onDestroy();
	}
}
