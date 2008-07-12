package uc3m.netcom.overlay.bt.message;

//import de.tud.kom.p2psim.api.overlay.OverlayID;
//import de.tud.kom.p2psim.api.overlay.OverlayKey;
//import de.tud.kom.p2psim.api.transport.TransProtocol;
import uc3m.netcom.overlay.bt.BTID;
import uc3m.netcom.transport.TransProtocol;
import uc3m.netcom.overlay.bt.BTConstants;

public class BTPeerMessageHave extends BTMessage {
	
	private int itsPieceNumber;
	
	private String itsOverlayKey;
	
	private static Type theirType = Type.HAVE;
	
	private static TransProtocol theirTransportProtocol = BTConstants.MESSAGE_SERVICE_CATEGORY_HAVE;
	
	public BTPeerMessageHave(int thePieceNumber, String theOverlayKey, BTID theSender, BTID theReceiver) {
		super(theirType, theirTransportProtocol, true, 4 + 1 + 4, theSender, theReceiver);
//		super(true, 0, theSender, theSenderPort, theDestination, theirMessageCategory);
		this.itsPieceNumber = thePieceNumber;
		this.itsOverlayKey = theOverlayKey;
	}
	
	public int getPieceNumber() {
		return this.itsPieceNumber;
	}
	
	public String getOverlayKey() {
		return this.itsOverlayKey;
	}
	
	@Override
	public String toString() {
		return "[BTPeerMessageHave| Piece number: " + String.valueOf(this.itsPieceNumber) + "|" + super.toString() + "]";
	}
	
	public static Type getStaticType() {
		return theirType;
	}
	
	public static TransProtocol getStaticTransportProtocol() {
		return theirTransportProtocol;
	}
	
}
