package nl.ict.aapbridge.aap;
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


import static nl.ict.aapbridge.TAG.TAG;

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

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

/**
 * 
 * Creates a connection to a android accessory using a usb bulk endpoint.
 * 
 * Briefly: Make sure you have the proper permissions and call {@link #easyConnect(Context)}
 * once your application is started when the android device is inserted into the accessory.
 * 
 * Manually creating UsbConnection objects using {@link UsbManager#requestPermission(UsbAccessory, PendingIntent) UsbManager.requestPermission()}
 * and the constructor is not recommended as the accessory may be in a inconsistent state. Disconnect the android deivce
 * and reconnect.
 * 
 */
public class UsbConnection implements AccessoryConnection, Closeable
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
		UsbManager mUSBManager = UsbManager.getInstance(context);
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
