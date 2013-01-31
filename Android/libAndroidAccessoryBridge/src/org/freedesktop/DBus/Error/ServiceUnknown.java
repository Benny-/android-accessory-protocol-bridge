package org.freedesktop.DBus.Error;

import nl.ict.aapbridge.dbus.RemoteDbusException;

public class ServiceUnknown extends RemoteDbusException {

	private static final long serialVersionUID = 2257005120775603980L;

	public ServiceUnknown(String errString) {
		super(ServiceUnknown.class.getName(), errString);
	}

}
