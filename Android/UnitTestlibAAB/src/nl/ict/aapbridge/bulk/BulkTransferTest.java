package nl.ict.aapbridge.bulk;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import nl.ict.aapbridge.bridge.AccessoryBridge;
import nl.ict.aapbridge.bridge.ServiceRequestException;
import nl.ict.aapbridge.test.UsbConnectorService;
import android.test.AndroidTestCase;

public class BulkTransferTest extends AndroidTestCase {
	
	private AccessoryBridge bridge;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		if(bridge == null)
		{
			bridge = UsbConnectorService.getAAPBridge(getContext());
		}
	}
	
	public void testApi() throws IOException
	{
		BulkTransfer transfer = null;
		try
		{
			transfer = bridge.createBulkTransfer("nl.ict.AABUnitTest", "/nl/ict/AABUnitTest/Bulk1", "Memory/File/Anything");
		}
		catch(ServiceRequestException e)
		{
			fail();
		}
		InputStream input = transfer.getInput();
		OutputStream output = transfer.getOutput();
		
		output.write(5);
		output.close();
		assertEquals(5, input.read());
		assertEquals(-1, input.read());
		input.close();
	}

}
