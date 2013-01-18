package nl.ict.aapbridge.bridge;

import java.io.IOException;

import nl.ict.aapbridge.bridge.AccessoryBridge.BridgeService;
import nl.ict.aapbridge.bridge.AccessoryBridge.Port;

public class ServiceSpawner implements BridgeService{

	public ServiceSpawner() {
		
	}

	@Override
	public void onDataReady(int length) throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public Port getPort() {
		return null;
	}

}
