package de.tud.kom.p2psim.impl.network.gnp;

import de.tud.kom.p2psim.api.common.Message;
import de.tud.kom.p2psim.api.network.NetID;
import de.tud.kom.p2psim.api.network.NetProtocol;
import de.tud.kom.p2psim.impl.network.AbstractNetMessage;

/**
 * Implements the NetworkMessage-Interface for the complex network model.
 * 
 * @author Sebastian Kaune
 */
public class IPv4Message extends AbstractNetMessage {

	public IPv4Message(Message payload, NetID receiver, NetID sender, NetProtocol netProtocol) {
		super(payload, receiver, sender, netProtocol);
	}

	public long getSize() {
		return this.getPayload().getSize();
	}

}