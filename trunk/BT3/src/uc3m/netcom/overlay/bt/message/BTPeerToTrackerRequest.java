package uc3m.netcom.overlay.bt.message;

import de.tud.kom.p2psim.api.overlay.OverlayID;
import de.tud.kom.p2psim.api.overlay.OverlayKey;
import de.tud.kom.p2psim.api.transport.TransProtocol;
import uc3m.netcom.overlay.bt.BTConstants;
import uc3m.netcom.overlay.bt.BTContact;
import uc3m.netcom.overlay.bt.BTInternStatistic;

public class BTPeerToTrackerRequest extends BTMessage {
	
	public enum Reason {STARTED, STOPPED, COMPLETED, EMPTY}
	
	private BTInternStatistic itsStatistic;
	
	private Reason itsReason;
	
	private OverlayKey itsDocument;
	
	private BTContact itsP2PAddress;
	
	/**
	 * The number of contacts, this peers wants to get from the server.
	 * A number smaller than 0 means: Use the standard value.
	 */
	private int itsNumberOfRequestedPeers;
	
	private static Type theirType = Type.TRACKER_REQUEST;
	
	private static TransProtocol theirTransportProtocol = BTConstants.MESSAGE_SERVICE_CATEGORY_PEER_TO_TRACKER;
	
	
	public BTPeerToTrackerRequest(Reason theReason, int theNumberOfRequestedPeers, OverlayKey theDocument, BTContact theP2PAddress, BTInternStatistic theStatistic, OverlayID theSender, OverlayID theReceiver) {
		super(theirType, theirTransportProtocol, true, 0, theSender, theReceiver);
		//TODO: Calculate the size!
		//Size calculation is much more difficult for tracker communication. But it only is a small part of the whole protocol overhead.
		this.initialize(theReason, theNumberOfRequestedPeers, theDocument, theP2PAddress, theStatistic);
	}
	
	public BTPeerToTrackerRequest(Reason theReason, int theNumberOfRequestedPeers, OverlayKey theDocument, BTContact theP2PAddress, OverlayID theSender, OverlayID theReceiver) {
		super(theirType, theirTransportProtocol, true, 0, theSender, theReceiver);
		this.initialize(theReason, theNumberOfRequestedPeers, theDocument, theP2PAddress, null);
	}
	
	/**
	 * This constructor creates a message that doesn't request for new contacts.
	 * @param theReason
	 * @param theDocument
	 * @param theP2PAddress
	 * @param theStatistic
	 * @param theSender
	 * @param theReceiver
	 */
	public BTPeerToTrackerRequest(Reason theReason, OverlayKey theDocument, BTContact theP2PAddress, BTInternStatistic theStatistic, OverlayID theSender, OverlayID theReceiver) {
		super(theirType, theirTransportProtocol, true, 0, theSender, theReceiver);
		this.initialize(theReason, 0, theDocument, theP2PAddress, theStatistic);
	}
	
	private void initialize(Reason theReason, int theNumberOfRequestedPeers, OverlayKey theDocument, BTContact theP2PAddress, BTInternStatistic theStatistic) {
		if (theReason == null)
			throw new RuntimeException("'theReason' must not be 'null'!");
		if (theDocument == null)
			throw new RuntimeException("'theDocument' must not be 'null'!");
		if (theP2PAddress == null)
			throw new RuntimeException("'theP2PAddress' must not be 'null'!");
		this.itsReason = theReason;
		this.itsNumberOfRequestedPeers = theNumberOfRequestedPeers;
		this.itsDocument = theDocument;
		this.itsP2PAddress = theP2PAddress;
		this.itsStatistic = theStatistic;
	}
	
	
	
	public Reason getReason() {
		return this.itsReason;
	}
	
	public BTInternStatistic getStatistic() {
		return this.itsStatistic;
	}
	
	public OverlayKey getDocument() {
		return this.itsDocument;
	}
	
	public BTContact getP2PAddress() {
		return this.itsP2PAddress;
	}
	
	public int getNumberOfRequestedPeers() {
		return this.itsNumberOfRequestedPeers;
	}
	
	public static Type getStaticType() {
		return theirType;
	}
	
	public static TransProtocol getStaticTransportProtocol() {
		return theirTransportProtocol;
	}
	
}
