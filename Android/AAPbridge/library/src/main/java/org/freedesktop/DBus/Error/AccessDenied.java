package org.freedesktop.DBus.Error;

import nl.bennyjacobs.aapbridge.dbus.RemoteDbusException;

public class AccessDenied extends RemoteDbusException {

	private static final long serialVersionUID = 5995823285356112568L;

	public AccessDenied(String errString) {
		super(AccessDenied.class.getName(), errString);
	}

}
