package de.tud.kom.p2psim.overlay.bt.operation;

import java.util.Collection;
import java.util.Date;

import org.apache.log4j.Logger;

import de.tud.kom.p2psim.api.common.Message;
import de.tud.kom.p2psim.api.common.OperationCallback;
import de.tud.kom.p2psim.api.overlay.DHTNode;
import de.tud.kom.p2psim.api.overlay.OverlayID;
import de.tud.kom.p2psim.api.transport.TransInfo;
import de.tud.kom.p2psim.api.transport.TransLayer;
import de.tud.kom.p2psim.api.transport.TransMessageCallback;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.util.logging.SimLogger;
import de.tud.kom.p2psim.overlay.bt.BTConstants;
import de.tud.kom.p2psim.overlay.bt.BTContact;
import de.tud.kom.p2psim.overlay.bt.BTDataStore;
import de.tud.kom.p2psim.overlay.bt.BTTorrent;
import de.tud.kom.p2psim.overlay.bt.message.BTPeerToTrackerRequest;
import de.tud.kom.p2psim.overlay.bt.message.BTTrackerToPeerReply;

/**
 * This operation sends periodically statistic data to the tracker.
 * It reactivates itself every <code>this.itsPeriod</code> timesteps.
 * Use the method <code>stop</code> to stop it.
 * It is also responsible for requesting more peers from the tracker.
 * @param <OwnerType> the class of the component that owns this operation
 * @author Jan Stolzenburg
 *
 */
public class BTOperationSendStatistic<OwnerType extends DHTNode> extends BTOperation<OwnerType, Void> implements TransMessageCallback {
	
	
	
	private BTTorrent itsTorrent;
	
	private TransLayer itsTransLayer;
	
	private OverlayID itsOverlayID;
	
	private int itsCommunicationID;
	
	private short itsP2PPort;
	
	private long itsLastRequest;
	
	private BTDataStore itsDataBus;
	
	private static long theirCallPeriod = BTConstants.PEER_SEND_STATISTIC_OPERATION_CALL_PERIOD;
	
	private static long theirRequestPeriod = BTConstants.PEER_SEND_STATISTIC_OPERATION_REQUEST_PERIOD;
	
	private static long theirMinimumNumberOfContacts = BTConstants.PEER_MIN_NEIGHBOURS;
	
	private static long theirMessageTimeout = BTConstants.PEER_TO_TRACKER_MESSAGE_TIMEOUT;
	
	static final Logger log = SimLogger.getLogger(BTOperationSendStatistic.class);
	
	
	
	public BTOperationSendStatistic(BTDataStore theDataBus, short theP2PPort, BTTorrent theTorrent, OverlayID theOverlayID, OwnerType theOwningComponent, OperationCallback<Void> theOperationCallback) {
		super(theOwningComponent, theOperationCallback);
		this.itsDataBus = theDataBus;
		this.itsP2PPort = theP2PPort;
		this.itsTorrent = theTorrent;
		this.itsTransLayer = this.getComponent().getHost().getTransLayer();
		this.itsOverlayID = theOverlayID;
		this.itsLastRequest = Simulator.getCurrentTime();
	}
	
	@Override
	protected void execute() {
		if (this.isFinished()) {
			BTPeerToTrackerRequest request = new BTPeerToTrackerRequest(BTPeerToTrackerRequest.Reason.STOPPED, this.itsTorrent.getKey(), new BTContact(this.itsOverlayID, this.itsTransLayer.getLocalTransInfo(this.itsP2PPort)), null, this.itsOverlayID, this.itsTorrent.getTrackerID());
			this.itsTransLayer.send(request, this.itsTorrent.getTrackerAddress(), this.itsP2PPort, BTPeerToTrackerRequest.getStaticTransportProtocol());
			return;
		}
		if (this.uploadFinished()) {
			this.operationFinished(true);
			BTPeerToTrackerRequest request = new BTPeerToTrackerRequest(BTPeerToTrackerRequest.Reason.STOPPED, this.itsTorrent.getKey(), new BTContact(this.itsOverlayID, this.itsTransLayer.getLocalTransInfo(this.itsP2PPort)), null, this.itsOverlayID, this.itsTorrent.getTrackerID());
			this.itsTransLayer.send(request, this.itsTorrent.getTrackerAddress(), this.itsP2PPort, BTPeerToTrackerRequest.getStaticTransportProtocol());
			return;
		}
		
		if (this.requestMorePeers()) {
//			log.debug("Too less contacts: '" + ((Map<OverlayKey, Map<String, Object>>)this.itsDataBus.get("Torrents")).get(this.itsTorrent.getKey()).get("NumberOfConnections") + "'; requesting more! At: " + this.itsOverlayID);
			BTPeerToTrackerRequest request = new BTPeerToTrackerRequest(BTPeerToTrackerRequest.Reason.EMPTY, -1, this.itsTorrent.getKey(), new BTContact(this.itsOverlayID, this.itsTransLayer.getLocalTransInfo(this.itsP2PPort)), null, this.itsOverlayID, this.itsTorrent.getTrackerID());
			this.itsCommunicationID = this.itsTransLayer.sendAndWait(request, this.itsTorrent.getTrackerAddress(), this.itsP2PPort, BTPeerToTrackerRequest.getStaticTransportProtocol(), this, theirMessageTimeout);
		}
		else if ((this.itsLastRequest + (theirRequestPeriod * 0.95)) <= Simulator.getCurrentTime()) { //Is it (nearly) time for the next message to the tracker?
			BTPeerToTrackerRequest request = new BTPeerToTrackerRequest(BTPeerToTrackerRequest.Reason.EMPTY, this.itsTorrent.getKey(), new BTContact(this.itsOverlayID, this.itsTransLayer.getLocalTransInfo(this.itsP2PPort)), null, this.itsOverlayID, this.itsTorrent.getTrackerID());
			this.itsTransLayer.send(request, this.itsTorrent.getTrackerAddress(), this.itsP2PPort, BTPeerToTrackerRequest.getStaticTransportProtocol());
		}
		
		this.scheduleWithDelay(theirCallPeriod);
		
		if (this.itsOverlayID.toString().equals("0")) { //TODO: Tells the user, what time it is in the simulation.
			log.info("Simulated Time: " + (Simulator.getCurrentTime() / Simulator.MINUTE_UNIT) + " Minutes; Real time: " + (new Date()).toString() + ";");
		}
	}
	
	private boolean uploadFinished() {
		if (! this.itsDataBus.isPerTorrentDataStored(this.itsTorrent.getKey(), "Upload Stopped"))
			return false;
		return (Boolean) this.itsDataBus.getPerTorrentData(this.itsTorrent.getKey(), "Upload Stopped");
	}
	
	private boolean requestMorePeers() {
		if (! this.itsDataBus.isPerTorrentDataStored(this.itsTorrent.getKey(), "NumberOfConnections"))
			return false;
		if (this.itsDataBus.isPerTorrentDataStored(this.itsTorrent.getKey(), "Download Finished") && (Boolean) this.itsDataBus.getPerTorrentData(this.itsTorrent.getKey(), "Download Finished"))
			return false;
		return (((Integer)this.itsDataBus.getPerTorrentData(this.itsTorrent.getKey(), "NumberOfConnections")) < theirMinimumNumberOfContacts);
	}
	
	public static long getPeriod() {
		return theirCallPeriod;
	}
	
	@Override
	public Void getResult() {
		return null;
	}
	
	@Override
	protected void operationTimeoutOccured() {
		this.operationFinished(false);
		log.warn("Sending statistic data timed out.");
	}
	
	public void messageTimeoutOccured(int commId) {
		//Nothing to do.
	}
	
	public void receive(Message theMessage, TransInfo theSenderInfo, int theCommunicationID) {
		assert (this.itsCommunicationID == theCommunicationID);
		assert (this.itsTorrent.getTrackerAddress().equals(theSenderInfo));
		if (!(theMessage instanceof BTTrackerToPeerReply)) {
			log.warn("Expected a 'BTTrackerToPeerReply', but got a '" + theMessage.getClass().getSimpleName() + "'!");
			return;
		}
		Collection<BTContact> theNewPeers = ((BTTrackerToPeerReply)theMessage).getNewPeerSet();
		for (BTContact anOtherPeer : theNewPeers) {
			this.itsDataBus.storePeer(this.itsTorrent.getKey(), anOtherPeer);
		}
	}
	
}
