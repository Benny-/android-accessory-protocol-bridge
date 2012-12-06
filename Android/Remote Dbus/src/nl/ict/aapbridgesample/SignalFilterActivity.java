package nl.ict.aapbridgesample;

import java.io.StringWriter;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.text.StringCharacterIterator;

import org.apache.http.util.ByteArrayBuffer;

import nl.ict.aapbridge.R;
import nl.ict.aapbridge.bridge.AccessoryBridge;
import nl.ict.aapbridge.bridge.AccessoryMessage.MessageType;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
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
					ByteBuffer buffer = ByteBuffer.allocate(1024);
					buffer.order(ByteOrder.LITTLE_ENDIAN);
					
					buffer.put((byte)1); // First byte is if we wish to register or unregister.
					textInputsToBytes(buffer);
					
					aapbridge.Write(
							buffer.array(),
							0,
							MessageType.SIGNAL);
				} catch (Exception e) {
					Log.e(TAG, "", e);
				}
			}
		});
        
        button_removewatch.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try {
					ByteBuffer buffer = ByteBuffer.allocate(1024);
					buffer.order(ByteOrder.LITTLE_ENDIAN);
					
					buffer.put((byte)0); // First byte is if we wish to register or unregister.
					textInputsToBytes(buffer);
					
					aapbridge.Write(
							buffer.array(),
							0,
							MessageType.SIGNAL);
				} catch (Exception e) {
					Log.e(TAG, "", e);
				}
			}
		});
    }
    
    private void textInputsToBytes(ByteBuffer buffer)
    {
		buffer.put(textfield_busname.getText().toString().getBytes(utf8));
		buffer.put((byte) 0);
		buffer.put(textfield_objectpath.getText().toString().getBytes(utf8));
		buffer.put((byte) 0);
		buffer.put(textfield_interface.getText().toString().getBytes(utf8));
		buffer.put((byte) 0);
		buffer.put(textfield_membername.getText().toString().getBytes(utf8));
		buffer.put((byte) 0);
    }
}
