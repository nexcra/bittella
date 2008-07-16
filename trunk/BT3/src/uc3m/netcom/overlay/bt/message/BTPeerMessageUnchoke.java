package uc3m.netcom.overlay.bt.message;

import uc3m.netcom.overlay.bt.BTID;
import uc3m.netcom.overlay.bt.BTConstants;
import uc3m.netcom.transport.TransProtocol;

public class BTPeerMessageUnchoke extends BTMessage {
	
	private String itsString;
	
	private static Type theirType = Type.UNCHOKE;
	
	private static TransProtocol theirTransportProtocol = BTConstants.MESSAGE_SERVICE_CATEGORY_UNCHOKE;
	
	public BTPeerMessageUnchoke(String theString, BTID theSender, BTID theReceiver) {
		super(theirType, theirTransportProtocol, true, 4 + 1, theSender, theReceiver);
//		super(true, 0, theSender, theSenderPort, theDestination, theirMessageCategory);
		this.itsString = theString;
                this.type = BTMessage.UNCHOKE;
	}
	
	public String getString() {
		return this.itsString;
	}
	
	public static Type getStaticType() {
		return theirType;
	}
	
	public static TransProtocol getStaticTransportProtocol() {
		return theirTransportProtocol;
	}
	
}
