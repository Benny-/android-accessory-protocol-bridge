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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;

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
	
	private static UUID uuid;
	private InputStream mInputStream;
	private OutputStream mOutputStream;
	private BluetoothSocket mSocket;
	private boolean disconnected = false;
	
	public BTConnection(Context context, BluetoothDevice device) throws IOException, XmlPullParserException {
		
		synchronized (BTConnection.class)
		{
			if(uuid == null)
			{
				Resources resources = context.getResources();
				XmlResourceParser xmlPuller = resources.getXml(resources.getIdentifier("accessory_filter", "xml", context.getPackageName()));
				do
				{
					if(xmlPuller.getEventType() == XmlPullParser.START_TAG && "bt-accessory".equals(xmlPuller.getName()))
					{
						String uuid_str = xmlPuller.getAttributeValue(null, "uuid");
						uuid = UUID.fromString(uuid_str);
					}
				} while( xmlPuller.next() != XmlPullParser.END_DOCUMENT );
			}
		}
		
		mSocket = device.createInsecureRfcommSocketToServiceRecord(uuid);
		mSocket.connect();
		mInputStream = new BufferedInputStream(mSocket.getInputStream());
		mOutputStream = new BufferedOutputStream(mSocket.getOutputStream());
	}
	
	public BTConnection(Context context, String address) throws IOException, XmlPullParserException {
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

