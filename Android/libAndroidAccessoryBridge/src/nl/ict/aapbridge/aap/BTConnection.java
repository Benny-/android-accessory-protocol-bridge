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


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;

/**
 * <p>Creates a connection to a android accessory using a rfcomm bluetooth socket.</p>
 * 
 * <p>The following permission is required to use this class:</p>
 * 
 * <pre>
 * {@code
 * <uses-permission android:name="android.permission.BLUETOOTH" />
 * }
 * </pre>
 * 
 * <p>There are multiple ways to connect to a bluetooth device.
 * You could enumerate all paired devices and let the user pick or
 * scan the local bluez for new bluetooth devices or a combination of both.</p>
 * 
 */
public class BTConnection implements AccessoryConnection {

	private InputStream mInputStream;
	private OutputStream mOutputStream;
	private BluetoothSocket mSocket;
	private boolean disconnected = false;

	private static final UUID MY_UUID_INSECURE = UUID
			.fromString("a48e5d50-188b-4fca-b261-89c13914e118");
	
	public BTConnection(Context context, BluetoothDevice device) throws IOException {
		Resources resources = context.getResources();
		XmlResourceParser xmlRP = resources.getXml(resources.getIdentifier("accessory_filter", "xml", context.getPackageName()));
		// TODO: Extract UUID out of "res/xml/accessory_filter.xml"
		
		mSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
		mSocket.connect();
		mInputStream = new BufferedInputStream(mSocket.getInputStream());
		mOutputStream = new BufferedOutputStream(mSocket.getOutputStream());
	}
	
	public BTConnection(Context context, String address) throws IOException {
		this(context, BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address));
	}

	public InputStream getInputStream() throws IOException {
		return mInputStream;
	}

	public OutputStream getOutputStream() throws IOException {
		return mOutputStream;
	}

	@Override
	public void close() throws IOException {
		if(!disconnected)
		{
			disconnected = true;
			mInputStream.close();
			mOutputStream.close();
			mSocket.close();
		}
	}

	public boolean disconnected() {
		return disconnected;
	}

}

