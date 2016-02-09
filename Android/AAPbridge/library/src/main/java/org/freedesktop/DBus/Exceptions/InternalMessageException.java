package org.freedesktop.DBus.Exceptions;

import nl.bennyjacobs.aapbridge.dbus.RemoteDbusException;

public class InternalMessageException extends RemoteDbusException {

	private static final long serialVersionUID = 1740937625174679283L;

	public InternalMessageException(String errString) {
		super(InternalMessageException.class.getName(), errString);
	}

}
