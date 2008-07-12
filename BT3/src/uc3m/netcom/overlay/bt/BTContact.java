package uc3m.netcom.overlay.bt;

import uc3m.netcom.transport.TransInfo;

/**
 * The basic knowledge of one peer about another peer: Its address and overlay id.
 * @author Jan Stolzenburg
 */
public class BTContact {
	
	private BTID itsOverlayID;
	
	private TransInfo itsTransInfo;
	
	public BTContact(BTID theOverlayID, TransInfo theTransInfo) {
		if (theOverlayID == null)
			throw new RuntimeException("'theOverlayID' must not be 'null'!");
		if (theTransInfo == null)
			throw new RuntimeException("'theTransInfo' must not be 'null'!");
		this.itsOverlayID = theOverlayID;
		this.itsTransInfo = theTransInfo;
	}
	
	public BTID getOverlayID() {
		return this.itsOverlayID;
	}
	
	public TransInfo getTransInfo() {
		return this.itsTransInfo;
	}
	
	@Override
	public boolean equals(Object theOther) {
		if (! (theOther instanceof BTContact))
			return false;
		BTContact aOther = (BTContact) theOther;
		return (this.getOverlayID().equals(aOther.getOverlayID()) && (this.getTransInfo().getNetId().equals(aOther.getTransInfo().getNetId()) && this.getTransInfo().getPort() == aOther.getTransInfo().getPort()));
	}
	
	@Override
	public int hashCode() {
		return this.getOverlayID().hashCode() ^ (this.getTransInfo().getNetId().hashCode() ^ this.getTransInfo().getPort());
	}
	
	@Override
	public String toString() {
		return "[BTContact| OverlayID: " + this.getOverlayID() + "; TransInfo: " + this.getTransInfo() + "]";
	}
	
}
