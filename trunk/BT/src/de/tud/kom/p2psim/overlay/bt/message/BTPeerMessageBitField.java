package de.tud.kom.p2psim.overlay.bt.message;

import java.util.BitSet;

import de.tud.kom.p2psim.api.overlay.OverlayID;
import de.tud.kom.p2psim.api.overlay.OverlayKey;
import de.tud.kom.p2psim.api.transport.TransProtocol;
import de.tud.kom.p2psim.overlay.bt.BTConstants;

public class BTPeerMessageBitField extends BTMessage {
	
	private BitSet itsBitset;
	
	/**
	 * The overlaykey of the document.
	 * Just for debugging. BitTorrent doesn't send this information.
	 */
	private OverlayKey itsOverlayKey;
	
	private static Type theirType = Type.BITFIELD;
	
	private static TransProtocol theirTransportProtocol = BTConstants.MESSAGE_SERVICE_CATEGORY_BITFIELD;
	
	public BTPeerMessageBitField(BitSet theBitset, OverlayKey theOverlayKey, OverlayID theSender, OverlayID theReceiver) {
		super(theirType, theirTransportProtocol, true, 4 + 1 + (theBitset.size() / 8), theSender, theReceiver);
//		super(true, 0, theSender, theSenderPort, theDestination, theirMessageCategory);
		this.itsOverlayKey = theOverlayKey;
		this.itsBitset = theBitset;
	}
	
	public BitSet getBitset() {
		return this.itsBitset;
	}
	
	public OverlayKey getOverlayKey() {
		return this.itsOverlayKey;
	}
	
	public static Type getStaticType() {
		return theirType;
	}
	
	public static TransProtocol getStaticTransportProtocol() {
		return theirTransportProtocol;
	}
	
}
