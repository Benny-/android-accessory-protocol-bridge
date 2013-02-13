/**
 * <p>How to run these tests:</p>
 * 
 * <p>First start AAP-brdige (/Accessory/AAP-Bridge) and teststub.py (located in /Accessory/Payloads ) on the accessory.
 * teststub.py is a simple python program which provides a number of d-bus methods and signals to test against.</p>
 * 
 * <p>Attach the phone to the accessory using usb. Do NOT start any applications which uses the usb
 * accessory connection , reattach cable if you accidently started a application.
 * Run this unit test from eclipse (using adb over wifi), some time later a request pops up on the phone
 * requesting permission to acces to the usb accessory.
 * Press YES and the unit test results begin showing up in eclipse.</p>
 * 
 */
package nl.ict.aapbridge.test;
