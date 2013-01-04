package nl.ict.aapbridgesample;

import java.nio.charset.Charset;

import nl.ict.aapbridge.bridge.AccessoryBridge;
import nl.ict.aapbridge.dbus.DbusSignals;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SignalFilterActivity extends Activity {
	
	public static final String TAG = SignalFilterActivity.class.getName();
	public static final Charset utf8 = Charset.forName("UTF-8");
	
	private AccessoryBridge aapbridge;
	
    private EditText textfield_busname;
    private EditText textfield_objectpath;
    private EditText textfield_interface;
    private EditText textfield_membername;
	
    private Button button_addwatch;
    private Button button_removewatch;
    
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
        
        aapbridge = AccessoryActionActivity.aapbridge;
        
        button_addwatch.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try {
					DbusSignals dbusSignals = new DbusSignals(aapbridge);
					dbusSignals.addWatch(
							textfield_busname.getText().toString(),
							textfield_objectpath.getText().toString(),
							textfield_interface.getText().toString(),
							textfield_membername.getText().toString());
				} catch (Exception e) {
					Log.e(TAG, "", e);
				}
			}
		});
        
        button_removewatch.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try {
					DbusSignals dbusSignals = new DbusSignals(aapbridge);
					dbusSignals.removeWatch(
							textfield_busname.getText().toString(),
							textfield_objectpath.getText().toString(),
							textfield_interface.getText().toString(),
							textfield_membername.getText().toString());
				} catch (Exception e) {
					Log.e(TAG, "", e);
				}
			}
		});
    }
}
