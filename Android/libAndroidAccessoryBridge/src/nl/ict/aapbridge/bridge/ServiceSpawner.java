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
import nl.ict.aapbridge.bridge.AccessoryBridge.ReceiverThread;

class ServiceSpawner implements BridgeService{
	
	private Port port;
	private ByteBuffer portRequest				= ByteBuffer.allocate(4);
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
	
	public ServiceSpawner(Port port) {
		portRequest.order(ByteOrder.LITTLE_ENDIAN);
		portRequestResponse.order(ByteOrder.LITTLE_ENDIAN);
		this.port = port;
	}

	/**
	 * This function is only called by the {@link ReceiverThread}
	 */
	@Override
	public void onDataReady(int length) throws IOException {
		portRequestResponse.rewind();
		try{
			port.readAll(portRequestResponse);
		} catch (IOException e)
		{
			this.openRequestResponsesReady.release(5000); // We release all these permits so the requestService() thread will not block forever
		}
		portRequestResponse.rewind();
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
	
	/**
	 * This may not be called from the {@link ReceiverThread}. This function may be called from any other thread (including the ui-thread)
	 * 
	 * @param serviceIdentifier
	 * @param arguments
	 * @return The port the new service will be located on
	 * @throws IOException If connection to accessory is lost
	 * @throws ServiceRequestException If accessory declined to create the service
	 */
	public short requestService(byte serviceIdentifier, ByteBuffer arguments) throws IOException, ServiceRequestException
	{
		portRequest.rewind();
		portRequest.put((byte)'o');
		portRequest.put(serviceIdentifier);
		portRequest.putShort((short) arguments.remaining());
		portRequest.rewind();
		
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
			throw new IOException("Diddent receive a response");
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

	@Override
	public void onEof() {
		// TODO Auto-generated method stub
	}
}
