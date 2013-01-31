package org.freedesktop.DBus.Python;

import nl.ict.aapbridge.dbus.RemotePayloadException;

/**
 * <p>This java class represents a Python TypeError exception transmitted over the d-bus</p>
 * 
 * <p>It is used to test if you can define your own exceptions (Who might be thrown on the payload and catched on Android)</p>
 */
public class TypeError extends RemotePayloadException {
	
	private static final long serialVersionUID = -3334803111128434372L;

	public TypeError(String errString) {
		super(TypeError.class.getName(), errString);
	}

}
