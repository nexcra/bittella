package uc3m.netcom.overlay.bt.operation;

import java.util.LinkedList;

import uc3m.netcom.common.OperationCallback;
import uc3m.netcom.common.SupportOperations;
//import de.tud.kom.p2psim.impl.simengine.Simulator;
import uc3m.netcom.overlay.bt.BTClientApplication;
import uc3m.netcom.overlay.bt.BTTorrent;

/**
 * This class starts peers at given times.
 * It is usefull, if you want to have more control over the scenario than the XML file allows.
 * @param <OwnerType> the class of the component that owns this operation
 * @author Jan Stolzenburg
 */
public class BTOperationPeerStarter<OwnerType extends SupportOperations> extends BTOperation<OwnerType, Void> {
	
	private LinkedList<Long> thePeersStartTime;
	
	private LinkedList<BTClientApplication> itsPeers;
	
	private BTTorrent itsTorrent;
	
	/**
	 * We save the start time for the case that the scheduler doesn't start at 0.
	 */
	private long itsStartTime = Integer.MAX_VALUE;
	
	/**
	 * 
	 * @param thePeersStartTime The SORTED list of start times for clients. The time starts at 0.
	 * @param thePeers The list of clients. They have to be in the same order as the start times. (The two lists are processed synchronously.)
	 * @param theTorrent The torrent that the peers should join and download.
	 * @param theOwningComponent The owner of this operation.
	 * @param theOperationCallback The callback for this operation.
	 */
	public BTOperationPeerStarter(LinkedList<Long> thePeersStartTime, LinkedList<BTClientApplication> thePeers, BTTorrent theTorrent, OwnerType theOwningComponent, OperationCallback<Void> theOperationCallback) {
		super(theOwningComponent, theOperationCallback);
		if (thePeersStartTime.size() != thePeers.size())
			throw new RuntimeException("The list of peers and the list of the peers start times have different sizes!");
		this.thePeersStartTime = thePeersStartTime;
		this.itsPeers = thePeers;
		this.itsTorrent = theTorrent;
	}
	
	@Override
	protected void execute() {
		long time = System.currentTimeMillis();
		if (this.itsStartTime == Integer.MAX_VALUE) { //That means: If this is the first run, store the simulator start time and use it later as time setoff.
			this.itsStartTime = time;
			this.scheduleImmediately();
			return;
		}
		while ((! this.thePeersStartTime.isEmpty()) && ((this.thePeersStartTime.getFirst() + this.itsStartTime) <= time)) {
			this.itsPeers.getFirst().downloadDocument(this.itsTorrent);
			this.thePeersStartTime.removeFirst();
			this.itsPeers.removeFirst();
		}
		if (! this.thePeersStartTime.isEmpty())
			this.scheduleAtTime(this.thePeersStartTime.getFirst() + this.itsStartTime);
	}
	
	@Override
	public Void getResult() {
		return null;
	}
	
	@Override
	protected void operationTimeoutOccured() {
		this.operationFinished(false);
//		log.warn("Starting peers timed out.");
	}
	
}
