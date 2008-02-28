package de.tud.kom.p2psim.overlay.bt;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Logger;

import de.tud.kom.p2psim.api.overlay.OverlayKey;
import de.tud.kom.p2psim.api.storage.Document;
import de.tud.kom.p2psim.impl.util.logging.SimLogger;

/**
 * This class represents the file, that the peers try to download.
 * Like a real file, it has a size, a hash value (OverlayKey) and a download state.
 * But it also handels very BitTorrent specific details:
 * piece exponent and block exponent. These two are used for dividing the document in smaller parts.
 * @author Jan Stolzenburg
 */
public class BTDocument implements Document {
	/*
	 * TODO: Die festen Grenzen errechnen und angeben, die sich aus der Implementierung ergeben:
	 * Anzahl Pieces, größe Pieces, Anzahl Blöcke, größe Blöcke, Faktoren aus mehreren der Zahlen...
	 */
	
	
	/**
	 * With this interface, other classes can be notified, if the download finishes.
	 * Use the method <code>registerDocumentFinished</code> to register such a class.
	 */
	public interface BTDocumentFinishedListener {
		
		public void documentFinished();
		
	}
	
	/**
	 * With this interface, other classes can be notified, if the download of a piece finishes.
	 * Use the method <code>registerPieceFinished</code> to register such a class.
	 */
	public interface BTPieceFinishedListener {
		
		public void pieceFinished(int thePieceNumber);
		
	}
	
	/**
	 * With this interface, other classes can be notified, if the download of a block finishes.
	 * Use the method <code>registerBlockFinished</code> to register such a class.
	 */
	public interface BTBlockFinishedListener {
		
		public void blockFinished(int thePieceNumber, int theBlockNumber);
		
	}
	
	
	
	//Variables/Members and Constants
	
	private State itsState;
	
	private OverlayKey itsKey;
	
	/**
	 * Größe der Datei in Byte.
	 */
	private long itsSize;
	
	private int itsPopularity;
	
	/**
	 * This value determines the number of bytes per block:
	 * 2 ^ itsBlockExponent = number of bytes per block.
	 * Default: 14
	 */
	private byte itsBlockExponent;
	
	
	/**
	 * This value determines the number of bytes per piece.
	 * 2 ^ itsPieceExponent = number of bytes per piece.
	 * Default: 19
	 */
	private byte itsPieceExponent;
	
	/**
	 * This is a list of all pieces of the document.
	 * The bits state, if their piece is complete.
	 * This will be <code>null</code> if the state of the document
	 * is <code>EMPTY</code> or <code>COMPLETE</code>.
	 */
	private BitSet itsPieces; //This will be set when the documents is in partial state. This saves much memory.
	
	/**
	 * The list of pieces that are downloaded partially.
	 * This will be <code>null</code> if the state of the document
	 * is <code>EMPTY</code> or <code>COMPLETE</code>.
	 */
	private Map<Integer, BitSet> itsPartialPieces;
	
	private Collection<BTDocumentFinishedListener> itsDocumentFinishedListener;
	
	private Collection<BTPieceFinishedListener> itsPieceFinishedListener;
	
	private Collection<BTBlockFinishedListener> itsBlockFinishedListener;
	
	private static final byte theirDefaultBlockExponent = BTConstants.DOCUMENT_DEFAULT_BLOCK_EXPONENT;
	
	private static final byte theirDefaultPieceExponent = BTConstants.DOCUMENT_DEFAULT_PIECE_EXPONENT;
	
	static final Logger log = SimLogger.getLogger(BTDocument.class);
	
	
	//Instantiation and initialization
	
	public BTDocument(OverlayKey theKey, long theSize) {
		if (theSize < 0)
			throw new RuntimeException("Size of a document must not be negative.");
		this.initialize(theKey, theSize, 0, theirDefaultPieceExponent, theirDefaultBlockExponent);
	}
	
	public BTDocument(OverlayKey theKey, long theSize, byte thePieceExponent, byte theBlockExponent) {
		if (theSize < 0)
			throw new RuntimeException("Size of a document must not be negative.");
		this.initialize(theKey, theSize, 0, thePieceExponent, theBlockExponent);
	}
	
	private void initialize(OverlayKey theKey, long theSize, int thePopularity, byte thePieceExponent, byte theBlockExponent) {
		this.itsState = State.EMPTY;
		this.itsKey = theKey;
		this.itsSize = theSize;
		this.itsPopularity = thePopularity;
		this.itsPieceExponent = thePieceExponent;
		this.itsBlockExponent = theBlockExponent;
		this.itsPieces = null;
		this.itsPartialPieces = null;
		this.itsDocumentFinishedListener = new LinkedList<BTDocumentFinishedListener>();
		this.itsPieceFinishedListener = new LinkedList<BTPieceFinishedListener>();
		this.itsBlockFinishedListener = new LinkedList<BTBlockFinishedListener>();
	}
	
	
	
	//Counting-Methods:
	
	public int getNumberOfPieces() { //The data of the last piece make the difference.
		int result = (int)this.itsSize / this.getNumberOfBytesPerPiece();
		if (0 != (this.itsSize % this.getNumberOfBytesPerPiece())) //If the division has a rest, we have to round up the result;
			result++;
		return result;
	}
	
	public int getNumberOfBytesPerPiece() {
		return (int)Math.round(Math.pow(2, this.itsPieceExponent));
	}
	
	public int getNumberOfBytesPerBlock() {
		return (int)Math.round(Math.pow(2, this.itsBlockExponent));
	}
	
	public int getNumberOfBlocksPerPiece() {
		return (int)Math.round(Math.pow(2, (this.itsPieceExponent - this.itsBlockExponent)));
	}
	
	public int getNumberOfBytesInLastPiece() {
		int rest = (int)(this.itsSize % this.getNumberOfBytesPerPiece());
		if (rest == 0)
			return this.getNumberOfBytesPerPiece();
		return ((rest < 0) ? (rest + this.getNumberOfBytesPerPiece()) : rest); // Java sometimes returns a negative number. I'm quite sure, not in this case. But: Its Java!
	}
	
	public int getNumberOfBlocksInLastPiece() {
		int result = this.getNumberOfBytesInLastPiece() / this.getNumberOfBytesPerBlock();
		if (0 != (this.getNumberOfBytesInLastPiece() % this.getNumberOfBytesPerBlock())) //If the division has a rest, we have to round up the result;
			result++;
		return result;
	}
	
	public int getNumberOfBytesInLastBlock() {
		int rest = (this.getNumberOfBytesInLastPiece() % this.getNumberOfBytesPerBlock());
		if (rest == 0)
			return this.getNumberOfBytesPerBlock();
		return ((rest < 0) ? (rest + this.getNumberOfBytesPerBlock()) : rest); // Java sometimes returns a negative number. I'm quite sure, not in this case. But: Its Java!
	}
	
	public int getNumberOfFinishedPieces() {
		if (this.getDocumentState() == +1)
			return this.getNumberOfPieces();
		if (this.getDocumentState() == -1)
			return 0;
		return this.itsPieces.cardinality();
	}
	
	public int getNumberOfPartialPieces() {
		if (this.getDocumentState() != 0)
			return 0;
		return this.itsPartialPieces.size();
	}
	
	/**
	 * Thic method counts partial pieces as unfinished pieces.
	 * @return the number of unfinished pieces.
	 */
	public int getNumberOfUnfinishedPieces() {
		return this.getNumberOfPieces() - this.getNumberOfFinishedPieces();
	}
	
	/**
	 * Finished blocks of partial pieces are not taken into account, as I can't verify them against a hash.
	 * @return the number of finished and verified bytes.
	 */
	public long getNumberOfFinishedBytes() {
		if (this.getDocumentState() == +1)
			return this.getSize();
		if (this.getDocumentState() == -1)
			return 0;
		long rawNumber = ((long)this.getNumberOfBytesPerPiece()) * (long)this.getNumberOfFinishedPieces();
		if (this.getPieceState(this.getNumberOfPieces() - 1) != 1) { //Take care of the last piece, which is most probably smaller.
			rawNumber -= this.getNumberOfBytesPerPiece();
			rawNumber += this.getNumberOfBytesInLastPiece();
		}
		return rawNumber;
	}
	
	public int getNumberOfBlocksInPiece(int thePieceNumber) {
		this.checkPieceBounds(thePieceNumber);
		if (isLastPiece(thePieceNumber))
			return this.getNumberOfBlocksInLastPiece();
		return this.getNumberOfBlocksPerPiece();
	}
	
	public int getNumberOfBytesInPiece(int thePieceNumber) {
		this.checkPieceBounds(thePieceNumber);
		if (isLastPiece(thePieceNumber))
			return this.getNumberOfBytesInLastPiece();
		return this.getNumberOfBytesPerPiece();
	}
	
	public int getNumberOfBytesInBlock(int thePieceNumber, int theBlockNumber) {
		this.checkBlockBounds(thePieceNumber, theBlockNumber);
		if (isLastBlock(thePieceNumber, theBlockNumber))
			return this.getNumberOfBytesInLastBlock();
		return this.getNumberOfBytesPerBlock();
	}
	
	
	
	//Finished/Unfinished pieces/blocks getter
	
	public BitSet getFinishedPieces() {
		if (this.getDocumentState() == +1) {
			return BTBitSetUtil.getFullBitset(this.getNumberOfPieces());
		}
		if (this.getDocumentState() == -1) {
			return BTBitSetUtil.getEmptyBitset(this.getNumberOfPieces());
		}
		return (BitSet)this.itsPieces.clone();
	}
	
	public BitSet getNotStartedPieces() {
		if (this.getDocumentState() == -1) {
			return BTBitSetUtil.getFullBitset(this.getNumberOfPieces());
		}
		if (this.getDocumentState() == +1) {
			return BTBitSetUtil.getEmptyBitset(this.getNumberOfPieces());
		}
		BitSet result = (BitSet)this.getFinishedPieces().clone();
		result.flip(0, this.getNumberOfPieces());
		for (int partialPieceIndex : this.itsPartialPieces.keySet()) {
			result.clear(partialPieceIndex);
		}
		return result;
	}
	
	public BitSet getPartialPiecesBitSet() {
		if (this.getDocumentState() != 0) {
			return BTBitSetUtil.getEmptyBitset(this.getNumberOfPieces());
		}
		BitSet result = new BitSet(this.getNumberOfPieces());
		for (int partialPieceIndex : this.itsPartialPieces.keySet()) {
			result.set(partialPieceIndex);
		}
		return result;
	}
	
	public BitSet getFinishedBlocks(int thePieceNumber) {
		this.checkPieceBounds(thePieceNumber);
		if (this.getPieceState(thePieceNumber) == +1) {
			return BTBitSetUtil.getFullBitset(this.getNumberOfBlocksInPiece(thePieceNumber));
		}
		if (this.getPieceState(thePieceNumber) == -1) {
			return BTBitSetUtil.getEmptyBitset(this.getNumberOfBlocksInPiece(thePieceNumber));
		}
		return (BitSet)this.itsPartialPieces.get(thePieceNumber).clone();
	}
	
	public BitSet getUnfinishedBlocks(int thePieceNumber) {
		this.checkPieceBounds(thePieceNumber);
		BitSet result = this.getFinishedBlocks(thePieceNumber);
		result.flip(0, this.getNumberOfBlocksInPiece(thePieceNumber));
		return result;
	}
	/*
	public int[] getFinishedBlocksIndices(int thePieceNumber) {
		this.checkPieceBounds(thePieceNumber);
//		int[] theResultArray = new int[this.itsPieces.cardinality()];
//		int position = 0;
//		for(int counter = 0; counter < this.itsPieces.cardinality(); counter++) { //TODO: Is there a smarter way to do this?
//			position = this.itsPieces.nextSetBit(position);
//			theResultArray[counter] = position;
//		}
//		return theResultArray;
	}
	*/
	/*
	public int[] getUnfinishedBlocksIndices(int thePieceNumber) {
		this.checkPieceBounds(thePieceNumber);
		return null;
	}
	*/
	
	public Map<Integer, BitSet> getPartialPiecesMap() {
		if (this.itsPartialPieces == null)
			return new HashMap<Integer, BitSet>(0);
		Map<Integer, BitSet> copiedPartialPieces = new HashMap<Integer, BitSet>(this.itsPartialPieces.size(), 1f);
		for(Integer key : this.itsPartialPieces.keySet()) {
			copiedPartialPieces.put(key, (BitSet)this.itsPartialPieces.get(key).clone());
		}
		return copiedPartialPieces;
	}
	
	/**
	 * This method returns the original Map. This is very unsecure. Use it only when the other method is too slow.
	 * @return the oridinal Map of partial pieces. Don't change it!
	 */
	/*
	@Deprecated
	public Map<Integer, BitSet> getPartialPiecesMapFast() {
		return this.itsPartialPieces;
	}
	*/
	
	
	//State getter
	
	/**
	 * Returns the state of the document:
	 * -1: Document is empty.
	 *  0: Document partially downloaded.
	 * +1: Document completelly downloaded.
	 * @return the state, one of the values: -1; 0; +1;
	 */
	public int getDocumentState() {
		if (this.itsState == State.EMPTY)
			return -1;
		if (this.itsState == State.COMPLETE)
			return +1;
		return 0;
	}
	
	/**
	 * Returns the state of the piece:
	 * -1: Piece is empty.
	 *  0: Piece partially downloaded.
	 * +1: Piece completelly downloaded.
	 * @param thePieceNumber the number of the piece in the document.
	 * @return the state, one of the values: -1; 0; +1;
	 */
	public int getPieceState(int thePieceNumber) {
		this.checkPieceBounds(thePieceNumber);
		int theDocumentState = this.getDocumentState();
		if (theDocumentState != 0)
			return theDocumentState;
		if (this.itsPieces.get(thePieceNumber))
			return +1;
		if (this.itsPartialPieces.containsKey(thePieceNumber))
			return 0;
		return -1;
	}
	
	/**
	 * Returns the state of the block:
	 * -1: Block is not downloaded.
	 * +1: Block is downloaded.
	 * @param thePieceNumber the number of the piece in the document.
	 * @param theBlockNumber the number of the block in the piece.
	 * @return the state, one of the values: -1; +1;
	 */
	public int getBlockState(int thePieceNumber, int theBlockNumber) {
		this.checkBlockBounds(thePieceNumber, theBlockNumber);
		int thePieceState = this.getPieceState(thePieceNumber);
		if (thePieceState != 0)
			return thePieceState;
		if (this.itsPartialPieces.get(thePieceNumber).get(theBlockNumber))
			return +1;
		return -1;
	}
	
	public void registerDocumentFinished(BTDocumentFinishedListener theListener) {
		this.itsDocumentFinishedListener.add(theListener);
	}
	
	public void registerPieceFinished(BTPieceFinishedListener theListener) {
		this.itsPieceFinishedListener.add(theListener);
	}
	
	public void registerBlockFinished(BTBlockFinishedListener theListener) {
		this.itsBlockFinishedListener.add(theListener);
	}
	
	public void unregisterDocumentFinished(BTDocumentFinishedListener theListener) {
		this.itsDocumentFinishedListener.remove(theListener);
	}
	
	public void unregisterPieceFinished(BTPieceFinishedListener theListener) {
		this.itsPieceFinishedListener.remove(theListener);
	}
	
	public void unregisterBlockFinished(BTBlockFinishedListener theListener) {
		this.itsBlockFinishedListener.remove(theListener);
	}
	
	
	
	//The only method which should be used for changing the state.
	/**
	 * Use this method, if an block gets downloaded. It will handle everything else for the document class.
	 * @param thePieceNumber The number of the piece, in which the downloaded block is located.
	 * @param theBlockNumber The number of the finished block.
	 */
	public void addBlock(int thePieceNumber, int theBlockNumber) {
		this.checkBlockBounds(thePieceNumber, theBlockNumber);
		if (this.getBlockState(thePieceNumber, theBlockNumber) == +1)
			return;
		this.checkStartPiece(thePieceNumber);
		this.itsPartialPieces.get(thePieceNumber).set(theBlockNumber);
		this.checkFinishPiece(thePieceNumber);
		for (BTBlockFinishedListener aListener : this.itsBlockFinishedListener) {
			aListener.blockFinished(thePieceNumber, theBlockNumber);
		}
	}
	
	
	
	//Internal methods for changing the state.
	
	private void checkStartPiece(int thePieceNumber) {
		if (this.getPieceState(thePieceNumber) == -1)
			this.startPiece(thePieceNumber);
	}
	
	private void checkFinishPiece(int thePieceNumber) {
		/*
		 * We cannot use getPieceState, as it checks only normal states.
		 * But this state is abnormal if the piece got really just finished:
		 * Its entry in the "itsPieces"-BitSet is not set, but the "partialPieces"-BitSet is full.
		 */
		if (BTBitSetUtil.isBitsetSet(this.itsPartialPieces.get(thePieceNumber), this.getNumberOfBlocksInPiece(thePieceNumber)))
			this.finishPiece(thePieceNumber);
	}
	
	private void startPiece(int thePieceNumber) {
		this.checkStartDocument();
		int newPieceSize = this.getNumberOfBlocksInPiece(thePieceNumber);
		this.itsPartialPieces.put(thePieceNumber, new BitSet(newPieceSize));
	}
	
	private void finishPiece(int thePieceNumber) {
		this.itsPartialPieces.remove(thePieceNumber);
		this.itsPieces.set(thePieceNumber);
		this.checkFinishDocument();
		for (BTPieceFinishedListener aListener : this.itsPieceFinishedListener) {
			aListener.pieceFinished(thePieceNumber);
		}
	}
	
	private void checkStartDocument() {
		if (this.getDocumentState() == -1)
			this.startDocument();
	}
	
	private void checkFinishDocument() {
		//We cannot use the normal "getDocumentState"-method, as it just checks "normal" states.
		if (BTBitSetUtil.isBitsetSet(this.itsPieces, this.getNumberOfPieces()))
			this.finishDocument();
	}
	
	private void startDocument() {
		this.itsState = State.PARTIAL;
		this.itsPieces = new BitSet(this.getNumberOfPieces());
		this.itsPartialPieces = new HashMap<Integer, BitSet>();
	}
	
	private void finishDocument() {
		this.itsPieces = null;
		this.itsPartialPieces = null;
		this.itsState = State.COMPLETE;
		for (BTDocumentFinishedListener aListener : this.itsDocumentFinishedListener) {
			aListener.documentFinished();
		}
	}
	
	
	
	//Different intern helper methods:
	
	private void checkPieceBounds(int thePieceNumber) {
		if ((thePieceNumber >= this.getNumberOfPieces()) || (thePieceNumber < 0))
			throw new IndexOutOfBoundsException("Lower bound: '0'; Upper Bound: '" + (this.getNumberOfPieces() - 1) + "'; Index: '" + thePieceNumber + "'!");
	}
	
	private void checkBlockBounds(int thePieceNumber, int theBlockNumber) {
		checkPieceBounds(thePieceNumber);
		if ((theBlockNumber >= this.getNumberOfBlocksInPiece(thePieceNumber)) || (theBlockNumber < 0))
			throw new IndexOutOfBoundsException("Lower bound: '0'; Upper Bound: '" + (this.getNumberOfBlocksInPiece(thePieceNumber) - 1) + "'; Index: '" + theBlockNumber + "'!");
	}
	
	private boolean isLastPiece(int thePieceNumber) {
		return thePieceNumber == (this.getNumberOfPieces() - 1);
	}
	
	private boolean isLastBlock(int thePieceNumber, int theBlockNumber) {
		if (! isLastPiece(thePieceNumber))
			return false;
		return theBlockNumber == (this.getNumberOfBlocksInPiece(thePieceNumber) - 1);
	}
	
	
	
	//Inhertited methods:
	
	public OverlayKey getKey() {
		return this.itsKey;
	}
	
	public int getPopularity() {
		return this.itsPopularity;
	}
	
	public long getSize() {
		return this.itsSize;
	}
	
	public State getState() {
		return this.itsState;
	}
	
	public void setKey(OverlayKey newKey) {
		this.itsKey = newKey;
	}
	
	public void setPopularity(int newPopularity) {
		if (newPopularity < 0)
			throw new RuntimeException("Popularity of a document must not be negative.");
		this.itsPopularity = newPopularity;
	}
	
	public void setSize(long newSize) {
		if (newSize < 0)
			throw new RuntimeException("Size of a document must not be negative.");
		this.itsSize = newSize;
	}
	
	public void setState(State newState) {
		if (this.itsState == newState)
			return;
		if (newState == State.PARTIAL) {
			if (this.getSize() <= this.getNumberOfBytesPerBlock())
				throw new RuntimeException("File to small to have the state 'partial'! It only has a single block. This can either be finished or empty, but not partially finished.");
			this.itsState = State.EMPTY;
			this.addBlock(0, 0); //this is the most secure way, to make it partial. Just setting "itsState" would cause a inconsistent state.
			return;
		}
		if (this.itsState == State.PARTIAL) {
			this.itsPieces = null;
			this.itsPartialPieces = null;
		}
		this.itsState = newState;
	}
	
	@Override
	/**
	 * Equality is checked on the documents contents, not the download state!
	 */
	public boolean equals(Object theOther) {
		if (! (theOther instanceof Document))
			return false;
		Document theOtherDocument = (Document) theOther;
		if (this.getSize() != theOtherDocument.getSize())
			return false;
		if (this.getKey() != theOtherDocument.getKey())
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "[BTDocument| Size: " + this.getSize() + "; Key: " + this.getKey() + "; State: " + this.getState() + "]";
	}
	
	public String inspect() {
		return "{State: '" + this.itsState + "'; #Pieces: '" + this.getNumberOfPieces() + "'; #Bytes: " + this.getSize() + "; #Blocks: ???; #Bytes per Block: " + this.getNumberOfBytesPerBlock() + "; #Blocks per Piece: " + this.getNumberOfBlocksPerPiece() + "; #Bytes per Piece: " + this.getNumberOfBytesPerPiece() + "; #Bytes in Last Piece: " + this.getNumberOfBytesInLastPiece() + "; #Bytes in Last Block: " + this.getNumberOfBytesInLastBlock() + "; #Blocks in Last Piece: " + this.getNumberOfBlocksInLastPiece() + "; Bitset: '" + this.itsPieces + "'; PartialPieces: '" + this.itsPartialPieces + "'}";
	}
}
