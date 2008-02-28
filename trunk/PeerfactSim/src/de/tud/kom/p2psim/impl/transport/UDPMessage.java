package de.tud.kom.p2psim.impl.transport;

import de.tud.kom.p2psim.api.common.Message;
import de.tud.kom.p2psim.api.transport.TransProtocol;

public class UDPMessage extends AbstractTransMessage {
	/** Packet header size. */
	public static final int HEADER_SIZE = 8;

	public UDPMessage(Message payload, short senderPort, short receiverPort,
			int commId, boolean isReply) {
		this.payload = payload;
		this.srcPort = senderPort;
		this.dstPort = receiverPort;
		this.commId = commId;
		this.protocol = TransProtocol.UDP;
		this.isReply = isReply;
	}

	public Message getPayload() {
		return this.payload;
	}

	public long getSize() {
		return HEADER_SIZE + this.payload.getSize();
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return ("[ UDP " + this.srcPort + " -> " + this.dstPort + " | size: "
				+ HEADER_SIZE + " + " + this.payload.getSize()
				+ " bytes | payload-hash: " + this.payload.hashCode() + " ]");
	}

}
