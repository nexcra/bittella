package de.tud.kom.p2psim.overlay.bt.message;

import de.tud.kom.p2psim.api.overlay.OverlayID;
import de.tud.kom.p2psim.api.overlay.OverlayKey;
import de.tud.kom.p2psim.api.transport.TransProtocol;
import de.tud.kom.p2psim.overlay.bt.BTConnection;
import de.tud.kom.p2psim.overlay.bt.BTConstants;

public class BTPeerMessageHandshake extends BTMessage {
	
	private OverlayKey itsOverlayKey;
	
	private BTConnection itsSenderConnection;
	
	private static Type theirType = Type.HANDSHAKE;
	
	private static TransProtocol theirTransportProtocol = BTConstants.MESSAGE_SERVICE_CATEGORY_HANDSHAKE;
	
	public BTPeerMessageHandshake(OverlayKey theOverlayKey, BTConnection theSenderConnection, OverlayID theSender, OverlayID theReceiver) {
		super(theirType, theirTransportProtocol, true, (1 + 19 + 8 + 20 + 20), theSender, theReceiver);
//		super(true, 0, theSender, theSenderPort, theDestination, theirMessageCategory);
		//Lok at the BitTorrent specification, if you want to know the reasons for this size.
		this.itsOverlayKey = theOverlayKey;
		this.itsSenderConnection = theSenderConnection;
	}
	
	public OverlayKey getOverlayKey() {
		return this.itsOverlayKey;
	}
	
	public BTConnection getSenderConnection() {
		return this.itsSenderConnection;
	}
	
	public static Type getStaticType() {
		return theirType;
	}
	
	public static TransProtocol getStaticTransportProtocol() {
		return theirTransportProtocol;
	}
	
}
