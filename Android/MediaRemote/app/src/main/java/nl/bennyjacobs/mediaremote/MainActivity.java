package nl.bennyjacobs.mediaremote;

import java.io.IOException;

import nl.bennyjacobs.mediaremote.R.id;
import nl.bennyjacobs.aapbridge.aap.UsbConnection;
import nl.bennyjacobs.aapbridge.bridge.AccessoryBridge;
import nl.bennyjacobs.aapbridge.bridge.ServiceRequestException;
import nl.bennyjacobs.aapbridge.dbus.DbusHandler;
import nl.bennyjacobs.aapbridge.dbus.DbusMethods;
import nl.bennyjacobs.aapbridge.dbus.RemoteDbusException;
import nl.bennyjacobs.aapbridge.dbus.RemotePayloadException;
import nl.bennyjacobs.aapbridge.dbus.message.DbusMessage;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	public static final String TAG = MainActivity.class.getName();
	public static AccessoryBridge aapbridge;
	
	private DbusMethods dbusMethods;
	
	private Button play;
	private Button next;
	private Button previous;
	private Button stop;
	
	/**
	 * This dbushandler only shows remote exceptions. We are not 
	 * actually interested in anything the remote system sends.
	 */
	private DbusHandler dbusHandler = new DbusHandler() {
		@Override
		public void handleMessage(Message msg) {
			DbusMessage dbusMessage = (DbusMessage) msg.obj;
			try {
				dbusMessage.getValues();
			} catch (Exception e) {
				String exMsg = "Exception: "+e.getLocalizedMessage();
				Log.e(TAG, exMsg, e);
				Toast.makeText(MainActivity.this, exMsg, Toast.LENGTH_LONG).show();
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		play = (Button) findViewById(R.id.button_play);
		next = (Button) findViewById(id.button_next);
		previous = (Button) findViewById(id.button_previous);
		stop = (Button) findViewById(id.button_stop);
		
        if(aapbridge == null || !aapbridge.isOpen())
        {
			try {
				aapbridge = new AccessoryBridge(UsbConnection.easyConnect(getApplicationContext()));
			} catch (IOException e) {
				Log.e(TAG, "Could not setup aapbridge", e);
				Toast.makeText(this, "Could not setup aapbridge: "+e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
				finish();
			}
        }
		
        try {
			dbusMethods = aapbridge.createDbus(dbusHandler);
		} catch (Exception e) {
			Log.e(TAG, "Could not setup dbusMethods", e);
			Toast.makeText(this, "Could not setup dbusMethods: "+e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
			finish();
		}
        
		play.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					dbusMethods.methodCall("org.gnome.Rhythmbox3", "/org/mpris/MediaPlayer2", "org.mpris.MediaPlayer2.Player", "PlayPause");
				} catch (IOException e) {
					String msg = "Could not send 'play/pause' command: " + e.getLocalizedMessage();
					Log.e(TAG, msg, e);
					Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
				}
			}
		});
		
		next.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					dbusMethods.methodCall("org.gnome.Rhythmbox3", "/org/mpris/MediaPlayer2", "org.mpris.MediaPlayer2.Player", "Next");
				} catch (IOException e) {
					String msg = "Could not send 'next' command: " + e.getLocalizedMessage();
					Log.e(TAG, msg, e);
					Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
				}
			}
		});
		
		previous.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					dbusMethods.methodCall("org.gnome.Rhythmbox3", "/org/mpris/MediaPlayer2", "org.mpris.MediaPlayer2.Player", "Previous");
				} catch (IOException e) {
					String msg = "Could not send 'previous' command: " + e.getLocalizedMessage();
					Log.e(TAG, msg, e);
					Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
				}
			}
		});
		
		stop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					dbusMethods.methodCall("org.gnome.Rhythmbox3", "/org/mpris/MediaPlayer2", "org.mpris.MediaPlayer2.Player", "Stop");
				} catch (IOException e) {
					String msg = "Could not send 'stop' command: " + e.getLocalizedMessage();
					Log.e(TAG, msg, e);
					Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
				}
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		try {
			// We only close the method service.
			// We do not close the entire bridge.
			// This enables the user to restart the activity and continue if we are connected to usb.
			dbusMethods.close();
		} catch (IOException e) {
			Log.e(TAG, "onDestroy()", e);
		}
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
