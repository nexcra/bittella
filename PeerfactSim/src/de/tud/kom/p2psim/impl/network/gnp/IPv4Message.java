package de.tud.kom.p2psim.impl.network.gnp;

import de.tud.kom.p2psim.api.common.Message;
import de.tud.kom.p2psim.api.network.NetID;
import de.tud.kom.p2psim.api.network.NetProtocol;
import de.tud.kom.p2psim.impl.network.AbstractNetMessage;
import de.tud.kom.p2psim.impl.transport.TCPMessage;
import de.tud.kom.p2psim.impl.transport.UDPMessage;

/**
 * Implements the NetworkMessage-Interface for the complex network model.
 * 
 * @author Sebastian Kaune
 */
public class IPv4Message extends AbstractNetMessage {

	/** Packet header size. */
	public static final int HEADER_SIZE = 20;
	public static final int MAX_IP_PACKET_SIZE = 65535;
	public static final int MTU_SIZE = 1500;

	private int noOfFragments = 1;
	private long size = 0;

	public IPv4Message(Message payload, NetID receiver, NetID sender) {
		super(payload, receiver, sender, NetProtocol.IPv4);
		

		noOfFragments = (int) Math.ceil((double) getPayload().getSize() / (double) (MTU_SIZE - HEADER_SIZE));
		size = getPayload().getSize() + HEADER_SIZE * noOfFragments;


		
	}

	public long getSize() {
		return size;
	}

	public int getNoOfFragments() {
		return noOfFragments;
	}

	@Override
	public String toString() {
		return "[ IP " + super.getSender() + " -> " + super.getReceiver()
				+ " | size: " + getSize() + " ( " + noOfFragments + "*"
				+ HEADER_SIZE + " + " + getPayload().getSize()
				+ " ) bytes | payload: " + getPayload() + " ]";
	}

}