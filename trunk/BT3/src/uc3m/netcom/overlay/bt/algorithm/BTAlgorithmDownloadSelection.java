package uc3m.netcom.overlay.bt.algorithm;

import java.util.BitSet;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.math.random.RandomGenerator;

import uc3m.netcom.overlay.bt.BTContact;
import uc3m.netcom.overlay.bt.BTDocument;
import uc3m.netcom.overlay.bt.BTInternRequest;

/**
 * This algorithm interface is meant for algorithms that compute requests.
 * Request mean: We want other peers to upload some data the desired data to us.
 * @author Jan Stolzenburg
 */
public interface BTAlgorithmDownloadSelection extends BTAlgorithm {
	
	public void setup(BTDocument theDocument, RandomGenerator theRandomGenerator);
	
	/**
	 * This method computes a download request.
	 * You give it the current state and it returns the list of requests.
	 * @param theOtherPeersPieces is a map from the <code>BTContact</code> of a peer,
	 * to the bitset, describing which pieces this peer offeres.
	 * @param thePendingRequests is the collection of all currently pending results.
	 * @return a collection of requests, the peer should send.
	 */
	public Collection<BTInternRequest> computeRequests(Map<BTContact, BitSet> theOtherPeersPieces, Collection<BTInternRequest> thePendingRequests);
	
	public boolean sendCancel();
	
}
