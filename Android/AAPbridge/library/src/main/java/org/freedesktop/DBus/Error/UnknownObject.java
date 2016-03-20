package org.freedesktop.DBus.Error;

import nl.bennyjacobs.aapbridge.dbus.RemoteDbusException;

public class UnknownObject extends RemoteDbusException {

	private static final long serialVersionUID = 4362696206132497717L;

	public UnknownObject(String errString) {
		super(UnknownObject.class.getName(), errString);
	}

}
