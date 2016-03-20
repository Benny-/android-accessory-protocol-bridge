package nl.bennyjacobs.aapbridge.dbus.message;

/**
 * A DbusType is a object who represents one of the dbus types.
 * 
 * Like uint16, uint32, objectpath, array, variant, ect..
 *
 */
public interface DbusType {
	String getSignature();
}
