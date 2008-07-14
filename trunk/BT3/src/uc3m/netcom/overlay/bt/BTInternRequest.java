package uc3m.netcom.overlay.bt;

//import de.tud.kom.p2psim.api.overlay.OverlayKey;
//import de.tud.kom.p2psim.impl.simengine.Simulator;

/**
 * This class is the peer intern representation of a request.
 * @author Jan Stolzenburg
 */
public class BTInternRequest {
	
	/**
	 * Which peer is requesting the data?
	 */
	private BTContact itsRequestingPeer;
	
	/**
	 * Which peer is requested for the data?
	 */
	private BTContact itsRequestedPeer;
	
	/**
	 * About which documents do we talk?
	 */
	private String itsOverlayKey;
	
	/**
	 * The piece number in which the requested block is located.
	 */
	private int itsPieceNumber;
	
	/**
	 * The number of the requested block.
	 */
	private int itsBlockNumber;
	
	/**
	 * At what time has this request been created?
	 */
	private long itsCreationTime;
	
	public BTInternRequest(BTContact theRequestingPeer, BTContact theRequestedPeer, String theOverlayKey, int thePieceNumber, int theBlockNumber) {
		this.itsRequestingPeer = theRequestingPeer;
		this.itsRequestedPeer = theRequestedPeer;
		this.itsOverlayKey = theOverlayKey;
		this.itsPieceNumber = thePieceNumber;
		this.itsBlockNumber = theBlockNumber;
		this.itsCreationTime = System.currentTimeMillis(); //To distinguish between repeated requests.
	}
	
	public String getOverlayKey() {
		return this.itsOverlayKey;
	}
	
	public int getPieceNumber() {
		return this.itsPieceNumber;
	}
	
	public int getBlockNumber() {
		return this.itsBlockNumber;
	}
	
	public BTContact getRequestedPeer() {
		return this.itsRequestedPeer;
	}
	
	public BTContact getRequestingPeer() {
		return this.itsRequestingPeer;
	}
	
	public long getCreationTime() {
		return this.itsCreationTime;
	}
	
	public void setRequestingPeer(BTContact theRequestingPeer) {
		this.itsRequestingPeer = theRequestingPeer;
	}
	
	public void setRequestedPeer(BTContact theRequestedPeer) {
		this.itsRequestedPeer = theRequestedPeer;
	}
	
	@Override
	public boolean equals(Object theOther) {
		if (! (theOther instanceof BTInternRequest))
			return false;
		BTInternRequest otherRequest = (BTInternRequest) theOther;
		if (! this.itsOverlayKey.equals(otherRequest.itsOverlayKey))
			return false;
		if (! this.itsRequestedPeer.equals(otherRequest.itsRequestedPeer))
			return false;
		if (! this.itsRequestingPeer.equals(otherRequest.itsRequestingPeer))
			return false;
		if (this.itsPieceNumber != otherRequest.itsPieceNumber)
			return false;
		if (this.itsBlockNumber != otherRequest.itsBlockNumber)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "[BTInternRequest| From: '" + this.itsRequestingPeer.getOverlayID() + "'; To: '" + this.itsRequestedPeer.getOverlayID() + "'; Document: '" + this.itsOverlayKey + "'; Piece: '" + this.itsPieceNumber + "'; Block: '" + this.itsBlockNumber + "'; Time: '" + this.itsCreationTime + "']";
	}
	
}
