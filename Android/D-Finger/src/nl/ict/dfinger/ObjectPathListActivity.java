package nl.ict.dfinger;

import java.io.IOException;

import nl.ict.aapbridge.aap.UsbConnection;
import nl.ict.aapbridge.bridge.AccessoryBridge;
import nl.ict.aapbridge.bridge.ServiceRequestException;
import nl.ict.aapbridge.dbus.DbusMethods;
import nl.ict.aapbridge.dbus.DbusHandler;
import nl.ict.aapbridge.dbus.introspection.IntroSpector;
import nl.ict.aapbridge.dbus.introspection.IntroSpector.ObjectPathHandler;
import nl.ict.aapbridge.dbus.introspection.ObjectPath;
import android.os.Bundle;
import android.os.Message;
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
	private IntroSpector introspector;
	private ObjectPathAdapter objectpathadapter;
	private ObjectPathHandler objectPathHandler = new ObjectPathHandler() {
		@Override
		public void handleMessage(Message msg) {
			objectpathadapter.add((ObjectPath) msg.obj);
		}
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
			introspector = new IntroSpector(bridge, objectPathHandler);
			introspector.startIntrospection(busname);
		} catch (Exception e) {
			String msg = "Could not start introspection: "+e.getLocalizedMessage();
			Log.e(TAG, msg, e);
			Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
			finish();
		}
	}
	
	@Override
	protected void onDestroy() {
		try {
			introspector.close();
		} catch (Exception e) {
			// Meh.
		}
		super.onDestroy();
	}
}
