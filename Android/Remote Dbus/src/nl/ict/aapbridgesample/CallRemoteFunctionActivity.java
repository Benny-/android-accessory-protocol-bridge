package nl.ict.aapbridgesample;

import java.io.IOException;
import java.nio.charset.Charset;

import nl.ict.aapbridge.bridge.AccessoryBridge;
import nl.ict.aapbridge.bridge.ServiceRequestException;
import nl.ict.aapbridge.dbus.Dbus;
import nl.ict.aapbridge.dbus.DbusHandler;
import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CallRemoteFunctionActivity extends Activity
{
	public static final String TAG = CallRemoteFunctionActivity.class.getName();
	public static final Charset utf8 = Charset.forName("UTF-8");
	
	private AccessoryBridge aapbridge;
    
    private EditText textfield_busname;
    private EditText textfield_objectpath;
    private EditText textfield_interface;
    private EditText textfield_functionname;
    
    private Button button_dbus_call;
    private Button button_keepalive;
    
    private DbusHandler dbus_handler = new DbusHandler() {
    	@Override
    	public void handleMessage(Message msg) {
    		Log.v(TAG, "Received some dbus response: "+msg.toString());
    		msg.recycle();
    	}
	};
	
	private Dbus d;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call_remote_function);
        
        textfield_busname = (EditText)findViewById(R.id.Busname);
        textfield_objectpath = (EditText) findViewById(R.id.Objectpath);
        textfield_interface = (EditText) findViewById(R.id.Interface);
        textfield_functionname = (EditText) findViewById(R.id.Functionname);
        
        button_dbus_call = (Button)findViewById(R.id.button_dbus_call);
        button_keepalive = (Button)findViewById(R.id.button_keepalive);
        
        aapbridge = AccessoryActionActivity.aapbridge;
        
        try {
			d = aapbridge.createDbus(dbus_handler);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
        
        button_dbus_call.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		
        		try {
					d.methodCall(
							textfield_busname.getText().toString(),
							textfield_objectpath.getText().toString(),
							textfield_interface.getText().toString(),
							textfield_functionname.getText().toString() );
				} catch (Exception e) {
					Toast.makeText(CallRemoteFunctionActivity.this, "Accessory not connected", Toast.LENGTH_SHORT).show();
		        	finish();
					Log.e(TAG, "", e);
				}
        	}
		});
        
        button_keepalive.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try {
					aapbridge.sendKeepalive();
				} catch (Exception e) {
					Toast.makeText(CallRemoteFunctionActivity.this, "Accessory not connected", Toast.LENGTH_SHORT).show();
		        	finish();
					Log.e(TAG, "", e);
				}
			}
		});
    }
    
    @Override
    protected void onDestroy() {
    	try {
			d.close();
		} catch (IOException e) {
			Log.e(TAG, "", e);
		}
    	super.onDestroy();
    }
}