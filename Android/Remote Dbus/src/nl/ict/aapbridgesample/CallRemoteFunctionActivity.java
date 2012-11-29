package nl.ict.aapbridgesample;

import java.io.IOException;

import nl.ict.aapbridge.R;
import nl.ict.aapbridge.bridge.AccessoryMessage;
import nl.ict.aapbridge.bridge.AccessoryBridge;
import nl.ict.aapbridge.bridge.MessageHandler;
import nl.ict.aapbridge.bridge.AccessoryMessage.MessageType;
import nl.ict.aapbridge.dbus.dbus;
import nl.ict.aapbridge.SystemHolder;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.future.usb.UsbManager;


public class CallRemoteFunctionActivity extends Activity
{
	public static final String TAG = CallRemoteFunctionActivity.class.getName();
	
	private AccessoryBridge aapbridge;
    
    private EditText textfield_busname;
    private EditText textfield_objectpath;
    private EditText textfield_interface;
    private EditText textfield_functionname;
    
    private Button button_dbus_call;
    private Button button_keepalive;
    
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
        
        button_dbus_call.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		dbus d = new dbus();
        		try {
					d.methodCall(textfield_functionname.getText().toString(), 0);
				} catch (Exception e) {
					Toast.makeText(SystemHolder.getContext(), "Accessory not connected", Toast.LENGTH_SHORT).show();
		        	finish();
					Log.e(TAG, "", e);
				}
        	}
		});
        
        button_keepalive.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try {
					AccessoryBridge.Write("Ping".getBytes(), 0, MessageType.KEEPALIVE);
				} catch (Exception e) {
					Toast.makeText(SystemHolder.getContext(), "Accessory not connected", Toast.LENGTH_SHORT).show();
		        	finish();
					Log.e(TAG, "", e);
				}
			}
		});
    }
}