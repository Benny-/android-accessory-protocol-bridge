package nl.bennyjacobs.remotedbus;
 
import java.io.IOException;

import nl.bennyjacobs.aapbridge.aap.UsbConnection;
import nl.bennyjacobs.aapbridge.bridge.AccessoryBridge;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class ServiceSelectionActivity extends Activity {
	
	public static final String TAG = ServiceSelectionActivity.class.getName();

	public static AccessoryBridge aapbridge;
	
	private Button button_rpc;
	private Button button_signals;
	private Button button_bulk;
	private Button button_keepalive;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        button_rpc = (Button) findViewById(R.id.button_rpc);
        button_signals = (Button) findViewById(R.id.button_signals);
        button_bulk = (Button) findViewById(R.id.button_bulk);
        button_keepalive = (Button)findViewById(R.id.button_keepalive);
        
        button_rpc.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), DbusMethodsActivity.class);
				ServiceSelectionActivity.this.startActivity(intent);
			}
		});
        
        button_signals.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), DbusSignalActivity.class);
				ServiceSelectionActivity.this.startActivity(intent);
			}
		});
        
        if(aapbridge == null || !aapbridge.isOpen())
        {
			try {
				aapbridge = new AccessoryBridge(UsbConnection.easyConnect(getApplicationContext()));
			} catch (IOException e) {
				Log.e(TAG, "Could not setup aapbridge", e);
				Toast.makeText(ServiceSelectionActivity.this, "Could not setup aapbridge: "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
				finish();
			}
        }
        
        button_bulk.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), BulkActivity.class);
				ServiceSelectionActivity.this.startActivity(intent);
			}
		});
        
        button_keepalive.setOnClickListener(new OnClickListener() {
    		public void onClick(View v) {
    			try {
    				aapbridge.sendKeepalive();
    			} catch (IOException e) {
    				Log.e(TAG, "Could not send keepalive", e);
    				Toast.makeText(ServiceSelectionActivity.this, "Failed to send keepalive: "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
    	        	finish();
    			}
    		}
    	});
        
    }
}
