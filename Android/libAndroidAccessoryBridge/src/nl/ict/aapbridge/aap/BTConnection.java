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


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class BTConnection implements AccessoryConnection {

	private final BluetoothAdapter mAdapter;
	private BluetoothSocket mSocket;
	private boolean disconnected = false;

	private static final UUID MY_UUID_INSECURE = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	public BTConnection(String address) throws IOException {
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		BluetoothDevice device = mAdapter.getRemoteDevice(address);
		mSocket = device
				.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
		mSocket.connect();
	}

	public InputStream getInputStream() throws IOException {
		return mSocket.getInputStream();
	}

	public OutputStream getOutputStream() throws IOException {
		return mSocket.getOutputStream();
	}

	public void close() throws IOException {
		disconnected = true;
		mSocket.close();
	}

	public boolean disconnected() {
		return disconnected;
	}

}

