package de.tud.kom.p2psim.impl.overlay.cd.messages;

import de.tud.kom.p2psim.api.common.Message;
import de.tud.kom.p2psim.api.storage.Document;

public class DownloadResultMsg implements Message{

	private Document doc;

	public DownloadResultMsg(Document doc) {
		this.doc = doc;
	}

	public Message getPayload() {
		return null;
	}

	public long getSize() {
		return 0;
	}

	@Override
	public String toString() {
		return "DownloadResultMsg (" + doc + ")";
	}

	public Document getDoc() {
		return doc;
	}

}
