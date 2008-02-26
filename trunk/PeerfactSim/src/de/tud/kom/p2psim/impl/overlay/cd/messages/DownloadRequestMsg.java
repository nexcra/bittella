package de.tud.kom.p2psim.impl.overlay.cd.messages;

import de.tud.kom.p2psim.api.common.Message;
import de.tud.kom.p2psim.api.overlay.OverlayKey;

public class DownloadRequestMsg implements Message {

	private OverlayKey key;

	public DownloadRequestMsg(OverlayKey key) {
		this.key = key;
	}

	public Message getPayload() {
		return null;
	}

	public long getSize() {
		return 0;
	}

	public OverlayKey getKey() {
		return key;
	}

	@Override
	public String toString() {
		return "DownloadRequestMsg (" + key + ")";
	}

}
