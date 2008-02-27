package de.tud.kom.p2psim.overlay.bt.message;

import de.tud.kom.p2psim.api.overlay.OverlayID;
import de.tud.kom.p2psim.api.overlay.OverlayKey;
import de.tud.kom.p2psim.api.transport.TransProtocol;
import de.tud.kom.p2psim.overlay.bt.BTConstants;

public class BTPeerMessageCancel extends BTMessage {
	
	/**
	 * The number of the piece, this block of data is part of.
	 */
	private int itsPieceNumber;
	
	/**
	 * The number of the block in the piece.
	 */
	private int itsBlockNumber;
	
	/**
	 * The overlaykey of the document.
	 */
	private OverlayKey itsOverlayKey;
	
	private static Type theirType = Type.CANCEL;
	
	private static TransProtocol theirTransportProtocol = BTConstants.MESSAGE_SERVICE_CATEGORY_CANCEL;
	
	public BTPeerMessageCancel(int thePieceNumber, int theBlockNumber, OverlayKey theOverlayKey, OverlayID theSender, OverlayID theReceiver) {
		super(theirType, theirTransportProtocol, true, 4 + 1 + 4 + 4, theSender, theReceiver);
//		super(true, 0, theSender, theSenderPort, theDestination, theirMessageCategory);
		this.itsPieceNumber = thePieceNumber;
		this.itsBlockNumber = theBlockNumber;
		this.itsOverlayKey = theOverlayKey;
	}
	
	public int getPieceNumber() {
		return this.itsPieceNumber;
	}
	
	public int getBlockNumber() {
		return this.itsBlockNumber;
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
