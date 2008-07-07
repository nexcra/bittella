package uc3m.netcom.overlay.bt.algorithm;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.tud.kom.p2psim.impl.util.logging.SimLogger;
import uc3m.netcom.overlay.bt.BTBitSetUtil;
import uc3m.netcom.overlay.bt.BTContact;
import uc3m.netcom.overlay.bt.BTDocument;

/**
 * This class calculates the interest of a peer in the other peers data.
 * If they have data that we don't have, we are interested.
 * @author Jan Stolzenburg
 *
 */
public class BTAlgorithmInterested implements BTAlgorithm {
	
	private BTDocument itsDocument;
	
	private boolean itsIsSetup = false;
	
	static final Logger log = SimLogger.getLogger(BTAlgorithmInterested.class);
	
	public void setup(BTDocument theDocument) {
		this.itsDocument = theDocument;
		this.itsIsSetup = true;
	}
	
	public boolean isSetup() {
		return this.itsIsSetup;
	}
	
	/**
	 * @param theBitset the bitset of the other peer, showing its finished pieces.
	 * @return Am I interested in this peer?
	 */
	public boolean computeInterest(BitSet theBitset) {
		if (! this.isSetup()) {
			log.error("You have to setup this algorithm first!");
			throw new RuntimeException("You have to setup this algorithm first!");
		}
		return computeInterest(theBitset, this.itsDocument);
	}
	
	/**
	 * @param theOtherPeersPieces a map that has a bitset for every peer. The bitset should contain a TRUE for every finished piece, that peer has.
	 * @return a map stating, if I am interested in the peers. True meens: I am interested.
	 */
	public Map<BTContact, Boolean> computeInterest(Map<BTContact, BitSet> theOtherPeersPieces) {
		if (! this.isSetup()) {
			log.error("You have to setup this algorithm first!");
			throw new RuntimeException("You have to setup this algorithm first!");
		}
		return computeInterest(theOtherPeersPieces, this.itsDocument);
	}
	
	/**
	 * Use this method, if you don't want to instanciate or setup this algorithm.
	 * @param theBitset the bitset of the other peer, showing its finished pieces.
	 * @param theDocument the document that I have.
	 * @return Am I interested in this peer?
	 */
	public static boolean computeInterest(BitSet theBitset, BTDocument theDocument) {
		BitSet clonedBitset = (BitSet)theBitset.clone();
		clonedBitset.andNot(theDocument.getFinishedPieces());
		return ! BTBitSetUtil.isBitsetEmpty(clonedBitset);
	}
	
	/**
	 * Use this method, if you don't want to instanciate or setup this algorithm.
	 * @param theOtherPeersPieces a map that has a bitset for every peer. The bitset should contain a TRUE for every finished piece, that peer has.
	 * @param theDocument the document that I have.
	 * @return a map stating, if I am interested in the peers. True meens: I am interested.
	 */
	public static Map<BTContact, Boolean> computeInterest(Map<BTContact, BitSet> theOtherPeersPieces, BTDocument theDocument) {
		Map<BTContact, Boolean> result = new HashMap<BTContact, Boolean>(theOtherPeersPieces.size(), 1f);
		for (BTContact anOtherPeer : theOtherPeersPieces.keySet()) {
			result.put(anOtherPeer, computeInterest(theOtherPeersPieces.get(anOtherPeer), theDocument));
		}
		return result;
	}
	
}
