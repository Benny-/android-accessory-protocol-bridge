package nl.bennyjacobs.remotedbus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import nl.bennyjacobs.aapbridge.bridge.AccessoryBridge;
import nl.bennyjacobs.aapbridge.bridge.ServiceRequestException;
import nl.bennyjacobs.aapbridge.bulk.BulkTransfer;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class BulkActivity extends Activity {

	public static final String TAG = BulkActivity.class.getName();
	public static final Charset utf8 = Charset.forName("UTF-8");
	
	private AccessoryBridge bridge;
	private InputStream input;
	private OutputStream output;
	
	private EditText text_busname;
	private EditText text_objectpath;
	private EditText text_arguments;
	private Button button_bulk_start;
	private EditText text_input;
	private Button button_send;
	private EditText text_output;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bulk);
		
		bridge = ServiceSelectionActivity.aapbridge;
		
		text_busname = (EditText) findViewById(R.id.editText_busname);
		text_objectpath = (EditText) findViewById(R.id.editText_objectpath);
		text_arguments = (EditText) findViewById(R.id.editText_arguments);
		button_bulk_start = (Button) findViewById(R.id.button_bulk_start);
		text_input = (EditText) findViewById(R.id.editText_bulk_input);
		button_send = (Button) findViewById(R.id.button_bulk_send);
		text_output = (EditText) findViewById(R.id.editText_bulk_output);
		
		button_bulk_start.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try {
					BulkTransfer bulk = bridge.createBulkTransfer(
											text_busname.getText().toString(),
											text_objectpath.getText().toString(),
											text_arguments.getText().toString());
					input = bulk.getInput();
					output = bulk.getOutput();
					
					new Thread() {
						public void run() {
							try{
								byte buffer[] = new byte[1024];
								int read;
								while( (read = input.read(buffer)) != 0)
								{
									final String received = new String(buffer, 0, read, utf8);
									
									BulkActivity.this.runOnUiThread(new Runnable() {
										public void run() {
											text_output.getText().append(received);
										}
									});
								}
							}
							catch (IOException e)
							{
								Log.v(TAG, "Bulk reader thread stopped");
							}
						}
					}.start();
					
				} catch (Exception e) {
					String error = "Could not start bulk transfer: "+e.getLocalizedMessage();
					Log.e(TAG, error, e);
					Toast.makeText(BulkActivity.this, error, Toast.LENGTH_LONG).show();
				}
			}
		});
		
		button_send.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try {
					output.write( text_input.getText().toString().getBytes(utf8) );
					output.flush();
				} catch (Exception e) {
					String error = "Could not start bulk transfer: "+e.getLocalizedMessage();
					Log.e(TAG, error, e);
					Toast.makeText(BulkActivity.this, error, Toast.LENGTH_LONG).show();
				}
			}
		});
	}
}
