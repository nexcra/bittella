package de.tud.kom.p2psim.overlay.bt.algorithm;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.math.random.RandomGenerator;
import org.apache.log4j.Logger;

import de.tud.kom.p2psim.impl.util.logging.SimLogger;
import de.tud.kom.p2psim.overlay.bt.BTBitSetUtil;
import de.tud.kom.p2psim.overlay.bt.BTConstants;
import de.tud.kom.p2psim.overlay.bt.BTContact;
import de.tud.kom.p2psim.overlay.bt.BTDocument;
import de.tud.kom.p2psim.overlay.bt.BTInternRequest;

/**
 * This class implements the BitTorrent piece(download) selection strategy.
 * @author Jan Stolzenburg
 */
public class BTAlgorithmDownloadSelectionThreePhased implements BTAlgorithmDownloadSelection {
	
	private BTDocument itsDocument;
	
	private Map<BTContact, Map<Integer, BitSet>> itsRequestsPerContact;
	
	/**
	 * Saves which pieces I am currently downloading.
	 * The Bitsets are the block in that pieces.
	 * If a bit is set, I either already have downloaded it,
	 * or it is currently been requested.
	 */
	private Map<Integer, BitSet> itsPartialPieces;
	
	private RandomGenerator itsRandomGenerator;
	
	private boolean itsIsSetup = false;
	
	private enum State {STARTING, MIDDLE, ENDGAME}
	
	private State itsState;
	
	/**
	 * The list of pieces that are rare in my neighbourhood.
	 */
	private Collection<Integer> itsRarePieces;
	
	/**
	 * Has the rare piece set been calculated?
	 */
	private boolean itsRarePiecesCalculated;
	
	private static int theirQueueSize = BTConstants.DOWNLOAD_QUEUE_REQUEST_SIZE; //This value may be crucial for performance.
	
	static final Logger log = SimLogger.getLogger(BTAlgorithmDownloadSelectionThreePhased.class);
	
	public void setup(BTDocument theDocument, RandomGenerator theRandomGenerator) {
		this.itsDocument = theDocument;
		this.itsRandomGenerator = theRandomGenerator;
		this.itsPartialPieces = new HashMap<Integer, BitSet>();
		this.itsRequestsPerContact = new HashMap<BTContact, Map<Integer, BitSet>>();
		this.itsRarePieces = new LinkedList<Integer>();
		this.itsRarePiecesCalculated = false;
		this.itsIsSetup = true;
	}
	
	public Collection<BTInternRequest> computeRequests(Map<BTContact, BitSet> theOtherPeersPieces, Collection<BTInternRequest> thePendingRequests) {
		if (! this.isSetup()) {
			log.error("You have to setup this algorithm first!");
			throw new RuntimeException("You have to setup this algorithm first!");
		}
		this.itsRequestsPerContact.clear();
		this.itsPartialPieces.clear();
		
		//Add partial pieces from the document.
		this.itsPartialPieces.putAll(this.itsDocument.getPartialPiecesMap());
			
		//Add currently pending requests
		for (BTInternRequest anRequest : thePendingRequests) {
			if (! this.itsRequestsPerContact.containsKey(anRequest.getRequestedPeer()))
				this.itsRequestsPerContact.put(anRequest.getRequestedPeer(), new HashMap<Integer, BitSet>());
			if (! this.itsRequestsPerContact.get(anRequest.getRequestedPeer()).containsKey(anRequest.getPieceNumber()))
				this.itsRequestsPerContact.get(anRequest.getRequestedPeer()).put(anRequest.getPieceNumber(), new BitSet(this.itsDocument.getNumberOfBlocksInPiece(anRequest.getPieceNumber())));
			this.itsRequestsPerContact.get(anRequest.getRequestedPeer()).get(anRequest.getPieceNumber()).set(anRequest.getBlockNumber());
			
			if (! this.itsPartialPieces.containsKey(anRequest.getPieceNumber()))
				this.itsPartialPieces.put(anRequest.getPieceNumber(), new BitSet(this.itsDocument.getNumberOfBlocksInPiece(anRequest.getPieceNumber())));
			this.itsPartialPieces.get(anRequest.getPieceNumber()).set(anRequest.getBlockNumber());
		}
		
		
		this.computeState();
		
		if (! this.itsRarePiecesCalculated) {
			this.calcRarestPieces(theOtherPeersPieces.values());
			this.itsRarePiecesCalculated = true;
		}
		//End of management stuff.
		
		LinkedList<BTInternRequest> requests = new LinkedList<BTInternRequest>();
		HashMap<BTContact, BitSet> peersWithNotFilledQueue = new HashMap<BTContact, BitSet>(theOtherPeersPieces);
		int previousSize = -1; //It has to be smaller zero, or it will never even reach the first round!
		while ((previousSize < requests.size()) && ! this.allBufferFilled(peersWithNotFilledQueue)) {
			previousSize = requests.size(); //Emergency break, if no more changes due to a bug.
			switch (this.itsState) {
				case STARTING : {
					requests.addAll(this.computeRandomFirst(peersWithNotFilledQueue));
					break;
				}
				case MIDDLE : {
					requests.addAll(this.computeRarestFirst(peersWithNotFilledQueue));
					break;
				}
				case ENDGAME: {
					requests.addAll(this.computeEndgame(peersWithNotFilledQueue));
					break;
				}
				//No default. If it is NULL, something is very wrong.
			}
		}
		return requests;
	}
	
	/**
	 * For testing and debugging.
	 * @param theRequests the requests that should be checked.
	 * @param theOtherPeersPieces The bitsets representing the pieces that the other peers have.
	 */
	@SuppressWarnings("unused")
	private void checkRequests(Collection<BTInternRequest> theRequests, Map<BTContact, BitSet> theOtherPeersPieces) {
		for (BTInternRequest anRequest : theRequests) {
			this.checkRequest(anRequest, theOtherPeersPieces.get(anRequest.getRequestedPeer()));
		}
	}
	
	/**
	 * For testing and debugging.
	 * @param theRequest the request that should be checked.
	 * @param theOtherPeersPieces The bitset representing the pieces that the other peer has.
	 */
	private void checkRequest(BTInternRequest theRequest, BitSet theOtherPeersPieces) {
		if (! theOtherPeersPieces.get(theRequest.getPieceNumber()))
			throw new RuntimeException("Tried to download not available piece '" + theRequest.getPieceNumber() + "'.");
	}
	
	/**
	 * For testing and debugging.
	 * @param thePieceNumber
	 * @param theBlockNumber
	 */
	@SuppressWarnings("unused")
	private void checkMultipleRequest(int thePieceNumber, int theBlockNumber) {
		if (this.itsState == State.ENDGAME)
			return;
		if (this.itsPartialPieces.get(thePieceNumber).get(theBlockNumber))
			throw new RuntimeException("Tried to download piece again!");
		for (BTContact anOtherPeer : this.itsRequestsPerContact.keySet()) {
			if (! this.itsRequestsPerContact.get(anOtherPeer).containsKey(thePieceNumber))
				continue;
			if (this.itsRequestsPerContact.get(anOtherPeer).get(thePieceNumber).get(theBlockNumber))
				throw new RuntimeException("Tried to download piece again!");
		}
	}
	
	/**
	 * Compute the requests for the endgame mode.
	 * @param theOtherPeersPieces The bitsets representing the pieces that the other peers have.
	 * @return the new requests
	 */
	private Collection<BTInternRequest> computeEndgame(Map<BTContact, BitSet> theOtherPeersPieces) {
		boolean success;
		LinkedList<BTInternRequest> result = new LinkedList<BTInternRequest>();
		
		//For every peer...
		for (BTContact anOtherPeer : theOtherPeersPieces.keySet()) {
			success = false;
			if (! this.itsRequestsPerContact.containsKey(anOtherPeer))
				this.itsRequestsPerContact.put(anOtherPeer, new HashMap<Integer, BitSet>());
			
			//Retry the following until we have success: a new and valid request.
			do {
				//With every piece that we started to download...
				for (Integer pieceNumber : this.itsPartialPieces.keySet()) {
					if (! theOtherPeersPieces.get(anOtherPeer).get(pieceNumber))
						continue;
					if (this.itsDocument.getPieceState(pieceNumber) == 1)
						continue; //If this piece is already finished, try the next one.
					if (this.itsRequestsPerContact.get(anOtherPeer).containsKey(pieceNumber) && BTBitSetUtil.isBitsetSet(BTBitSetUtil.or(this.itsDocument.getFinishedBlocks(pieceNumber), this.itsRequestsPerContact.get(anOtherPeer).get(pieceNumber)), this.itsDocument.getNumberOfBlocksInPiece(pieceNumber)))
						continue; //If all blocks are either requested or downloaded, try the next piece.
					if (! this.itsRequestsPerContact.get(anOtherPeer).containsKey(pieceNumber))
						this.itsRequestsPerContact.get(anOtherPeer).put(pieceNumber, new BitSet(this.itsDocument.getNumberOfBlocksInPiece(pieceNumber)));
					int blockNumber = BTBitSetUtil.or(this.itsDocument.getFinishedBlocks(pieceNumber), this.itsRequestsPerContact.get(anOtherPeer).get(pieceNumber)).nextClearBit(0); //Take the first valid block. (neither downloaded nor requested)
					if ((blockNumber >= this.itsDocument.getNumberOfBlocksInPiece(pieceNumber)) || (blockNumber < 0)) {
						log.error("Internal Error: Invalid block number generated: '" + blockNumber + "', but it only has '" + this.itsDocument.getNumberOfBlocksInPiece(pieceNumber) + "' blocks.");
						continue;
					}
					
					//We had success. This is our new request.
//					this.checkMultipleRequest(pieceNumber, blockNumber);
					this.itsRequestsPerContact.get(anOtherPeer).get(pieceNumber).set(blockNumber);
					this.itsPartialPieces.get(pieceNumber).set(blockNumber);
					BTInternRequest newRequest = new BTInternRequest(null, anOtherPeer, this.itsDocument.getKey(), pieceNumber, blockNumber);
					result.add(newRequest);
//					this.checkRequest(newRequest, theOtherPeersPieces.get(newRequest.getRequestedPeer()));
					success = true;
					break;
				}
				
				// Let's add another piece to it's list and try again.
				//TODO: Wenn der das macht, geht der oben wieder alle pieces durch, nicht nur das neue.
				if (! success) {
					int pieceNumber;
					for (pieceNumber = this.itsDocument.getFinishedPieces().nextClearBit(0); pieceNumber < this.itsDocument.getNumberOfPieces(); pieceNumber = this.itsDocument.getFinishedPieces().nextClearBit(pieceNumber + 1)) {
						if (! theOtherPeersPieces.get(anOtherPeer).get(pieceNumber))
							continue;
						if (! this.itsPartialPieces.containsKey(pieceNumber)) {
							this.itsPartialPieces.put(pieceNumber, new BitSet(this.itsDocument.getNumberOfBlocksInPiece(pieceNumber)));
							break;
						}
					}
					if (pieceNumber >= this.itsDocument.getNumberOfPieces())
						break; //There are no more pieces left. Let's give it up.
				}
			} while (! success);
		}
		return result;
	}
	
	/**
	 * Compute the requests for the 'rarest first' mode.
	 * @param theOtherPeersPieces The bitsets representing the pieces that the other peers have.
	 * @return the new requests
	 */
	private Collection<BTInternRequest> computeRarestFirst(Map<BTContact, BitSet> theOtherPeersPieces) {
		LinkedList<BTInternRequest> result = new LinkedList<BTInternRequest>();
		
		//For every peer...
		for (BTContact anOtherPeer : theOtherPeersPieces.keySet()) {
			
			//Calculate the piece number for our new request.
			int pieceNumber = this.calcNextPieceRarestFirst(theOtherPeersPieces.get(anOtherPeer), theOtherPeersPieces);
			if (pieceNumber == -1)
				continue; //No valid piece for that peer found.
			
			if (! this.itsRequestsPerContact.containsKey(anOtherPeer))
				this.itsRequestsPerContact.put(anOtherPeer, new HashMap<Integer, BitSet>());
			if (! this.itsRequestsPerContact.get(anOtherPeer).containsKey(pieceNumber))
				this.itsRequestsPerContact.get(anOtherPeer).put(pieceNumber, new BitSet(this.itsDocument.getNumberOfBlocksInPiece(pieceNumber)));
			
			if (! this.itsPartialPieces.containsKey(pieceNumber))
				this.itsPartialPieces.put(pieceNumber, new BitSet(this.itsDocument.getNumberOfBlocksInPiece(pieceNumber)));
			
			//Calculate the block number for our new request.
			int blockNumber = this.calcNextBlock(pieceNumber);
			if (blockNumber < 0)
				continue; //No more blocks left in this piece.
			
			//We had success. This is our new request.
//			this.checkMultipleRequest(pieceNumber, blockNumber);
			this.itsRequestsPerContact.get(anOtherPeer).get(pieceNumber).set(blockNumber);
			this.itsPartialPieces.get(pieceNumber).set(blockNumber);
			BTInternRequest newRequest = new BTInternRequest(null, anOtherPeer, this.itsDocument.getKey(), pieceNumber, blockNumber);
			result.add(newRequest);
//			this.checkRequest(newRequest, theOtherPeersPieces.get(newRequest.getRequestedPeer()));
		}
		return result;
	}
	
	/**
	 * Compute the requests for the 'random first' mode.
	 * @param theOtherPeersPieces The bitsets representing the pieces that the other peers have.
	 * @return the new requests
	 */
	private Collection<BTInternRequest> computeRandomFirst(Map<BTContact, BitSet> theOtherPeersPieces) {
		LinkedList<BTInternRequest> result = new LinkedList<BTInternRequest>();
		
		//For every peer...
		for (BTContact anOtherPeer : theOtherPeersPieces.keySet()) {
			
			//Calculate the piece number for our new request.
			int pieceNumber = this.calcNextPieceRandom(theOtherPeersPieces.get(anOtherPeer));
			if (pieceNumber == -1)
				continue; //No valid piece for that peer found.
			
			if (! this.itsRequestsPerContact.containsKey(anOtherPeer))
				this.itsRequestsPerContact.put(anOtherPeer, new HashMap<Integer, BitSet>());
			if (! this.itsRequestsPerContact.get(anOtherPeer).containsKey(pieceNumber))
				this.itsRequestsPerContact.get(anOtherPeer).put(pieceNumber, new BitSet(this.itsDocument.getNumberOfBlocksInPiece(pieceNumber)));
			
			if (! this.itsPartialPieces.containsKey(pieceNumber))
				this.itsPartialPieces.put(pieceNumber, new BitSet(this.itsDocument.getNumberOfBlocksInPiece(pieceNumber)));
			
			//Calculate the block number for our new request.
			int blockNumber = this.calcNextBlock(pieceNumber);
			if (blockNumber < 0)
				continue; //No more blocks left in this piece.
			
			//We had success. This is our new request.
//			this.checkMultipleRequest(pieceNumber, blockNumber);
			this.itsRequestsPerContact.get(anOtherPeer).get(pieceNumber).set(blockNumber);
			this.itsPartialPieces.get(pieceNumber).set(blockNumber);
			BTInternRequest newRequest = new BTInternRequest(null, anOtherPeer, this.itsDocument.getKey(), pieceNumber, blockNumber);
			result.add(newRequest);
//			this.checkRequest(newRequest, theOtherPeersPieces.get(newRequest.getRequestedPeer()));
		}
		return result;
	}
	
	/**
	 * Computes the next block that can be downloaded. Which means, it is neither already downloaded nor already requested.
	 * @param thePieceNumber the number of the piece, in which we have to find the new block.
	 * @return the number of the new block
	 */
	private int calcNextBlock(int thePieceNumber) {
		int blockNumber = -1;
		int retries = 0;
		
		//Retry until we find a valid block...
		do {
			retries += 1;
			if (retries > 5) { // If, for example, there is only one block left out of 128, it could take too long. This will shorten it, after some retries.
				//Take just the first that we find:
				blockNumber = BTBitSetUtil.or(this.itsDocument.getFinishedBlocks(thePieceNumber), this.itsPartialPieces.get(thePieceNumber)).nextClearBit(0);
				if (blockNumber >= this.itsDocument.getNumberOfBlocksInPiece(thePieceNumber)) {
					return -1; //There is no block left in this piece.
				}
				return blockNumber;
			}
			else { //Try out a random block.
				blockNumber = this.itsRandomGenerator.nextInt(this.itsDocument.getNumberOfBlocksInPiece(thePieceNumber));
			}
		} while ((this.itsDocument.getBlockState(thePieceNumber, blockNumber) == 1) || this.itsPartialPieces.get(thePieceNumber).get(blockNumber));
		//As long as it is already downloaded or requested by anyone, retry it.
		
		return blockNumber;
	}
	
	/**
	 * Computes the next rare piece that can be downloaded. Which means, it has at least one block that is neither already downloaded nor already requested.
	 * @param theFinishedPieces the pieces that the other peer has finished.
	 * @param theOtherPeersPieces The bitsets representing the pieces that the other peers have.
	 * @return the number of the chosen piece
	 */
	private int calcNextPieceRarestFirst(BitSet theFinishedPieces, Map<BTContact, BitSet> theOtherPeersPieces) {
		//First, we try to take a piece that we already used in earlier requests.
		int pieceNumber = this.calcNextRequestablePartialPiece(theFinishedPieces);
		if (pieceNumber != -1) {
			return pieceNumber;
		}
		//We haven't found a piece in our earlier requests to that peer. Now we have to find a complete new piece.
		for (Integer rarePieceNumber : this.itsRarePieces) {
			if (this.checkPieceForRequest(rarePieceNumber, theFinishedPieces)) {
				this.itsRarePieces.remove(rarePieceNumber);
				return rarePieceNumber;
			}
		}
		//Could it be worthwhile to recalculate the list of rare pieces?
		if (this.itsRarePieces.isEmpty()) {
			this.calcRarestPieces(theOtherPeersPieces.values());
			
			//Can we use one of the new rare pieces?
			for (Integer rarePieceNumber : this.itsRarePieces) {
				if (this.checkPieceForRequest(rarePieceNumber, theFinishedPieces)) {
					this.itsRarePieces.remove(rarePieceNumber);
					return rarePieceNumber;
				}
			}
		}
		
		//take any piece:
		return this.calcNextPieceRandom(theFinishedPieces);
	}
	
	/**
	 * Recalculate the list of rare pieces.
	 * @param theOtherPeersPieces
	 */
	private void calcRarestPieces(Collection<BitSet> theOtherPeersPieces) {
		this.itsRarePieces.clear();
		int leastSeenCounter = Integer.MAX_VALUE;
		int currentValue;
		
		//We go through the pieces and count each. We remember how often we have seen the rarest piece.
		//We keep a list of all pieces that are that often.
		//If we find a more rare piece, we  clear the list, add the new element and go on.
		//For every piece...
		for (int pieceNumber = 0; pieceNumber < this.itsDocument.getNumberOfPieces(); pieceNumber++) {
			currentValue = 0;
			
			//For every peer...
			for (BitSet aBitSet : theOtherPeersPieces) {
				if (aBitSet.get(pieceNumber))
					currentValue += 1; //If the peer has the piece, increment the count.
			}
			
			if (currentValue < leastSeenCounter) { //We found a more rare piece...
				leastSeenCounter = currentValue;
				this.itsRarePieces.clear();
				this.itsRarePieces.add(pieceNumber);
			} else if (currentValue == leastSeenCounter) //This piece is as rare as the rarest piece. We add it to the list of the rare pieces.
				this.itsRarePieces.add(pieceNumber);
		}
	}
	
	/**
	 * Computes the next random piece that can be downloaded. Which means, it has at least one block that is neither already downloaded nor already requested.
	 * @param theFinishedPieces the pieces that the other peer has finished.
	 * @return the number of the chosen piece
	 */
	private int calcNextPieceRandom(BitSet theFinishedPieces) {
		
		//First, we try to take a piece that we already used in earlier requests.
		int pieceNumber = this.calcNextRequestablePartialPiece(theFinishedPieces);
		
		//We haven't found a piece in our earlier requests to that peer. Now we have to find a complete new piece.
		if (pieceNumber != -1)
			return pieceNumber;
		int retryCounter = 0;
		
		//Try to find a valid random piece number:
		do {
			retryCounter += 1;
			if (retryCounter > 10) {
				pieceNumber = -1;
				break;
			}
			pieceNumber = this.itsRandomGenerator.nextInt(this.itsDocument.getNumberOfPieces());
		} while (! this.checkPieceForRequest(pieceNumber, theFinishedPieces));
		
		//It has failed to often to find a random piece, now we test every single piece one after another.
		if (pieceNumber == -1) {
			for (pieceNumber = 0; pieceNumber < this.itsDocument.getNumberOfPieces(); pieceNumber++) {
				if (this.checkPieceForRequest(pieceNumber, theFinishedPieces))
					break;
			}
		}
		//Nothing found?
		if ((pieceNumber >= this.itsDocument.getNumberOfPieces()) || (pieceNumber < 0))
			return -1;
		return pieceNumber;
	}
	
	/**
	 * Checks if there is an partially downloaded or requested piece, which should be continued to be requested or downloaded.
	 * @param theFinishedPieces The finished pieces of the peer.
	 * @return the number of the piece. If we are not successful, we return -1.
	 */
	private int calcNextRequestablePartialPiece(BitSet theFinishedPieces) {
		if (this.itsPartialPieces.isEmpty()) {
			return -1;
		}
		int pieceNumber;
		Iterator<Integer> iter = this.itsPartialPieces.keySet().iterator();
		do {
			pieceNumber = iter.next();
			if (this.checkPieceForRequest(pieceNumber, theFinishedPieces)) {
				return pieceNumber; //We take the first piece we find.
			}
		} while (iter.hasNext());
		return -1; //We found none.
	}
	
	/**
	 * Checks, if we can request this piece from the other peer.
	 * @param thePieceNumber the number of the piece that we should check.
	 * @param theFinishedPieces The bitsets representing the offered pieces from the peer.
	 * @return Can we request this piece?
	 */
	private boolean checkPieceForRequest(int thePieceNumber, BitSet theFinishedPieces) {
		//Check if this piece is already downloaded completely.
		if (this.itsDocument.getPieceState(thePieceNumber) == 1)
			return false;
		
		//Check, if the other peer has this piece.
		if (! theFinishedPieces.get(thePieceNumber))
			return false;
		
		//Check, if this piece is beeing requested from another peer.
		if (this.itsPartialPieces.containsKey(thePieceNumber) && BTBitSetUtil.isBitsetSet(this.itsPartialPieces.get(thePieceNumber), this.itsDocument.getNumberOfBlocksInPiece(thePieceNumber)))
			return false;
		return true;
	}
	
	/**
	 * Compute the state of the download(started, middle, endgame).
	 */
	private void computeState() {
		if (this.itsDocument.getNumberOfFinishedPieces() < 4) {
			this.itsState = State.STARTING;
			return;
		}
		BitSet notStartedPieces = this.itsDocument.getNotStartedPieces();
		
		//Check which pieces has not been requested.
		for (int pieceNumber : this.itsPartialPieces.keySet())
			notStartedPieces.clear(pieceNumber);
		
		if (BTBitSetUtil.isBitsetEmpty(notStartedPieces)) {
			this.itsState = State.ENDGAME;
			return;
		}
		this.itsState = State.MIDDLE;
	}
	
	/**
	 * Checks the queues of all peers.
	 * Removes every peer from the list, if it's queue is full of (pending) requests.
	 * Leaves the peers that still need more requests.
	 * @param theOtherPeersPieces The list of all the other peers. Peers with full queues get removed from it.
	 * @return Are the queues of all peers full of (pending) requests?
	 */
	private boolean allBufferFilled(Map<BTContact, BitSet> theOtherPeersPieces) {
		
		//For every peer...
		for (BTContact anOtherPeer : new LinkedList<BTContact>(theOtherPeersPieces.keySet())) {
			int numberOfPendingRequests = 0;
			
			//If we have requests to this peer
			if (this.itsRequestsPerContact.containsKey(anOtherPeer)) {
				
				//Count the requests
				for (BitSet pendingRequests : this.itsRequestsPerContact.get(anOtherPeer).values())
					numberOfPendingRequests += pendingRequests.cardinality();
			}
			
			//Are this enough requests?
			if (numberOfPendingRequests >= theirQueueSize)
				theOtherPeersPieces.remove(anOtherPeer); //Filter out all peers, with enough pending requests.
		}
		return theOtherPeersPieces.isEmpty();
	}
	
	public boolean isSetup() {
		return this.itsIsSetup;
	}
	
	/**
	 * @return Do we have to send cancel messages, because we send a request to multiple peers?
	 */
	public boolean sendCancel() {
		return this.itsState == State.ENDGAME;
	}
	
}
