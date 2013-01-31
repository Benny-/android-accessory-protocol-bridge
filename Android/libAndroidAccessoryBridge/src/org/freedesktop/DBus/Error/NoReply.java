package org.freedesktop.DBus.Error;

import nl.ict.aapbridge.dbus.RemoteDbusException;

public class NoReply extends RemoteDbusException {

	private static final long serialVersionUID = -3515918060114373722L;

	public NoReply(String errString) {
		super(NoReply.class.getName(), errString);
	}

}
