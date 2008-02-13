package de.tud.kom.p2psim.impl.transport;

import de.tud.kom.p2psim.api.common.Message;
import de.tud.kom.p2psim.api.transport.TransProtocol;

public class TCPMessage extends AbstractTransMessage {
	/** Packet header size. */
	public static final int HEADER_SIZE = 20;

	public TCPMessage(Message payload, short srcPort, short dstPort, int commId, boolean isReply) {
		this.payload = payload;
		this.srcPort = srcPort;
		this.dstPort = dstPort;
		this.commId = commId;
		this.protocol = TransProtocol.TCP;
		this.isReply = isReply;
	}

	public Message getPayload() {
		return this.payload;
	}

	public long getSize() {
		return HEADER_SIZE + this.payload.getSize();
	}

	/** {@inheritDoc} */
	public String toString() {
		return ("tcp " + this.srcPort + " " + this.dstPort + " " + this.payload);
	}
}
