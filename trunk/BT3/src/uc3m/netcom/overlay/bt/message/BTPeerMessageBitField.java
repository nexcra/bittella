package uc3m.netcom.overlay.bt.message;

import java.util.BitSet;

import uc3m.netcom.overlay.bt.BTID;
//import de.tud.kom.p2psim.api.overlay.OverlayKey;
import uc3m.netcom.transport.TransProtocol;
import uc3m.netcom.overlay.bt.BTConstants;

public class BTPeerMessageBitField extends BTMessage {
	
	private BitSet itsBitset;
	
	/**
	 * The overlaykey of the document.
	 * Just for debugging. BitTorrent doesn't send this information.
	 */
	private String itsOverlayKey;
	
	private static Type theirType = Type.BITFIELD;
	
	private static TransProtocol theirTransportProtocol = BTConstants.MESSAGE_SERVICE_CATEGORY_BITFIELD;
	
	public BTPeerMessageBitField(BitSet theBitset, String theOverlayKey, BTID theSender, BTID theReceiver) {
		super(theirType, theirTransportProtocol, true, 4 + 1 + (theBitset.size() / 8), theSender, theReceiver);
//		super(true, 0, theSender, theSenderPort, theDestination, theirMessageCategory);
		this.itsOverlayKey = theOverlayKey;
		this.itsBitset = theBitset;
                this.type = BTMessage.BITFIELD;
	}
	
	public BitSet getBitset() {
		return this.itsBitset;
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
