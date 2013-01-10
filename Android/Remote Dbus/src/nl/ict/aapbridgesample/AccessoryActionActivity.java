package nl.ict.aapbridgesample;
 
import java.io.IOException;

import nl.ict.aapbridge.aap.UsbConnection;
import nl.ict.aapbridge.bridge.AccessoryBridge;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AccessoryActionActivity extends Activity {
	
	public static final String TAG = AccessoryActionActivity.class.getName();

	public static AccessoryBridge aapbridge;
	
	private Button button_rpc;
	private Button button_signals;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        button_rpc = (Button) findViewById(R.id.button_rpc);
        button_signals = (Button) findViewById(R.id.button_signals);
        
        button_rpc.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), CallRemoteFunctionActivity.class);
				AccessoryActionActivity.this.startActivity(intent);
			}
		});
        
        button_signals.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), SignalFilterActivity.class);
				AccessoryActionActivity.this.startActivity(intent);
			}
		});
        
        if(aapbridge == null || !aapbridge.isOpen())
        {
			try {
				aapbridge = new AccessoryBridge(UsbConnection.easyConnect(getApplicationContext()));
			} catch (IOException e) {
				Log.e(TAG, "", e);
			}
        }
    }
}
