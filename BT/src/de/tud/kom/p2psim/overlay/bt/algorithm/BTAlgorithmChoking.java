package de.tud.kom.p2psim.overlay.bt.algorithm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.random.RandomGenerator;
import org.apache.log4j.Logger;

import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.util.logging.SimLogger;
import de.tud.kom.p2psim.overlay.bt.BTConstants;
import de.tud.kom.p2psim.overlay.bt.BTContact;
import de.tud.kom.p2psim.overlay.bt.BTDocument;
import de.tud.kom.p2psim.overlay.bt.BTInternStatistic;
import de.tud.kom.p2psim.overlay.bt.manager.BTConnectionManager;

/**
 * This class contains the choking algorithm.
 * @author Jan Stolzenburg
 */
public class BTAlgorithmChoking {
	
	private BTDocument itsDocument;
	
	private BTInternStatistic itsStatistic;
	
	private RandomGenerator itsRandomGenerator;
	
	private boolean itsIsSetup = false;
	
	private LinkedList<BTContact> itsOptimisticUnchoke;
	
	private LinkedList<Long> itsOptimisticUnchokeTime;
	
	private BTConnectionManager itsConnectionManager;
	
	/**
	 * In seeder state, we have a three-tick-state-cycle. Tick 1 and 2 are the same. But tick 3 is different.
	 * -1: Tick 1
	 *  0: Tick 2
	 * +1: Tick 3
	 */
	private byte itsSeederStateCounter;
	
	//private static int theirRegularUnchokeNumberAsLeecher = BTConstants.CHOKING_NUMBER_OF_REGULAR_UNCHOKES;
        private int theirRegularUnchokeNumberAsLeecher = BTConstants.CHOKING_NUMBER_OF_REGULAR_UNCHOKES;
	
	private static int theirOptimisticUnchokeNumberAsLeecher = BTConstants.CHOKING_NUMBER_OF_OPTIMISTIC_UNCHOKES;
	
	private final static long theirOptimisticUnchokeTime = BTConstants.CHOKING_OPTIMISTIC_CHOKING_RECALC_PERIOD;
	
	private static int theirRegularUnchokeNumberAsSeeder = BTConstants.CHOKING_NUMBER_OF_REGULAR_UNCHOKES;
	
	private static int theirOptimisticUnchokeNumberAsSeeder = BTConstants.CHOKING_NUMBER_OF_OPTIMISTIC_UNCHOKES;
	 
        static final Logger log = SimLogger.getLogger(BTAlgorithmChoking.class);
	
	/**
	 * Use this method to use the algorithm.
	 * It will remove all peers that are choked.
	 * Therefore, the given list will contain only the unchoked peers after this call.
	 * @param theContacts the list of contacts that we have. After this call, it contains only the unchoked peers.
	 */
	public void filterUploadContacts(Collection<BTContact> theContacts) {
		if (! this.isSetup()) {
			log.error("You have to setup this algorithm first!");
			throw new RuntimeException("You have to setup this algorithm first!");
		}
		if (theContacts.isEmpty())
			return;
		if (this.itsDocument.getState() == BTDocument.State.COMPLETE) {
			this.filterInSeederState(theContacts);
		}
		else {
			this.filterInLeecherState(theContacts);
		}
	}
	
	public void setup(BTDocument theDocument, BTConnectionManager theConnectionManager, BTInternStatistic theStatistic, RandomGenerator theRandomGenerator) {
		this.itsDocument = theDocument;
		this.itsRandomGenerator = theRandomGenerator;
		this.itsOptimisticUnchoke = new LinkedList<BTContact>();
		this.itsOptimisticUnchokeTime = new LinkedList<Long>();
		this.itsStatistic = theStatistic;
		this.itsConnectionManager = theConnectionManager;
		this.itsSeederStateCounter = -1;
		this.itsIsSetup = true;
	}
	
	public boolean isSetup() {
		return this.itsIsSetup;
	}
	
	/**
	 * If the peer is in seeder state, this method computes the choking.
	 * @param theContacts the list of contacts that we have. After this call, it contains only the unchoked peers.
	 */
	private void filterInSeederState(Collection<BTContact> theContacts) {
		int regularUnchokeNumber = theirRegularUnchokeNumberAsSeeder;
		int optimisticUnchokeNumber = theirOptimisticUnchokeNumberAsSeeder;
		LinkedList<BTContact> sortedPeers = sortInSeederState(theContacts);
		theContacts.clear();
		
		if (this.itsSeederStateCounter <= 0) { //Phase -1 and 0.
			if (sortedPeers.size() > regularUnchokeNumber) {
				
				//Regular unchoke:
				for (int i = 0; (i < regularUnchokeNumber) && (i < sortedPeers.size()); i++)
					theContacts.add(sortedPeers.get(i));
				
				//Optimistic unchoke:
				theContacts.add(sortedPeers.get(regularUnchokeNumber + this.itsRandomGenerator.nextInt(sortedPeers.size() - regularUnchokeNumber))); //We don't want to unchoke one of the regular unchokes as optimistic unchoke. And we don't want errors if we have less than 3 peers.
			}
			else { //to few peers.
				theContacts.addAll(sortedPeers);
			}
			this.itsSeederStateCounter += 1;
		}
		else { //Phase +1
			
			//Only regular unchoke:
			for (int i = 0; (i < (regularUnchokeNumber + optimisticUnchokeNumber)) && (i < sortedPeers.size()); i++)
				theContacts.add(sortedPeers.get(i));
			this.itsSeederStateCounter = -1;
		}
	}
	
	/**
	 * Sort the peers accordingly to the specification of BitTorrent for the seeder state:
	 * @param theContacts the list of peers
	 * @return the sorted list of peers.
	 */
	private LinkedList<BTContact> sortInSeederState(Collection<BTContact> theContacts) {
		Collection<BTContact> copyOfContacts = new LinkedList<BTContact>(theContacts);
		LinkedList<BTContact> sortedPeers = new LinkedList<BTContact>();
		Collection<BTContact> unchokedPeers = new LinkedList<BTContact>();
		
		//Get the peers that were unchoked(CHANGE from choke to unchoke) in the last few seconds.
		for (BTContact anOtherPeer : copyOfContacts)
			if ((! this.itsConnectionManager.getConnection(anOtherPeer).amIChoking()) && (this.itsConnectionManager.getConnection(anOtherPeer).getLastUnchoking() + 20 * Simulator.SECOND_UNIT >= Simulator.getCurrentTime())) //TODO: Konstante
				unchokedPeers.add(anOtherPeer);
		
		//Store the remaining peers.
		copyOfContacts.removeAll(unchokedPeers);
		long latestUnchokeTime;
		int uploadedBlocks;
		BTContact winner;
		int uploadedCurrentPeer;
		
		//Sort the peers that were unchoked in the last few seconds.
		while (! unchokedPeers.isEmpty()) {
			latestUnchokeTime = Long.MIN_VALUE;
			uploadedBlocks = -1;
			winner = null;
			
			//Sort them. Those that were unchoked at last come first.
			for (BTContact anOtherPeer : unchokedPeers) {
				if (this.itsConnectionManager.getConnection(anOtherPeer).getLastUnchoking() >= latestUnchokeTime) {
					uploadedCurrentPeer = this.uploadedSince(this.itsConnectionManager.getConnection(anOtherPeer).getLastUnchoking(), anOtherPeer);
					
					//if two peers were unchoked at the same time, prefer the peer that uploaded more.
					if ((this.itsConnectionManager.getConnection(anOtherPeer).getLastUnchoking() == latestUnchokeTime) && (uploadedCurrentPeer < uploadedBlocks))
							continue;
					latestUnchokeTime = this.itsConnectionManager.getConnection(anOtherPeer).getLastUnchoking();
					winner = anOtherPeer;
					uploadedBlocks = uploadedCurrentPeer;
				}
			}
			sortedPeers.addLast(winner);
			unchokedPeers.remove(winner);
		}
		
		//Sort the rest of the peers. The peer that uploaded the most blocks in the last few seconds comes first.
		//Append them to the other peers that have been sorted before.
		while (! copyOfContacts.isEmpty()) {
			uploadedBlocks = -1;
			winner = null;
			for (BTContact anOtherPeer : copyOfContacts) {
				uploadedCurrentPeer = uploadedSince(Simulator.getCurrentTime() - 20 * Simulator.SECOND_UNIT, anOtherPeer); //Upload rate in the last 20 seconds. //TODO: CONSTANT
				if (uploadedCurrentPeer > uploadedBlocks) {
					winner = anOtherPeer;
					uploadedBlocks = uploadedCurrentPeer;
				}
			}
			sortedPeers.addLast(winner);
			copyOfContacts.remove(winner);
		}
		if ((! sortedPeers.containsAll(theContacts)) || (! theContacts.containsAll(sortedPeers)))
			log.error("Intern sorting failure!");
		return sortedPeers;
	}
	
	/**
	 * How many block did the peer upload to me since the given time?
	 * @param theTime from this time on, we count the upload.
	 * @param theContact the peer a want the information for.
	 * @return the number of uploaded blocks.
	 */
	private int uploadedSince(long theTime, BTContact theContact) {
		int result = 0;
		List<Long> uploadPiecesTimes = this.itsStatistic.getUploadStatisticForPeer(theContact);
		int currentPosition = uploadPiecesTimes.size() - 1;
		while ((currentPosition >= 0) && (uploadPiecesTimes.get(currentPosition) >= theTime)) {
			result += 1;
			currentPosition -= 1;
		}
		return result;
	}
	
	/**
	 * If the peer is in leecher state, this method computes the choking.
	 * @param theContacts the list of contacts that we have. After this call, it contains only the unchoked peers.
	 */
	private void filterInLeecherState(Collection<BTContact> theContacts) {
                //log.info("Number of Unchocked peers:"+this.theirRegularUnchokeNumberAsLeecher);
		if (theContacts.size() <= (theirRegularUnchokeNumberAsLeecher + theirOptimisticUnchokeNumberAsLeecher))
			return; //To few peers in the list. We return just all.
		LinkedList<BTContact> originalContacts = new LinkedList<BTContact>(theContacts);
		this.doAntiSnabbing(theContacts);
		
		//Refill the list, if we removed to much:
		if (theContacts.size() < (theirRegularUnchokeNumberAsLeecher + theirOptimisticUnchokeNumberAsLeecher)) {
			Iterator<BTContact> iter = originalContacts.iterator();
			BTContact currentContact;
			while ((theContacts.size() < (theirRegularUnchokeNumberAsLeecher + theirOptimisticUnchokeNumberAsLeecher)) && iter.hasNext()) {
				currentContact = iter.next();
				if (! theContacts.contains(currentContact))
					theContacts.add(currentContact);
			}
		}
		if (theContacts.size() <= (theirRegularUnchokeNumberAsLeecher + theirOptimisticUnchokeNumberAsLeecher))
			return; //To few peers in the list. We return just all.
		
		LinkedList<BTContact> candidates = new LinkedList<BTContact>(theContacts);
		this.doRegularUnchoke(theContacts);
		LinkedList<BTContact> remaining = new LinkedList<BTContact>();
		
		//Time out of optimistic unchokes:
		while ((! this.itsOptimisticUnchokeTime.isEmpty()) && ((this.itsOptimisticUnchokeTime.getFirst() + theirOptimisticUnchokeTime) < Simulator.getCurrentTime())) {
			this.itsOptimisticUnchoke.removeFirst();
			this.itsOptimisticUnchokeTime.removeFirst();
		}
		BTContact winner = null;
		
		//While theere are to few peers unchoked, and we still have choked peers, do further optimistic unchokes.
		while ((theContacts.size() < (theirRegularUnchokeNumberAsLeecher + theirOptimisticUnchokeNumberAsLeecher)) && (theContacts.size() < candidates.size())) {
			remaining.clear();
			remaining.addAll(candidates);
			remaining.removeAll(theContacts);
			//We cleard it, added all known peers and removed the already unchoked. Those who are still choked remain. (For further unchokes.
			
			winner = this.doOptimisticUnchoke(remaining);
			theContacts.add(winner);
			this.itsOptimisticUnchoke.addLast(winner);
			this.itsOptimisticUnchokeTime.addLast(Simulator.getCurrentTime());
		}
	}
	
	private void doAntiSnabbing(Collection<BTContact> theContacts) {
		
		//Delete every peer from the list that didn't upload anything in th last few seconds.
		for (BTContact anOtherPeer : new LinkedList<BTContact>(theContacts)) {
			List<Long> temp = this.itsStatistic.getDownloadStatisticForPeer(anOtherPeer);
			if ((temp.size() == 0) || (Simulator.getCurrentTime() > (temp.get(temp.size() - 1) + 30 * Simulator.SECOND_UNIT))) //If the last received block is older than 30 seconds, we have nothing received in the last 30 seconds from it. Drop it! //TODO: CONSTANT
				theContacts.remove(anOtherPeer);
		}
	}
	
	/**
	 * Do regular unchoke in leecher state.
	 * @param theContacts the peers that we can unchoke.
	 */
	private void doRegularUnchoke(Collection<BTContact> theContacts) {
		if (theContacts.size() <= theirRegularUnchokeNumberAsLeecher)
			return;
		long thirtySecondsAgo; //Stores the time of '30 seconds before now'.
		
		//If the simulator started the time at 'Long.MIN_VALUE', we cannot substract 30 second. This could would lead to an underflow, if the simulator has simulated less than 30 seconds.
		if ((Long.MIN_VALUE + 30 * Simulator.SECOND_UNIT) > Simulator.getCurrentTime()) //TODO: CONSTANT???
			thirtySecondsAgo = Long.MIN_VALUE;
		else
			thirtySecondsAgo = Simulator.getCurrentTime() - 30 * Simulator.SECOND_UNIT; //The normale case: We are more than 30 seconds from Long.MIN_VALUE away.
		
		//Calculate the number of blocks that each peer send us currently(in the last 30 seconds).
		Map<BTContact, Integer> numberOfSendBlocks = new HashMap<BTContact, Integer>();
		for (BTContact anOtherPeer : theContacts) {
			numberOfSendBlocks.put(anOtherPeer, 0);
			List<Long> temp = this.itsStatistic.getDownloadStatisticForPeer(anOtherPeer);
			for (int i = temp.size() - 1; (i >= 0) && (temp.get(i) >= thirtySecondsAgo); i--)
				numberOfSendBlocks.put(anOtherPeer, numberOfSendBlocks.get(anOtherPeer) + 1);
		}
		
		
		Collection<BTContact> winner = new LinkedList<BTContact>();
		BTContact theWinner = null;
		int score;
		//As long as we don't have enough unchoked peers, unchoke another peer.
		//We unchoke the peers which send us the most data in the last few seconds.
    
		while((winner.size() < theirRegularUnchokeNumberAsLeecher) && ! numberOfSendBlocks.isEmpty()) {
			
			//Find the biggest upload amount and store the peer for getting unchoked.
			score = -1;
			for (BTContact anOtherPeer : numberOfSendBlocks.keySet()) {
				if (score < numberOfSendBlocks.get(anOtherPeer)) {
					score = numberOfSendBlocks.get(anOtherPeer);
					theWinner = anOtherPeer;
				}
			}
			if (theWinner != null) {
				winner.add(theWinner);
				numberOfSendBlocks.remove(theWinner);
			}
			else {
				log.error("Internal error in choking algorithm!");
				break; //Something went wrong...
			}
		}
		theContacts.clear();
		theContacts.addAll(winner);
	}
	
	/**
	 * Do optimistic unchoke in leecher state.
	 * @param theContacts the list of contacts that we have.
	 * @return the winner (optimistic unchoke peer)
	 */
	private BTContact doOptimisticUnchoke(LinkedList<BTContact> theContacts) {
		if (theContacts.isEmpty())
			return null;
		int winNumber = this.itsRandomGenerator.nextInt(theContacts.size());
		return theContacts.get(winNumber);
	}
        
	
}
