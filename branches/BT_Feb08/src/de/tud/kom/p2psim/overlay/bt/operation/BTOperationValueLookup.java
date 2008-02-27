package de.tud.kom.p2psim.overlay.bt.operation;

import java.util.Collection;

import org.apache.log4j.Logger;

import de.tud.kom.p2psim.api.common.Message;
import de.tud.kom.p2psim.api.common.OperationCallback;
import de.tud.kom.p2psim.api.overlay.DHTNode;
import de.tud.kom.p2psim.api.overlay.OverlayID;
import de.tud.kom.p2psim.api.overlay.OverlayKey;
import de.tud.kom.p2psim.api.transport.TransInfo;
import de.tud.kom.p2psim.api.transport.TransLayer;
import de.tud.kom.p2psim.api.transport.TransMessageCallback;
import de.tud.kom.p2psim.impl.util.logging.SimLogger;
import de.tud.kom.p2psim.overlay.bt.BTConstants;
import de.tud.kom.p2psim.overlay.bt.BTContact;
import de.tud.kom.p2psim.overlay.bt.BTDataStore;
import de.tud.kom.p2psim.overlay.bt.BTTorrent;
import de.tud.kom.p2psim.overlay.bt.message.BTPeerToTrackerRequest;
import de.tud.kom.p2psim.overlay.bt.message.BTTrackerToPeerReply;

/**
 * This class is responsible for the first contact to the tracker.
 * It tells the tracker we are just starting the download and need some peers.
 * @param <OwnerType> the class of the component that owns this operation
 * @author Jan Stolzenburg
 */
public class BTOperationValueLookup<OwnerType extends DHTNode> extends BTOperation<OwnerType, Collection<BTContact>> implements TransMessageCallback {
	
	
	
	private Collection<BTContact> itsResult;
	
	private BTTorrent itsTorrent;
	
	private TransLayer itsTransLayer;
	
	private OverlayID itsOverlayID;
	
	private short itsTrackerPort;
	
	private short itsP2PPort;
	
	private byte attempts = 0;
	
	private BTDataStore itsDataBus;
	
	private static long theirMessageTimeout = BTConstants.PEER_TO_TRACKER_MESSAGE_TIMEOUT;
	
	private static byte maxAttempts = BTConstants.PEER_TO_TRACKER_MAX_MESSAGE_RETRY;
	
	private static long theirOperationTimeout = BTConstants.PEER_TO_TRACKER_CONTACT_TIMEOUT;
	
	static final Logger log = SimLogger.getLogger(BTOperationValueLookup.class);
	
	
	
	public BTOperationValueLookup(BTDataStore theDataBus, short theTrackerPort, short theP2PPort, BTTorrent theTorrent, OverlayID theOverlayID, OwnerType theOwningComponent, OperationCallback<Collection<BTContact>> theOperationCallback) {
		super(theOwningComponent, theOperationCallback);
		this.itsDataBus = theDataBus;
		this.itsTrackerPort = theTrackerPort;
		this.itsP2PPort = theP2PPort;
		this.itsTorrent = theTorrent;
		this.itsTransLayer = this.getComponent().getHost().getTransLayer();
		this.itsOverlayID = theOverlayID;
	}
	
	@Override
	protected void execute() {
		this.scheduleOperationTimeout(theirOperationTimeout);
		this.tryIt();
	}
	
	/**
	 * If the first connection attemp fails, we will retry it.
	 * This method handles one of such tries.
	 * It gets called for every retry.
	 */
	private void tryIt() {
		this.attempts += 1;
		BTPeerToTrackerRequest request = new BTPeerToTrackerRequest(BTPeerToTrackerRequest.Reason.STARTED, -1, this.itsTorrent.getKey(), new BTContact(this.itsOverlayID, this.itsTransLayer.getLocalTransInfo(this.itsP2PPort)), this.itsOverlayID, this.itsTorrent.getTrackerID());
		this.itsTransLayer.sendAndWait(request, this.itsTorrent.getTrackerAddress(), this.itsTrackerPort, BTPeerToTrackerRequest.getStaticTransportProtocol(), this, theirMessageTimeout);
	}
	
	@Override
	public Collection<BTContact> getResult() {
		return this.itsResult;
	}
	
	@Override
	protected void operationTimeoutOccured() {
		this.operationFinished(false);
		log.warn("Peer value lookup operation timed out.");
	}
	
	public void messageTimeoutOccured(int commId) {
		if (this.isFinished())
			return;
		if (this.attempts < maxAttempts) {
			this.tryIt();
		}
		else {
			this.operationFinished(false);
			log.warn("Peer message to the tracker timed out.");
		}
	}
	
	/**
	 * We receive the tracker response via this method.
	 * It then finished this operation which will return
	 * the list of peers to the event listener of this operation.
	 * @param theMessage the tracker response
	 * @param theSenderInfo the tracker address
	 * @param theCommunicationID the communication id usefull for direct replies.
	 */
	public void receive(Message theMessage, TransInfo theSenderInfo, int theCommunicationID) {
		if (!(theMessage instanceof BTTrackerToPeerReply)) {
			log.warn("Expected a 'BTTrackerToPeerReply', but got a '" + theMessage.getClass().getSimpleName() + "'!");
			return;
		}
		Collection<BTContact> theNewPeers = ((BTTrackerToPeerReply)theMessage).getNewPeerSet();
		this.itsResult = theNewPeers;
		for (BTContact anOtherPeer : theNewPeers) {
			this.itsDataBus.storePeer(this.getDocumentKey(), anOtherPeer);
		}
		this.operationFinished(true);
	}
	
	public OverlayKey getDocumentKey() {
		return this.itsTorrent.getKey();
	}
	
}
