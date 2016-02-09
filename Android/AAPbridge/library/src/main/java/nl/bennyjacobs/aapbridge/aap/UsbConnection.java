package nl.bennyjacobs.aapbridge.aap;
/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */


import static nl.bennyjacobs.aapbridge.TAG.TAG;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelFileDescriptor;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;

/**
 * 
 * <p>Creates a connection to a android accessory using a usb bulk endpoint.</p>
 * 
 * <p>Briefly: Make sure you have the proper permissions and call {@link #easyConnect(Context)}
 * once your application is started when the android device is inserted into the accessory.</p>
 * 
 * <p>The following must be in your manifest for the usb connection to properly work:</p>
 * 
 * <pre>
 * {@code
 * <manifest xmlns:android="http://schemas.android.com/apk/res/android">
 *	<uses-feature android:name="com.android.future.usb.accessory" android:required="false"/>
 *	<application>
 *		<activity
 *		android:name="com.example.yourMainActivity"
 *			<intent-filter>
 *				<action android:name="android.hardware.usb.action.USB_ACCESSORY_ATTACHED" />
 *			</intent-filter>
 *			<meta-data
 *				android:name="android.hardware.usb.action.USB_ACCESSORY_ATTACHED"
 *				android:resource="@xml/accessory_filter" />
 *		</activity>
 *	</application>
 * </manifest>
 * }
 * </pre>
 * 
 * <p>"@xml/accessory_filter" contains matching information. The Android system uses this
 * to decide which application should be started when a accessory identifies itself.
 * This is a example of a minimal accessory_filter.xml:</p>
 * 
 * <pre>
 * {@code
 *	<?xml version="1.0" encoding="utf-8"?>
 *	<resources>
 *		<usb-accessory model="Example product" manufacturer="Example company" version="1.0"/>
 *	</resources>
 * }
 * </pre>
 * 
 * <p>The user will be given a option to start your application when the accessory is inserted.
 * If the user chooses so, the "yourMainActivity" in the manifest above will be started.
 * The activity will have permission to access the accessory once it is started this way.
 * To create a usb connection, call {@link #easyConnect(Context) } like this: </p>
 * 
 * <pre>
 * {@code
 *	try {
 *		AccessoryConnection con = UsbConnection.easyConnect(getApplicationContext());
 *	} catch (IOException e) {
 *		Log.e(TAG, "Could not setup connection", e);
 *		Toast.makeText(this, "Could not setup connection: "+e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
 *		finish();
 *	}
 * }
 * </pre>
 * 
 * <p>Manually creating UsbConnection objects using {@link UsbManager#requestPermission(UsbAccessory, PendingIntent) UsbManager.requestPermission()}
 * and the constructor is not recommended as the accessory may be in a inconsistent state. Disconnect the android deivce
 * and reconnect.</p>
 * 
 */
public class UsbConnection implements AccessoryConnection
{
	
	private Context mContext;
	private InputStream mInputStream;
	private OutputStream mOutputStream;
	private ParcelFileDescriptor mFileDescriptor;
	private UsbAccessory mAccessory;
	private boolean disconnected = false;

	private static final String ACTION_USB_PERMISSION = "com.google.android.DemoKit.action.USB_PERMISSION";

//	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			String action = intent.getAction();
//			if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
//				UsbAccessory accessory = UsbManager.getAccessory(intent);
//				if (accessory != null && accessory.equals(mAccessory)) {
//					Log.i(TAG, "Accessory disconnected");
//					ConnectActivity.connection = null;
//					context.unregisterReceiver(this);
//				}
//			}
//		}
//	};

	public UsbConnection(Context context, UsbManager usbManager,
			UsbAccessory accessory) throws IOException {
		mContext = context;
		mAccessory = accessory;
		
		mFileDescriptor = usbManager.openAccessory(accessory);
		if (mFileDescriptor == null) {
			throw new IOException("Could not open accessory");
		}
		
		FileDescriptor fd = mFileDescriptor.getFileDescriptor();
		mInputStream = new BufferedInputStream(new FileInputStream(fd));
		mOutputStream = new BufferedOutputStream(new FileOutputStream(fd));
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		//mContext.registerReceiver(mUsbReceiver, filter);
	}

	public InputStream getInputStream() {
		return mInputStream;
	}

	public OutputStream getOutputStream() {
		return mOutputStream;
	}

	public void close() throws IOException {
		//mActivity.unregisterReceiver(mUsbReceiver);
		if (!disconnected) {
			disconnected = true;
			mInputStream.close();
			mOutputStream.close();
			mFileDescriptor.close();
		}
	}
	
	/**
	 * Connect to the first usb accessory.
	 * 
	 * @param context
	 * @return A connection to the first usb accessory or null in case of failure.
	 * @throws IOException 
	 */
	public static UsbConnection easyConnect(Context context) throws IOException
	{
		UsbManager mUSBManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
    	UsbAccessory[] accessories = mUSBManager.getAccessoryList();
		UsbAccessory accessory = (accessories == null ? null
				: accessories[0]);
		if (accessory != null)
		{
			if (mUSBManager.hasPermission(accessory)) {
				UsbConnection connection = new UsbConnection(context, mUSBManager, accessory);
				Log.v(TAG, "Connected to USB accessory");
				return connection;
			}
			throw new IOException("No permission to operate on accessory");
		}
		throw new IOException("No USB accessory found");
	}

	public boolean disconnected() {
		return disconnected;
	}
}
