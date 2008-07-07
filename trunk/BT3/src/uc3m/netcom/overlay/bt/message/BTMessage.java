package uc3m.netcom.overlay.bt.message;

import de.tud.kom.p2psim.api.common.Message;
import de.tud.kom.p2psim.api.overlay.OverlayID;
import de.tud.kom.p2psim.api.transport.TransProtocol;
import de.tud.kom.p2psim.impl.overlay.AbstractOverlayMessage;
import de.tud.kom.p2psim.impl.simengine.Simulator;

/**
 * This is the abstract super class of all BitTorrent Messages.
 * @author Jan Stolzenburg
 */
public abstract class BTMessage extends AbstractOverlayMessage<OverlayID> {
	
	/**
	 * The BitTorrent type of the message.
	 * @author Jan Stolzenburg
	 */
	public enum Type {CHOKE, UNCHOKE, INTERESTED, UNINTERESTED, HAVE, BITFIELD, REQUEST, PIECE, CANCEL, KEEPALIVE, HANDSHAKE, TRACKER_REQUEST, TRACKER_REPLY}
	
	/**
	 * The BitTorrent type of the message.
	 */
	private Type itsType;
	
	private TransProtocol itsTransportProtocol;
	
	private int itsSize;
	
	/**
	 * Stores, if the data got corrupted or not.
	 */
	private boolean itsDataCorrect;
	
	/**
	 * When has the message bin created and send?
	 */
	private long itsTimestamp;
	
//	private boolean received;
	
	public BTMessage(Type theType, TransProtocol theTransportProtocol, boolean theDataCorrect, int theSize, OverlayID theSender, OverlayID theReceiver) {
		super(theSender, theReceiver);
		this.itsSize = theSize;
		this.itsType = theType;
		this.itsTransportProtocol = theTransportProtocol;
		this.itsDataCorrect = theDataCorrect;
		this.itsTimestamp = Simulator.getCurrentTime();
//		this.received = false;
	}
	
	public boolean isDataCorrect() {
		return this.itsDataCorrect;
	}
	
	/**
	 * Call this method, if you send this message with delay after its creation.
	 */
	public void updateTimestamp() {
		this.itsTimestamp = Simulator.getCurrentTime();
//		this.received = false;
	}
	
	public long getTimestamp() {
		return this.itsTimestamp;
	}
	
//	/**
//	 * Call this method, when you receive this message.
//	 */
//	public void received() {
//		this.received = true;
//	}
//	
//	public boolean isReceived() {
//		return this.received;
//	}
	
	/**
	 * @return the BitTorrent message type.
	 */
	public Type getType() {
		return this.itsType;
	}
	
	public TransProtocol getTransportProtocol() {
		return this.itsTransportProtocol;
	}
	
	@Override
	public String toString() {
		return "[BTMessage| Type: " + this.getType() + "; Size: " + this.getSize() + "; From: " + this.getSender() + "; To: " + this.getReceiver() + "]";
	}
	
	public Message getPayload() {
		return null;
	}

	public long getSize() {
		return this.itsSize;
	}
	
}
