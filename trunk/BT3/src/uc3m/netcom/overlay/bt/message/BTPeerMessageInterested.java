package uc3m.netcom.overlay.bt.message;

import uc3m.netcom.overlay.bt.BTID;
import uc3m.netcom.transport.TransProtocol;
import uc3m.netcom.overlay.bt.BTConstants;

public class BTPeerMessageInterested extends BTMessage {
	
	private String itsOverlayKey;
	
	private static Type theirType = Type.INTERESTED;
	
	private static TransProtocol theirTransportProtocol = BTConstants.MESSAGE_SERVICE_CATEGORY_INTERESTED;
	
	public BTPeerMessageInterested(String theOverlayKey, BTID theSender, BTID theReceiver) {
		super(theirType, theirTransportProtocol, true, 4 + 1, theSender, theReceiver);
//		super(true, 0, theSender, theSenderPort, theDestination, theirMessageCategory);
		this.itsOverlayKey = theOverlayKey;
                this.type = BTMessage.INTERESTED;
	}
	
	public String getOverlayKey() {
		return this.itsOverlayKey;
	}
	
	public static Type getStaticType() {
		return theirType;
	}
	
	public static TransProtocol getStaticTransportProtocol() {
		return theirTransportProtocol;
	}
	
}
