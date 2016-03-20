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


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 
 * Provides an abstraction layer to a android accessory.
 * 
 * @see BTConnection
 * @see UsbConnection
 */
public interface AccessoryConnection {
	
	/**
	 * Retrieves a inputstream to read data from the accessory. This inputsteam is buffered.
	 * 
	 * @return A buffered inputstream
	 * @throws IOException
	 */
	public abstract InputStream getInputStream() throws IOException;
	
	/**
	 * Retrieves a outputstream to write data to the accessory. This outputstream is buffered.
	 * 
	 * @return A buffered outputstream
	 * @throws IOException
	 */
	public abstract OutputStream getOutputStream() throws IOException;
	
	/**
	 * Closes the input and output streams. No more bytes may be read or written to the streams.
	 * 
	 * @throws IOException
	 */
	public abstract void close() throws IOException;
	
	/**
	 * Check if both communication channels are closed.
	 * 
	 * Will always return true if {@link #close() close} has been called.
	 * 
	 * @return True if communication channel is closed. False if communication channel can still be used.
	 */
	public boolean disconnected();
}
