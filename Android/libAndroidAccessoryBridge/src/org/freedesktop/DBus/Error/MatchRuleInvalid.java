package org.freedesktop.DBus.Error;

import nl.ict.aapbridge.dbus.RemoteDbusException;

public class MatchRuleInvalid extends RemoteDbusException {

	private static final long serialVersionUID = -8614069650229320959L;

	public MatchRuleInvalid(String errString) {
		super(MatchRuleInvalid.class.getName(), errString);
	}

}
