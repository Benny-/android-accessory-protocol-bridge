package nl.ict.aapbridgesample;

import java.io.IOException;
import java.nio.charset.Charset;

import nl.ict.aapbridge.bridge.AccessoryBridge;
import nl.ict.aapbridge.bridge.ServiceRequestException;
import nl.ict.aapbridge.dbus.Dbus;
import nl.ict.aapbridge.dbus.DbusHandler;
import nl.ict.aapbridge.dbus.RemoteException;
import nl.ict.aapbridge.dbus.message.DbusMessage;
import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class DbusMethodsActivity extends Activity
{
	public static final String TAG = DbusMethodsActivity.class.getName();
	public static final Charset utf8 = Charset.forName("UTF-8");
	
	private AccessoryBridge aapbridge;
    
    private EditText textfield_busname;
    private EditText textfield_objectpath;
    private EditText textfield_interface;
    private EditText textfield_functionname;
    
    private Button button_dbus_call;
    
    private DbusHandler dbus_handler = new DbusHandler() {
    	@Override
    	public void handleMessage(Message msg) {
    		DbusMessage dbusmsg =  (DbusMessage) msg.obj;
    		Log.v(TAG, "Received a dbus response: \n"+dbusmsg.toString());
    		try
    		{
    			Object[] values = dbusmsg.getValues();
    		}
    		catch(RemoteException e)
    		{
    			Log.v(TAG, "Dbus threw a exception", e);
    			Toast.makeText(DbusMethodsActivity.this, "A "+e.getTrueType()+" exception occured: "+e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
    		}
    		
    		// msg.recycle(); // This crashes the process.
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
        
        aapbridge = ServiceSelectionActivity.aapbridge;
        
        try {
			d = aapbridge.createDbus(dbus_handler);
		} catch (Exception e) {
			Log.e(TAG, "Could not create remote dbus service", e);
			Toast.makeText(this, "Could not start d-bus method service: "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
			finish();
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
					Log.e(TAG, "", e);
					Toast.makeText(DbusMethodsActivity.this, "Accessory not connected", Toast.LENGTH_SHORT).show();
		        	finish();
				}
        	}
		});
    }
    
    @Override
    protected void onDestroy() {
    	try {
			d.close();
		} catch (Exception e) {
			Log.e(TAG, "", e);
		}
    	super.onDestroy();
    }
}