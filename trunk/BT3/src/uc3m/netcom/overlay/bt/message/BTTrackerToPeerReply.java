package uc3m.netcom.overlay.bt.message;

import java.util.Collection;

import uc3m.netcom.transport.TransProtocol;
import uc3m.netcom.overlay.bt.BTID;
import uc3m.netcom.overlay.bt.BTConstants;
import uc3m.netcom.overlay.bt.BTContact;

public class BTTrackerToPeerReply extends BTMessage {
	
	private Collection<BTContact> itsNewPeerSet;
	
	private static Type theirType = Type.TRACKER_REPLY;
	
	private static TransProtocol theirTransportProtocol = BTConstants.MESSAGE_SERVICE_CATEGORY_TRACKER_TO_PEER;
	
	public BTTrackerToPeerReply(Collection<BTContact> theNewPeerSet, BTID theSender, BTID theReceiver) {
		super(theirType, theirTransportProtocol, true, 0, theSender, theReceiver);
		//TODO: Calculate the size!
		//Size calculation is much more difficult for tracker communication. But it only is a small part of the whole protocol overhead.
		this.itsNewPeerSet = theNewPeerSet;
	}
	
	public Collection<BTContact> getNewPeerSet() {
		return this.itsNewPeerSet;
	}
	
	public static Type getStaticType() {
		return theirType;
	}
	
	public static TransProtocol getStaticTransportProtocol() {
		return theirTransportProtocol;
	}
	
}
