package nl.bennyjacobs.remotedbus;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import nl.bennyjacobs.aapbridge.bridge.AccessoryBridge;
import nl.bennyjacobs.aapbridge.dbus.DbusHandler;
import nl.bennyjacobs.aapbridge.dbus.DbusSignals;
import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class DbusSignalActivity extends Activity {
	
	public static final String TAG = DbusSignalActivity.class.getName();
	public static final Charset utf8 = Charset.forName("UTF-8");
	
	private AccessoryBridge aapbridge;
	
    private EditText textfield_busname;
    private EditText textfield_objectpath;
    private EditText textfield_interface;
    private EditText textfield_membername;
	
    private Button button_addwatch;
    private Button button_removewatch;
    
    private DbusHandler dbus_handler = new DbusHandler() {
    	@Override
    	public void handleMessage(Message msg) {
    		Log.v(TAG, "Received some signal response: "+msg.toString());
    		// msg.recycle();
    	}
	};
	
	private List<DbusSignals> signalListeners = new ArrayList<DbusSignals>();
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signal_filter);
        
        textfield_busname = (EditText)findViewById(R.id.Busname);
        textfield_objectpath = (EditText) findViewById(R.id.Objectpath);
        textfield_interface = (EditText) findViewById(R.id.Interface);
        textfield_membername = (EditText) findViewById(R.id.Membername);
        
        button_addwatch = (Button)findViewById(R.id.button_addwatch);
        button_removewatch = (Button)findViewById(R.id.button_removewatch);
        
        aapbridge = ServiceSelectionActivity.aapbridge;
        
        button_addwatch.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try {
					DbusSignals signalListener = aapbridge.createDbusSignal(
							dbus_handler,
							textfield_busname.getText().toString(),
							textfield_objectpath.getText().toString(),
							textfield_interface.getText().toString(),
							textfield_membername.getText().toString());
					signalListeners.add(signalListener);
				} catch (Exception e) {
					Log.e(TAG, "", e);
					Toast.makeText(DbusSignalActivity.this, "Accessory not connected", Toast.LENGTH_SHORT).show();
		        	finish();
				}
			}
		});
        
        button_removewatch.setEnabled(false);
        button_removewatch.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
//				try {
//					DbusSignals dbusSignals = new DbusSignals(aapbridge);
//					dbusSignals.removeWatch(
//							textfield_busname.getText().toString(),
//							textfield_objectpath.getText().toString(),
//							textfield_interface.getText().toString(),
//							textfield_membername.getText().toString());
//				} catch (Exception e) {
//					Log.e(TAG, "", e);
//				}
			}
		});
    }
    
    @Override
    protected void onDestroy() {
    	for(DbusSignals listener : signalListeners)
    	{
    		try {
				listener.close();
			} catch (IOException e) {
				Log.e(TAG, "", e);
			}
    	}
    	super.onDestroy();
    }
}
