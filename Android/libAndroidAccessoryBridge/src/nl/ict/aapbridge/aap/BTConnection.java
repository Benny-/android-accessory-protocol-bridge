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

/**
 * 
 * Creates a connection to a android accessory using a rfcomm bluetooth socket.
 * 
 */
public class BTConnection implements AccessoryConnection, Closeable {

	private InputStream mInputStream;
	private OutputStream mOutputStream;
	private BluetoothSocket mSocket;
	private boolean disconnected = false;

	private static final UUID MY_UUID_INSECURE = UUID
			.fromString("a48e5d50-188b-4fca-b261-89c13914e118");
	
	public BTConnection(BluetoothDevice device) throws IOException {
		mSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
		mSocket.connect();
		mInputStream = new BufferedInputStream(mSocket.getInputStream());
		mOutputStream = new BufferedOutputStream(mSocket.getOutputStream());
	}
	
	public BTConnection(String address) throws IOException {
		this(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address));
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

