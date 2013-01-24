package nl.ict.aapbridgesample;

import java.io.IOException;
import java.util.Set;

import nl.ict.aapbridge.aap.BTConnection;
import nl.ict.aapbridge.bridge.AccessoryBridge;

import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass.Device.Major;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class BluetoothAccessoryActivity extends ListActivity{
	
	public static final String TAG = BluetoothAccessoryActivity.class.getSimpleName();
	
	private ArrayAdapter<btDeviceContainerView> btArrayAdapter;
	
	class btDeviceContainerView{
		private BluetoothDevice device;
		
		public btDeviceContainerView(BluetoothDevice device) {
			if(device == null)
				throw new NullPointerException();
			this.device = device;
		}
		
		@Override
		public String toString() {
			return device.getAddress() + " " + device.getName() + " Class: " + device.getBluetoothClass();
		}
		
		public BluetoothDevice getBtDevice()
		{
			return device;
		}
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connector);
        
	    btArrayAdapter = new ArrayAdapter<btDeviceContainerView>(this, android.R.layout.simple_list_item_1);
	    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	  	Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
	  	for(BluetoothDevice device : pairedDevices)
	  	{
	  		btArrayAdapter.add(new btDeviceContainerView(device));
	  	}
	  	setListAdapter(btArrayAdapter);
    }
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Log.v(TAG, "Clicked on item "+position+" : "+btArrayAdapter.getItem(position));
		
		try {
			ServiceSelectionActivity.aapbridge = new AccessoryBridge(
					new BTConnection(btArrayAdapter.getItem(position).getBtDevice()));
			Intent intent = new Intent(getApplicationContext(), ServiceSelectionActivity.class);
			startActivity(intent);
		} catch (IOException e) {
			Log.e(TAG, "", e);
			Toast.makeText(this, "Connection failed: "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
		}
	}
}
