package uc3m.netcom.overlay.bt.message;

import de.tud.kom.p2psim.api.overlay.OverlayID;
import de.tud.kom.p2psim.api.transport.TransProtocol;
import uc3m.netcom.overlay.bt.BTConstants;
import uc3m.netcom.overlay.bt.BTInternRequest;

public class BTPeerMessageRequest extends BTMessage {
	
	private BTInternRequest itsRequest;
	
	private static Type theirType = Type.REQUEST;
	
	private static TransProtocol theirTransportProtocol = BTConstants.MESSAGE_SERVICE_CATEGORY_REQUEST;
	
	public BTPeerMessageRequest(BTInternRequest theRequest, OverlayID theSender, OverlayID theReceiver) {
		super(theirType, theirTransportProtocol, true, 4 + 1 + 4 + 4 + 4, theSender, theReceiver);
//		super(true, 0, theSender, theSenderPort, theDestination, theirMessageCategory);
		this.itsRequest = theRequest;
	}
	
	public BTInternRequest getRequest() {
		return this.itsRequest;
	}
	
	@Override
	public String toString() {
		return "[BTPeerMessageRequest| Requests: " + this.itsRequest.toString() + "|" + super.toString() + "]";
	}
	
	public static Type getStaticType() {
		return theirType;
	}
	
	public static TransProtocol getStaticTransportProtocol() {
		return theirTransportProtocol;
	}
	
}
