package uc3m.netcom.overlay.bt.operation;


import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.math.random.RandomGenerator;
//import org.apache.log4j.Logger;

import uc3m.netcom.common.OperationCallback;
import uc3m.netcom.common.DistributionStrategy;
import uc3m.netcom.common.Operations;
import uc3m.netcom.transport.TransLayer;
import uc3m.netcom.transport.TransMsgEvent;
//import de.tud.kom.p2psim.impl.common.Operations;
//import de.tud.kom.p2psim.impl.simengine.Simulator;
//import de.tud.kom.p2psim.impl.util.logging.SimLogger;
import uc3m.netcom.overlay.bt.BTConstants;
import uc3m.netcom.overlay.bt.BTContact;
import uc3m.netcom.overlay.bt.BTDataStore;
import uc3m.netcom.overlay.bt.BTDocument;
import uc3m.netcom.overlay.bt.BTInternRequest;
import uc3m.netcom.overlay.bt.BTInternStatistic;
import uc3m.netcom.overlay.bt.BTPeerDistributeNode;
import uc3m.netcom.overlay.bt.manager.BTConnectionManager;
import uc3m.netcom.overlay.bt.message.BTPeerMessagePiece;

/**
 * This class controls the upload of data to other peers.
 * Therefore, it gets called regularly.
 * @param <OwnerType> the class of the component that owns this operation
 * @author Jan Stolzenburg
 */
public class BTOperationUpload<OwnerType extends DistributionStrategy> extends BTOperation<OwnerType, Void> implements Runnable {
	
	
	
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
	
//	static final Logger log = SimLogger.getLogger(BTOperationUpload.class);
	
	
	
	@SuppressWarnings("unchecked")
	public BTOperationUpload(BTDataStore theDataBus, BTDocument theDocument, BTContact theOwnContact, OwnerType theOwningComponent, OperationCallback<Void> theOperationCallback, BTConnectionManager theConnectionManager, BTInternStatistic theStatistic, RandomGenerator theRandomGenerator) {
		super(theOwningComponent, theOperationCallback);
		this.itsDataBus = theDataBus;
		this.itsDocument = theDocument;
		this.itsTransLayer = ((BTPeerDistributeNode)theOwningComponent).getTransLayer();
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

        }
        
        public void run(){
            
            while(!this.isFinished()){
		if (this.isFinished()) {
			this.itsChokingOperation.stop(true);
			this.itsKeepAliveOperation.stop(true);
			this.itsStatistic.stopUpload();
			//log.info("---Upload stopped at time " + Simulator.getCurrentTime() + " at '" + this.itsOwnContact.getOverlayID() + "'.");
			return;
		}
		if(this.itsIsFirstTime) {
			new Thread(this.itsChokingOperation).start();
			new Thread(this.itsKeepAliveOperation).start();
			this.itsIsFirstTime = false;
			this.itsStatistic.startUpload();
		}
		
		if (this.downloadFinished()) {
			this.scheduleOperationTimeout(this.calcDurationOfUploadAfterDownloadFinished());
		}
		
		try{
                    Thread.sleep(this.itsPeriod);
                }catch(Exception e){
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
		
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
			if (aCanceledRequest.getCreationTime() + 15 * 60000 < System.currentTimeMillis())
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
			if (System.currentTimeMillis() > (aArrivelTime + theirRequestTimeout)) {
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
				//log.error("Removed a impossible request: " + aRequest);
				this.itsRequests.removeFirst();
				this.itsRequestEvents.removeFirst();
				this.itsRequestArrivels.removeFirst();
				continue;
			}
			
                        int apiece = aRequest.getPieceNumber();
                        int ablock = aRequest.getBlockNumber();
                        int req_size = aRequest.getChunkLength();
                        int real_size = this.itsDocument.getNumberOfBytesInBlock(aRequest.getPieceNumber(),aRequest.getBlockNumber());
                        int ans_size = Math.min(req_size, real_size);
			BTPeerMessagePiece answer = new BTPeerMessagePiece(apiece, ablock,ans_size,this.itsDocument.getRawBytes(apiece,ablock,ans_size), this.itsDocument.getKey(), true, this.itsOwnContact.getOverlayID(), aRequest.getRequestingPeer().getOverlayID());
//			this.itsConnectionManager.getConnection(aRequest.getRequestingPeer()).addMessage(answer);
                        try{
                            this.itsTransLayer.sendReply(answer,this.itsDocument.getKey(), aMessageEvent, this.itsOwnContact.getTransInfo().getPort(), BTPeerMessagePiece.getStaticTransportProtocol());
			}catch(Exception e){
                            System.out.println(e.getMessage());
                            e.printStackTrace();
                        }
			this.itsRequests.removeFirst();
			this.itsRequestEvents.removeFirst();
			this.itsRequestArrivels.removeFirst();
			this.itsStatistic.blockSendToPeer(aRequest.getRequestingPeer());
		}
                
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
			//log.warn("Received an request for an piece that I don't have: '" + theRequest.getPieceNumber() + "'");
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
		this.itsRequestArrivels.addLast(System.currentTimeMillis());
	}
	
	/**
	 * If a requests gets canceled, this method stores this information until the requests gets processed and withdrawn.
	 * @param thePieceNumber the number of the piece in which the data block is located.
	 * @param theBlockNumber the number of the data block.
	 * @param theOtherPeer the peer that canceled the request.
	 */
	public void handleCancel(int thePieceNumber, int theBlockNumber, int theChunkNumber,BTContact theOtherPeer) {
		this.itsCanceledRequests.add(new BTInternRequest(theOtherPeer, this.itsOwnContact, this.itsDocument.getKey(), thePieceNumber, theBlockNumber,theChunkNumber));
	}

}
