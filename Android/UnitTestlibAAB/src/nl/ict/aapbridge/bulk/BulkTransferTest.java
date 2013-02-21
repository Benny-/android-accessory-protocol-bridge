package nl.ict.aapbridge.bulk;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import nl.ict.aapbridge.bridge.AccessoryBridge;
import nl.ict.aapbridge.bridge.ServiceRequestException;
import nl.ict.aapbridge.test.BridgeFactoryService;
import android.test.AndroidTestCase;

public class BulkTransferTest extends AndroidTestCase {
	
	private AccessoryBridge bridge;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		if(bridge == null)
		{
			bridge = BridgeFactoryService.getAAPBridge(getContext());
		}
	}
	
	public void testShortString() throws IOException
	{
		BulkTransfer transfer = null;
		try
		{
			transfer = bridge.createBulkTransfer("nl.ict.AABUnitTest", "/nl/ict/AABUnitTest/bulk/echo1", "Memory/File/Anything");
		}
		catch(ServiceRequestException e)
		{
			fail();
		}
		InputStream input = transfer.getInput();
		OutputStream output = transfer.getOutput();
		
		output.write('h');
		output.write('e');
		output.write('l');
		output.write('l');
		output.write('o');
		output.close();
		assertEquals('h', input.read());
		assertEquals('e', input.read());
		assertEquals('l', input.read());
		assertEquals('l', input.read());
		assertEquals('o', input.read());
		assertEquals(-1, input.read());
		input.close();
	}
	
	public void testEof() throws IOException
	{
		BulkTransfer transfer = null;
		try
		{
			transfer = bridge.createBulkTransfer("nl.ict.AABUnitTest", "/nl/ict/AABUnitTest/bulk/echo1", "Memory/File/Anything");
		}
		catch(ServiceRequestException e)
		{
			fail();
		}
		InputStream input = transfer.getInput();
		OutputStream output = transfer.getOutput();
		
		output.close();
		assertEquals(-1, input.read());
		input.close();
	}
	
	public void testOneByte() throws IOException
	{
		BulkTransfer transfer = null;
		try
		{
			transfer = bridge.createBulkTransfer("nl.ict.AABUnitTest", "/nl/ict/AABUnitTest/bulk/echo1", "Memory/File/Anything");
		}
		catch(ServiceRequestException e)
		{
			fail();
		}
		InputStream input = transfer.getInput();
		OutputStream output = transfer.getOutput();
		
		output.write('h');
		output.close();
		assertEquals('h', input.read());
		assertEquals(-1, input.read());
		input.close();
	}

}
