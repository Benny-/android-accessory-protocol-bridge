package nl.ict.dfinger;

import java.io.IOException;

import nl.ict.aapbridge.aap.UsbConnection;
import nl.ict.aapbridge.bridge.AccessoryBridge;
import nl.ict.aapbridge.dbus.DbusMethods;
import nl.ict.aapbridge.dbus.DbusHandler;
import nl.ict.aapbridge.dbus.RemoteException;
import nl.ict.aapbridge.dbus.introspection.IntroSpector;
import nl.ict.aapbridge.dbus.message.DbusMessage;
import nl.ict.aapbridge.dbus.message.types.DbusArray;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class BusNameListActivity extends ListActivity {
	
	public static final String TAG = BusNameListActivity.class.getSimpleName();
	public static AccessoryBridge staticBridge;
	
	private AccessoryBridge bridge;
	private IntroSpector introSpector;
	private BusnameAdapter busnameadapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bus_name_list);
		
		busnameadapter = new BusnameAdapter(this);
		setListAdapter(busnameadapter);
		
		if(staticBridge == null || !staticBridge.isOpen())
		{
			try {
				bridge = new AccessoryBridge(UsbConnection.easyConnect(getApplicationContext()));
				staticBridge = bridge;
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
			introSpector = new IntroSpector(bridge);
			busnameadapter.setList(introSpector.getBusnames());
		} catch (Exception e) {
			String msg = "Could not setup introSpector: "+e.getLocalizedMessage();
			Log.e(TAG, msg, e);
			Toast.makeText(BusNameListActivity.this, msg, Toast.LENGTH_SHORT).show();
			finish();
		} finally {
			try {
				introSpector.close();
			} catch (IOException e) {
				String msg = "Could not close introSpector: "+e.getLocalizedMessage();
				Log.e(TAG, msg, e);
				Toast.makeText(BusNameListActivity.this, msg, Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(getApplicationContext(), ObjectPathListActivity.class);
		intent.putExtra(ObjectPathListActivity.EXTRA_BUSNAME, busnameadapter.getList().get(position).toString());
		startActivity(intent);
	}
	
	@Override
	protected void onDestroy() {
		try 
		{
			introSpector.close();
		} catch (Exception e)
		{
			// meh.
		}
		
		super.onDestroy();
	}
}
