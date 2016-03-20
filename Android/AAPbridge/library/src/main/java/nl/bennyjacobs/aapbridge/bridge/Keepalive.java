package nl.bennyjacobs.aapbridge.bridge;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;
import android.util.Log;

import nl.bennyjacobs.aapbridge.bridge.AccessoryBridge.BridgeService;
import nl.bennyjacobs.aapbridge.bridge.AccessoryBridge.Port;

class Keepalive implements BridgeService{
	
	private static final String TAG = "Keepalive";
	private Timer pinger = new Timer("Pinger", true);
	private static final ByteBuffer ping = ByteBuffer.allocate(4);
	private final Port port;
	private boolean keepAliveReceived;
	private final Handler handler;
	
	static {
		ping.order(ByteOrder.LITTLE_ENDIAN);
		ping.put("ping".getBytes(Charset.forName("utf-8")));
	}
	
	public void sendKeepalive() throws IOException
	{
		ping.rewind();
		port.write(ping);
	}
	
	public Keepalive(Port port, Handler disconnectHandler) {
		keepAliveReceived = true;
		this.handler = disconnectHandler;
		this.port = port;
		pinger.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				try {
					if(!Keepalive.this.keepAliveReceived)
					{
						Log.w(TAG, "Diddent receive keepalive in time");
						pinger.cancel();
						handler.sendEmptyMessage(0);
					}
					else
					{
						keepAliveReceived = false;
						Log.v(TAG, "Sending new keepalive");
					}
					sendKeepalive();
				} catch (Exception e) {
					handler.sendEmptyMessage(1);
				}
			}
		}, 0, 10000);
	}
	
	@Override
	public void onDataReady(int length) throws IOException {
		port.skipRead(length);
		this.keepAliveReceived = true;
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
