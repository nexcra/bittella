package de.tud.kom.p2psim.overlay.bt.operation;


import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.math.random.RandomGenerator;
import org.apache.log4j.Logger;

import de.tud.kom.p2psim.api.common.OperationCallback;
import de.tud.kom.p2psim.api.overlay.DistributionStrategy;
import de.tud.kom.p2psim.api.transport.TransLayer;
import de.tud.kom.p2psim.api.transport.TransMsgEvent;
import de.tud.kom.p2psim.impl.common.Operations;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.util.logging.SimLogger;
import de.tud.kom.p2psim.overlay.bt.BTConstants;
import de.tud.kom.p2psim.overlay.bt.BTContact;
import de.tud.kom.p2psim.overlay.bt.BTDataStore;
import de.tud.kom.p2psim.overlay.bt.BTDocument;
import de.tud.kom.p2psim.overlay.bt.BTInternRequest;
import de.tud.kom.p2psim.overlay.bt.BTInternStatistic;
import de.tud.kom.p2psim.overlay.bt.manager.BTConnectionManager;
import de.tud.kom.p2psim.overlay.bt.message.BTPeerMessagePiece;

/**
 * This class controls the upload of data to other peers.
 * Therefore, it gets called regularly.
 * @param <OwnerType> the class of the component that owns this operation
 * @author Jan Stolzenburg
 */
public class BTOperationUpload<OwnerType extends DistributionStrategy> extends BTOperation<OwnerType, Void> {
	
	
	
	private BTDocument itsDocument;
	
	private TransLayer itsTransLayer;
	
	private BTContact itsOwnContact;
	
	private BTConnectionManager itsConnectionManager;
	
	private LinkedList<BTInternRequest> itsRequests;
	private LinkedList<TransMsgEvent> itsRequestEvents;
	private LinkedList<Long> itsRequestArrivels;
	
	private Collection<BTInternRequest> itsCanceledRequests;
	
	private long itsPeriod = BTConstants.PEER_UPLOAD_OPERATION_PERIOD;
	
	private BTOperationChoking<OwnerType> itsChokingOperation;
	
	private BTOperationKeepAlive<OwnerType> itsKeepAliveOperation;
	
	private BTInternStatistic itsStatistic;
	
	private boolean itsIsFirstTime;
	
	private BTDataStore itsDataBus;
	
	private long itsDurationOfUploadAfterDownloadFinished = BTConstants.DURATION_OF_UPLOAD_AFTER_DOWNLOAD_FINISHED;
	
	private static long theirRequestTimeout = BTConstants.PEER_REPLY_TIMEOUT;
	
	static final Logger log = SimLogger.getLogger(BTOperationUpload.class);
	
	
	
	@SuppressWarnings("unchecked")
	public BTOperationUpload(BTDataStore theDataBus, BTDocument theDocument, BTContact theOwnContact, OwnerType theOwningComponent, OperationCallback<Void> theOperationCallback, BTConnectionManager theConnectionManager, BTInternStatistic theStatistic, RandomGenerator theRandomGenerator) {
		super(theOwningComponent, theOperationCallback);
		this.itsDataBus = theDataBus;
		this.itsDocument = theDocument;
		this.itsTransLayer = this.getComponent().getHost().getTransLayer();
		this.itsOwnContact = theOwnContact;
		this.itsConnectionManager = theConnectionManager;
		this.itsStatistic = theStatistic;
		this.itsRequests = new LinkedList<BTInternRequest>();
		this.itsRequestEvents = new LinkedList<TransMsgEvent>();
		this.itsRequestArrivels = new LinkedList<Long>();
		this.itsCanceledRequests = new LinkedList<BTInternRequest>();
		this.itsChokingOperation = new BTOperationChoking<OwnerType>(this.itsDataBus, this.itsDocument, this.getComponent(), Operations.EMPTY_CALLBACK, this.itsStatistic, theRandomGenerator, this.itsConnectionManager);
		this.itsKeepAliveOperation = new BTOperationKeepAlive<OwnerType>(this.itsDataBus, this.itsDocument, this.itsOwnContact, this.getComponent(), Operations.EMPTY_CALLBACK, this.itsConnectionManager);
		this.itsIsFirstTime = true;
	}
	
	/**
	 * This method gets called periodically and controls all actions of this operation.
	 * It tries to reply to every stored request. But first,
	 * it makes multiple tests with the requests:
	 * Has it been canceled?
	 * Has it timed out?
	 * Is the peer choked?
	 * Do we have the requested data?
	 */
	@Override
	protected void execute() {
		if (this.isFinished()) {
			this.itsChokingOperation.stop(true);
			this.itsKeepAliveOperation.stop(true);
			this.itsStatistic.stopUpload();
			log.info("---Upload stopped at time " + Simulator.getCurrentTime() + " at '" + this.itsOwnContact.getOverlayID() + "'.");
			return;
		}
		if(this.itsIsFirstTime) {
			this.itsChokingOperation.execute();
			this.itsKeepAliveOperation.execute();
			this.itsIsFirstTime = false;
			this.itsStatistic.startUpload();
		}
		
		if (this.downloadFinished()) {
			this.scheduleOperationTimeout(this.calcDurationOfUploadAfterDownloadFinished());
		}
		
		this.scheduleWithDelay(this.itsPeriod);
		
		if (this.itsRequests.isEmpty())
			return;
		
		if ((this.itsRequests.size() != this.itsRequestEvents.size()) || (this.itsRequests.size() != this.itsRequestArrivels.size())) {
			this.itsRequests.clear();
			this.itsRequestEvents.clear();
			this.itsRequestArrivels.clear();
			throw new RuntimeException("The three lists 'itsRequests', 'itsRequestCommunicationIDs' and 'itsRequestArrivels' had different length! I cleared both.");
		}
		
		//Timeout of cancel requests. If they come to late, they would stay forever, otherwise.
		Collection<BTInternRequest> timedOutCancels = new LinkedList<BTInternRequest>();
		for (BTInternRequest aCanceledRequest : this.itsCanceledRequests) {
			if (aCanceledRequest.getCreationTime() + 15 * Simulator.MINUTE_UNIT < Simulator.getCurrentTime())
				timedOutCancels.add(aCanceledRequest);
		}
		for (BTInternRequest aCanceledRequest : timedOutCancels)
			this.itsCanceledRequests.remove(aCanceledRequest);
		
		while (! (this.itsRequests.isEmpty() || this.itsRequestEvents.isEmpty() || this.itsRequestArrivels.isEmpty())) {
			
			BTInternRequest aRequest = this.itsRequests.getFirst();
			TransMsgEvent aMessageEvent = this.itsRequestEvents.getFirst();
			Long aArrivelTime = this.itsRequestArrivels.getFirst();
			
			//Take care of cancels:
			if (this.itsCanceledRequests.contains(aRequest)) {
//				log.debug("Removed a canceled request: " + aRequest);
				this.itsCanceledRequests.remove(aRequest);
				this.itsRequests.removeFirst();
				this.itsRequestEvents.removeFirst();
				this.itsRequestArrivels.removeFirst();
				continue;
			}
			
			//Take care of timeouts:
			if (Simulator.getCurrentTime() > (aArrivelTime + theirRequestTimeout)) {
//				log.debug("Removed a timed out request: " + aRequest);
				this.itsRequests.removeFirst();
				this.itsRequestEvents.removeFirst();
				this.itsRequestArrivels.removeFirst();
				continue;
			}
			
			//Take care of Interest:
			//If a peer send a request and did not cancel it, he is still interested, even if he says he is not.
			//Therefore, we ignore this case.
//			if (! this.itsConnectionManager.getConnection(aRequest.getRequestingPeer()).isInterested()) {
////				log.debug("Removed uninterested request: " + aRequest);
//				this.itsRequests.removeFirst();
//				this.itsRequestCommunicationIDs.removeFirst();
//				this.itsRequestArrivels.removeFirst();
//				continue;
//			}
			
			//Take care of Choking:
			if (this.itsConnectionManager.getConnection(aRequest.getRequestingPeer()).amIChoking()) {
//				log.debug("Removed a choked request: " + aRequest);
				this.itsRequests.removeFirst();
				this.itsRequestEvents.removeFirst();
				this.itsRequestArrivels.removeFirst();
				continue;
			}
			
			//Check, if we have the requested piece:
			if (1 != this.itsDocument.getPieceState(aRequest.getPieceNumber())) {
				log.error("Removed a impossible request: " + aRequest);
				this.itsRequests.removeFirst();
				this.itsRequestEvents.removeFirst();
				this.itsRequestArrivels.removeFirst();
				continue;
			}
			
			BTPeerMessagePiece answer = new BTPeerMessagePiece(aRequest.getPieceNumber(), aRequest.getBlockNumber(), this.itsDocument.getNumberOfBytesInBlock(aRequest.getPieceNumber(), aRequest.getBlockNumber()), this.itsDocument.getKey(), true, this.itsOwnContact.getOverlayID(), aRequest.getRequestingPeer().getOverlayID());
//			this.itsConnectionManager.getConnection(aRequest.getRequestingPeer()).addMessage(answer);
			this.itsTransLayer.sendReply(answer, aMessageEvent, this.itsOwnContact.getTransInfo().getPort(), BTPeerMessagePiece.getStaticTransportProtocol());
			
			this.itsRequests.removeFirst();
			this.itsRequestEvents.removeFirst();
			this.itsRequestArrivels.removeFirst();
			this.itsStatistic.blockSendToPeer(aRequest.getRequestingPeer());
		}
	}
	
	/**
	 * Recalc the choking of the peers.
	 */
	public void recalcChoking() {
		this.itsChokingOperation.recalc();
	}
	
	/**
	 * @return Has the download finished?
	 */
	private boolean downloadFinished() {
		if (! this.itsDataBus.isPerTorrentDataStored(this.itsDocument.getKey(), "Download Finished"))
			return false;
		return (Boolean) this.itsDataBus.getPerTorrentData(this.itsDocument.getKey(), "Download Finished");
	}
	
	/**
	 * @return How long should we upload if the download finished?
	 */
	private long calcDurationOfUploadAfterDownloadFinished() {
		Long duration = (Long) this.itsDataBus.getGeneralData("DurationOfUploadAfterDownloadFinished");
		if (duration != null)
			return duration;
		return this.itsDurationOfUploadAfterDownloadFinished;
	}
	
	@Override
	public Void getResult() {
		return null;
	}
	
	@Override
	protected void operationTimeoutOccured() {
		this.itsDataBus.storePerTorrentData(this.itsDocument.getKey(), "Upload Stopped", true, (new Boolean(true)).getClass());
		this.operationFinished(true); //The timeout of the upload operation is the normal way to stop it. Therefore, success.
//		log.debug("Uploading document finished.");
	}
	
	/**
	 * If we receive a request, this method checks if we have the requested data block
	 * and stores the request until it gets processed.
	 * @param theRequest the received request
	 * @param theMessageEvent the message-receive-event.
	 */
	public void handleRequest(BTInternRequest theRequest, TransMsgEvent theMessageEvent) {
		if (1 != this.itsDocument.getPieceState(theRequest.getPieceNumber())) {
			log.warn("Received an request for an piece that I don't have: '" + theRequest.getPieceNumber() + "'");
			return;
		}
		if (! this.itsConnectionManager.getConnection(theRequest.getRequestingPeer()).isInterestedInMe()) {
			//This can happen, if the interest message was to slow.
//			log.warn("Received an request, but the peer is uninterested");
//			return;
		}
//		log.debug("Time: " + Simulator.getCurrentTime() + "; Peer: " + this.itsOwnContact.getOverlayID() + "; Requests: " + this.itsRequests.size() + "; Received a request: " + theRequest);
		this.itsRequests.addLast(theRequest);
		this.itsRequestEvents.addLast(theMessageEvent);
		this.itsRequestArrivels.addLast(Simulator.getCurrentTime());
	}
	
	/**
	 * If a requests gets canceled, this method stores this information until the requests gets processed and withdrawn.
	 * @param thePieceNumber the number of the piece in which the data block is located.
	 * @param theBlockNumber the number of the data block.
	 * @param theOtherPeer the peer that canceled the request.
	 */
	public void handleCancel(int thePieceNumber, int theBlockNumber, BTContact theOtherPeer) {
		this.itsCanceledRequests.add(new BTInternRequest(theOtherPeer, this.itsOwnContact, this.itsDocument.getKey(), thePieceNumber, theBlockNumber));
	}

}
