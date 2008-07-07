package uc3m.netcom.overlay.bt.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.math.random.RandomGenerator;
import org.apache.log4j.Logger;

import de.tud.kom.p2psim.impl.util.logging.SimLogger;
import uc3m.netcom.overlay.bt.BTContact;
import uc3m.netcom.overlay.bt.BTUtil;

/**
 * This algorithm computes the list of peers the tracker send to a peer.
 * It takes care of the amount of peers this list should have and which peer requested this list.
 * Currently, it returns a random subset of the known peers with the wanted size.
 * It takes care that the requesting peer isn't part of the answer.
 * @author Jan Stolzenburg
 */
public class BTAlgorithmTrackerPeerSelection implements BTAlgorithm {
	
	private RandomGenerator itsRandomGenerator;
	
	private boolean itsIsSetup = false;
	
	static final Logger log = SimLogger.getLogger(BTAlgorithmTrackerPeerSelection.class);
	
	public boolean isSetup() {
		return this.itsIsSetup;
	}
	
	/**
	 * To setup this algorithm, it just needs an random generator.
	 * @param theRandomGenerator This random generator is needed by the algorithm.
	 */
	public void setup(RandomGenerator theRandomGenerator) {
		this.itsRandomGenerator = theRandomGenerator;
		this.itsIsSetup = true;
	}
	
	/**
	 * Computes the list of peers.
	 * @param theOtherPeers The collection that stores all the peers that the tracker knows of.
	 * @param theRequestingPeer The peer that requests this list. It is never part of the resulting list.
	 * @param theAmount The number of peers that the list should contain. If we know enough peers, the list will have this size. Otherwise, it will be as large as possible.
	 * @return The list of peers that we send to the requesting peer.
	 */
	public Collection<BTContact> computePeerSelection(List<BTContact> theOtherPeers, BTContact theRequestingPeer, int theAmount) {
		if (! this.isSetup()) {
			log.error("You have to setup this algorithm first!");
			throw new RuntimeException("You have to setup this algorithm first!");
		}
		Set<Integer> indexOfSelectedPeers = BTUtil.getRandomSubSetIndexes(theAmount + 1, theOtherPeers.size(), this.itsRandomGenerator);
		ArrayList<BTContact> result = new ArrayList<BTContact>();
		for (int index : indexOfSelectedPeers) {
			result.add(theOtherPeers.get(index));
		}
		if (! result.isEmpty()) {
			if (result.contains(theRequestingPeer)) {
				result.remove(theRequestingPeer); //Take care that we don't send a colletion containing the requesting peer itself.
			}
			else {
				result.remove(0); //Remove one, never mind, which one. We just re-adjust the peer amount to get the "+ 1" away.
			}
		}
		return result;
	}
	
}
