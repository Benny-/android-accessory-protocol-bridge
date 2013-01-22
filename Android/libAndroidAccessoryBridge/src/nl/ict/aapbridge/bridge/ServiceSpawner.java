package nl.ict.aapbridge.bridge;

import java.io.IOException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Semaphore;

import android.util.Log;

import static nl.ict.aapbridge.TAG.TAG;
import nl.ict.aapbridge.bridge.AccessoryBridge.BridgeService;
import nl.ict.aapbridge.bridge.AccessoryBridge.Port;

class ServiceSpawner implements BridgeService{
	
	private Port port;
	private static final ByteBuffer portRequest = ByteBuffer.allocate(4);
	private ByteBuffer portRequestResponse		= ByteBuffer.allocate(8);
	private Semaphore openRequestResponsesReady = new Semaphore(0, true);
	private Semaphore openRequestResponsesDone  = new Semaphore(0);
	private Queue<OpenRequestResponse> openRequestResponses = new LinkedList<OpenRequestResponse>();
	
	static class OpenRequestResponse{
		private boolean success;
		private short port;
		private int errorCode;
		public OpenRequestResponse(boolean success, short port, int errorCode) {
			this.success = success;
			this.port = port;
			this.errorCode = errorCode;
		}
		public boolean getSucces()	{return success;}
		public short getPort()		{return port;}
		public int getErrorCode()	{return errorCode;}
	}
	
	static {
		portRequest.order(ByteOrder.LITTLE_ENDIAN);
		portRequest.put((byte)'o');
		portRequest.mark();
	}
	
	public ServiceSpawner(Port port) {
		this.port = port;
	}

	@Override
	public void onDataReady(int length) throws IOException {
		port.readAll(portRequestResponse);
		char msgType = (char) portRequestResponse.get();
		if( msgType != 's')
		{
			Log.w(TAG, "Accessory is screwing with the protocol, expecting a port status message, but got "+msgType);
		}
		else
		{
			Short port = portRequestResponse.getShort();
			boolean success = portRequestResponse.get() == 1 ? true : false;
			int errorCode = portRequestResponse.getInt();
			this.openRequestResponses.add(new OpenRequestResponse(success, port, errorCode));
			this.openRequestResponsesReady.release();
			try {
				this.openRequestResponsesDone.acquire();
			} catch (InterruptedException e) {
				// This should never happen.
				Log.wtf(TAG, e);
				throw new Error(e);
			}
		}
	}
	
	public short requestService(byte serviceIdentifier, ByteBuffer arguments) throws IOException, ServiceRequestException
	{
		portRequest.reset();
		portRequest.put(serviceIdentifier);
		portRequest.putShort((short) arguments.remaining());
		portRequest.position(0);
		
		synchronized (this) {
			while(portRequest.hasRemaining())
				port.write(portRequest);
			while(arguments.hasRemaining())
				port.write(arguments);
		}
		
		OpenRequestResponse response = null;
		try {
			this.openRequestResponsesReady.acquire();
			response = openRequestResponses.poll();
			this.openRequestResponsesDone.release();
		} catch (InterruptedException e) {
			// This should never happen. The protocol state will become corrupted if it happens.
			Log.wtf(TAG, e);
			throw new Error(e);
		}
		if(response == null)
		{
			// This should never happen.
			String errmsg = "Service spawner is in illegal state";
			Log.wtf(TAG, errmsg);
			throw new Error(errmsg);
		}
		else
		{
			if(!response.success)
			{
				throw new ServiceRequestException(response.getErrorCode());
			}
		}
		return response.getPort();
	}
	
	@Override
	public Port getPort() {
		return port;
	}
}
