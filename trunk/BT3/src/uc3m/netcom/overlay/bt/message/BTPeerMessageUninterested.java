package uc3m.netcom.overlay.bt.message;

//import de.tud.kom.p2psim.api.overlay.OverlayID;
//import de.tud.kom.p2psim.api.overlay.OverlayKey;
//import de.tud.kom.p2psim.api.transport.TransProtocol;
import uc3m.netcom.transport.TransProtocol;
import uc3m.netcom.overlay.bt.BTID;
import uc3m.netcom.overlay.bt.BTConstants;

public class BTPeerMessageUninterested extends BTMessage {
	
	private String itsOverlayKey;
	
	private static Type theirType = Type.UNINTERESTED;
	
	private static TransProtocol theirTransportProtocol = BTConstants.MESSAGE_SERVICE_CATEGORY_UNINTERESTED;
	
	public BTPeerMessageUninterested(String theOverlayKey, BTID theSender, BTID theReceiver) {
		super(theirType, theirTransportProtocol, true, 4 + 1, theSender, theReceiver);
//		super(true, 0, theSender, theSenderPort, theDestination, theirMessageCategory);
		this.itsOverlayKey = theOverlayKey;
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
