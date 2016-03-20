package org.freedesktop.DBus.Exceptions;

import nl.bennyjacobs.aapbridge.dbus.RemoteDbusException;

public class NotConnected extends RemoteDbusException {

	private static final long serialVersionUID = 6427249803292814162L;

	public NotConnected(String errString) {
		super(NotConnected.class.getName(), errString);
	}

}
