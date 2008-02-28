package de.tud.kom.p2psim.impl.transport;

import de.tud.kom.p2psim.api.common.Message;
import de.tud.kom.p2psim.api.transport.TransProtocol;
import de.tud.kom.p2psim.impl.network.gnp.IPv4Message;

public class TCPMessage extends AbstractTransMessage {
	/** Packet header size. */
	public static final int HEADER_SIZE = 20;

	private long sequenzNumber = 0;
	
	private int maxPayloadPerSegment = IPv4Message.MTU_SIZE - IPv4Message.HEADER_SIZE - TCPMessage.HEADER_SIZE;
	private int numberOfSegments = 0;

	public TCPMessage(Message payload, short srcPort, short dstPort, int commId, boolean isReply, long sequenzNumber) {
		this.payload = payload;
		this.srcPort = srcPort;
		this.dstPort = dstPort;
		this.commId = commId;
		this.protocol = TransProtocol.TCP;
		this.isReply = isReply;
		this.sequenzNumber = sequenzNumber;
		
		this.numberOfSegments =  (int) Math.ceil((double) getPayload().getSize() / (double) (maxPayloadPerSegment));

	}

	public Message getPayload() {
		return this.payload;
	}

	public long getSize() {
		return numberOfSegments*HEADER_SIZE + this.payload.getSize();
	}

	public long getSequenzNumber() {
		return sequenzNumber;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return ("[ TCP " + this.srcPort + " -> " + this.dstPort + " | sn: "
				+ sequenzNumber + " | size: " + numberOfSegments + "*" + HEADER_SIZE + " + "
				+ this.payload.getSize() + " bytes | payload-hash: "
				+ this.payload.hashCode() + " ]");
	}
}
