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
                String id1 = new java.math.BigInteger(this.getOverlayID().getID()).toString(16);
                String id2 = new java.math.BigInteger(aOther.getOverlayID().getID()).toString(16);
                
                if(id1.equalsIgnoreCase(id2)){
                    if(this.getTransInfo().getNetId().equals(aOther.getTransInfo().getNetId())){
                        if(this.getTransInfo().getPort() == aOther.getTransInfo().getPort()) return true;
                        else System.out.println("Different Port");
                    }else System.out.println("Differentt IPs");
                }else{
                    System.out.println("Different IDs");
                }
		return false;
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
