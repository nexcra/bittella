package de.tud.kom.p2psim.overlay.bt.message;

import org.apache.log4j.Logger;

import de.tud.kom.p2psim.api.overlay.OverlayID;
import de.tud.kom.p2psim.api.overlay.OverlayKey;
import de.tud.kom.p2psim.api.transport.TransProtocol;
import de.tud.kom.p2psim.impl.util.logging.SimLogger;
import de.tud.kom.p2psim.overlay.bt.BTConstants;

public class BTPeerMessagePiece extends BTMessage {
	
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
	
	private static Type theirType = Type.PIECE;
	
	private static TransProtocol theirTransportProtocol = BTConstants.MESSAGE_SERVICE_CATEGORY_PIECE;
	
	static final Logger log = SimLogger.getLogger(BTPeerMessagePiece.class);
	
	
	
	public BTPeerMessagePiece(int thePieceNumber, int theBlockNumber, int theSize, OverlayKey theOverlayKey, boolean theData, OverlayID theSender, OverlayID theReceiver) {
		super(theirType, theirTransportProtocol, theData, 4 + 1 + 4 + 4 + theSize, theSender, theReceiver);
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
	
	@Override
	public String toString() {
		return "[BTPieceMessage| Piece: " + this.itsPieceNumber + "; Block: " + this.itsBlockNumber + "|" + super.toString() + "]";
	}
	
}
