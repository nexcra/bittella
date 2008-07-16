package uc3m.netcom.overlay.bt.message;

import uc3m.netcom.overlay.bt.BTID;
import uc3m.netcom.transport.TransProtocol;
import uc3m.netcom.overlay.bt.BTConstants;

public class BTPeerMessageKeepAlive extends BTMessage {
	
	private String itsString;
	
	private static Type theirType = Type.KEEPALIVE;
	
	private static TransProtocol theirTransportProtocol = BTConstants.MESSAGE_SERVICE_CATEGORY_KEEP_ALIVE;
	
	public BTPeerMessageKeepAlive(String theString, BTID theSender, BTID theReceiver) {
		super(theirType, theirTransportProtocol, true, 4, theSender, theReceiver);
		this.itsString = theString;
                this.type = BTMessage.KEEPALIVE;
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
