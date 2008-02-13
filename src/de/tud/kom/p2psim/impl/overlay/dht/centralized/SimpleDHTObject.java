package de.tud.kom.p2psim.impl.overlay.dht.centralized;

import de.tud.kom.p2psim.api.overlay.DHTObject;
import de.tud.kom.p2psim.api.overlay.OverlayKey;
import de.tud.kom.p2psim.api.transport.TransInfo;

public class SimpleDHTObject implements DHTObject {
	TransInfo addr;

	OverlayKey key;

	public SimpleDHTObject(OverlayKey key, TransInfo addr) {
		super();
		this.key = key;
		this.addr = addr;
	}

	public OverlayKey getKey() {
		return key;
	}

	@Override
	public String toString() {
		return "SimpleDHTObject( key: " + key + " TransInfo: " + addr + " )";
	}

	TransInfo getAddress() {
		return addr;
	}

}
