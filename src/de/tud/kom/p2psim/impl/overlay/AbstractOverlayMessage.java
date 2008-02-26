package de.tud.kom.p2psim.impl.overlay;

import de.tud.kom.p2psim.api.common.Message;
import de.tud.kom.p2psim.api.overlay.OverlayID;

public abstract class AbstractOverlayMessage<T extends OverlayID> implements Message {

	private T sender;

	private T receiver;

	public AbstractOverlayMessage(T sender, T receiver) {
		super();
		this.sender = sender;
		this.receiver = receiver;
	}

	public T getReceiver() {
		return receiver;
	}

	public T getSender() {
		return sender;
	}

}
