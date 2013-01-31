package org.freedesktop.DBus.Error;

import nl.ict.aapbridge.dbus.RemoteDbusException;

public class UnknownMethod extends RemoteDbusException{

	private static final long serialVersionUID = 3171556429001289000L;

	public UnknownMethod(String errString) {
		super(UnknownError.class.getName(), errString);
	}
}
