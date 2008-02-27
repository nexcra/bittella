package de.tud.kom.p2psim.overlay.bt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import de.tud.kom.p2psim.impl.simengine.Simulator;

/**
 * This class stores the statistic data in the clients.
 * The client has to have access to these data for different algorithms.
 * @author Jan Stolzenburg
 */
public class BTInternStatistic {
	
	private long itsDownloadStart;
	private long itsDownloadStop;
	private long itsUploadStart;
	private long itsUploadStop;
	
	/**
	 * This map saves for every peer, at which time I received a block from it.
	 */
	private Map<BTContact, List<Long>> itsDownloadStatistic;
	
	/**
	 * This map saves for every peer, at which time I uploaded a block to it.
	 */
	private Map<BTContact, List<Long>> itsUploadStatistic;
	
	public BTInternStatistic() {
		this.itsDownloadStatistic = new HashMap<BTContact, List<Long>>();
		this.itsUploadStatistic = new HashMap<BTContact, List<Long>>();
		this.itsDownloadStart = Long.MIN_VALUE;
		this.itsDownloadStop = Long.MIN_VALUE;
		this.itsUploadStart = Long.MIN_VALUE;
		this.itsUploadStop = Long.MIN_VALUE;
	}
	
	/**
	 * Store the time of the download start.
	 */
	public void startDownload() {
		this.itsDownloadStart = Simulator.getCurrentTime();
	}
	
	/**
	 * Store the time of the download stop.
	 * In most cases, the client has finished the download, when it stops.
	 */
	public void stopDownload() {
		this.itsDownloadStop = Simulator.getCurrentTime();
	}
	
	/**
	 * Store the time of the upload start.
	 */
	public void startUpload() {
		this.itsUploadStart = Simulator.getCurrentTime();
	}
	
	/**
	 * Store the time of the upload stop.
	 */
	public void stopUpload() {
		this.itsUploadStop = Simulator.getCurrentTime();
	}
	
	public long getDownloadStart() {
		return this.itsDownloadStart;
	}
	
	public long getDownloadStop() {
		return this.itsDownloadStop;
	}
	
	public long getUploadStart() {
		return this.itsUploadStart;
	}
	
	public long getUploadStop() {
		return this.itsUploadStop;
	}
	
	/**
	 * Adds a block to the download statistic for this peer.
	 * @param theSender the sender of the downloaded block.
	 */
	public void blockReceivedFromPeer(BTContact theSender) {
		if (! this.itsDownloadStatistic.containsKey(theSender)) {
			this.itsDownloadStatistic.put(theSender, new ArrayList<Long>());
		}
		this.itsDownloadStatistic.get(theSender).add(Simulator.getCurrentTime());
	}
	
	/**
	 * Adds a block to the upload statistic for this peer.
	 * @param theReceiver the receiver of the uploaded block.
	 */
	public void blockSendToPeer(BTContact theReceiver) {
		if (! this.itsUploadStatistic.containsKey(theReceiver)) {
			this.itsUploadStatistic.put(theReceiver, new ArrayList<Long>());
		}
		this.itsUploadStatistic.get(theReceiver).add(Simulator.getCurrentTime());
	}
	
	/**
	 * Returns the statistic for the block that the current peer received from the given peer.
	 * @param thePeer the peer how uploaded the data to us.
	 * @return the list of the times at which we received a block from that peer. 
	 */
	public List<Long> getDownloadStatisticForPeer(BTContact thePeer) {
		if (this.itsDownloadStatistic.containsKey(thePeer))
			return this.itsDownloadStatistic.get(thePeer);
		return new ArrayList<Long>(0);
	}
	
	/**
	 * Returns the statistic for the block that the current peer send to the given peer.
	 * @param thePeer the peer how downloaded the data from us.
	 * @return the list of the times at which we send a block to that peer. 
	 */
	public List<Long> getUploadStatisticForPeer(BTContact thePeer) {
		if (this.itsUploadStatistic.containsKey(thePeer))
			return this.itsUploadStatistic.get(thePeer);
		return new ArrayList<Long>(0);
	}
	
	/**
	 * Returns all stored upload statistics.
	 * @return all stored upload statictics.
	 */
	public Map<BTContact, List<Long>> getUploadStatistic() {
		return this.itsUploadStatistic;
	}
	
	/**
	 * Returns all stored download statistics.
	 * @return all stored download statictics.
	 */
	public Map<BTContact, List<Long>> getDownloadStatistic() {
		return this.itsDownloadStatistic;
	}
	
}
