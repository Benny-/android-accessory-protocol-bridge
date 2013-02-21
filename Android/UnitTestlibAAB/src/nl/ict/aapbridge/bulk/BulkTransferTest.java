package nl.ict.aapbridge.bulk;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import nl.ict.aapbridge.bridge.AccessoryBridge;
import nl.ict.aapbridge.bridge.ServiceRequestException;
import nl.ict.aapbridge.test.BridgeFactoryService;
import android.test.AndroidTestCase;
import android.util.Log;

public class BulkTransferTest extends AndroidTestCase {
	
	public static final String TAG = BulkTransferTest.class.getName();
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
	
	public void testWrongObjectPath() throws IOException
	{
		boolean threw_first_error = false;
		boolean threw_second_error = false;
		
		BulkTransfer transfer = null;
		try
		{
			transfer = bridge.createBulkTransfer("nl.ict.AABUnitTest", "/nl/ict/AABUnitTest/bulk/non-existing-object", "Memory/File/Anything");
		}
		catch(ServiceRequestException e)
		{
			threw_first_error = true;
		}
		
		if(!threw_first_error)
			fail();
		
		try
		{
			transfer = bridge.createBulkTransfer("nl.ict.AABUnitTest", "/nl/ict/AABUnitTest/bulk/non-existing-object", "Memory/File/Anything");
		}
		catch(ServiceRequestException e)
		{
			threw_second_error = true;
		}
		
		if(!threw_second_error)
			fail();
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
	
	public void testAllot() throws IOException
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
		final OutputStream output = transfer.getOutput();
		
		final long bytes_to_transfer = 1000*500;
		Log.v(TAG, "Transferring "+bytes_to_transfer+" bytes");
		new Thread(new Runnable() {
			public void run() {
				long bytesLeft = bytes_to_transfer;
				int increasingNr = 0;
				
				try {
					while(bytesLeft > 0)
					{
						output.write(increasingNr);
						increasingNr++;
						increasingNr %= 16;
						bytesLeft--;
					}
					output.close();
				} catch (IOException e) {
					fail(e.getStackTrace().toString());
				}
			}
		}).start();
		
		long bytesLeft = bytes_to_transfer;
		int increasingNr = 0;
		try {
			while(bytesLeft > 0)
			{
				assertEquals(increasingNr, input.read());
				increasingNr++;
				increasingNr %= 16;
				bytesLeft--;
			}
			assertEquals(-1, input.read());
		} catch (IOException e) {
			fail(e.getStackTrace().toString());
		}
	}

}
