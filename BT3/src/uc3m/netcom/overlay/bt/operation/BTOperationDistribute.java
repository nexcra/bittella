package uc3m.netcom.overlay.bt.operation;



import java.util.Collection;

import org.apache.log4j.Logger;

import uc3m.netcom.common.Application;
import uc3m.netcom.common.Operation;
import uc3m.netcom.common.OperationCallback;
//import de.tud.kom.p2psim.impl.util.logging.SimLogger;
import uc3m.netcom.overlay.bt.BTContact;
import uc3m.netcom.overlay.bt.BTDataStore;
import uc3m.netcom.overlay.bt.BTDocument;
import uc3m.netcom.overlay.bt.BTPeerDistributeNode;
import uc3m.netcom.overlay.bt.BTPeerSearchNode;
import uc3m.netcom.overlay.bt.BTTorrent;


/**
 * This method starts the upload, download and all related operations.
 * @param <OwnerType> the class of the component that owns this operation
 * @author Jan Stolzenburg
 */
public class BTOperationDistribute<OwnerType extends Application>  extends BTOperation<OwnerType, BTDocument> implements OperationCallback<BTDocument> {
	
	
	
	private BTDocument itsResult;
	
	private BTTorrent itsTorrent;
	
	private BTPeerSearchNode itsDhtNode;
	
	private BTPeerDistributeNode itsDistributionNode;
	
//	private BTOperationManager itsOperationManager;
	
	private BTOperationSendStatistic<BTPeerSearchNode> itsSendStatisticOperation;
	
	private int itsLookupOperationID;
	
	private BTDataStore itsDataBus;
	
//	static final Logger log = SimLogger.getLogger(BTOperationDistribute.class);
	
	
	
	public BTOperationDistribute(BTDataStore theDataBus, BTTorrent theTorrent, BTPeerSearchNode theDhtNode, BTPeerDistributeNode theDistributionNode, OwnerType theOwningComponent, OperationCallback<BTDocument> theOperationCallback) {
		super(theOwningComponent, theOperationCallback);
		this.itsDataBus = theDataBus;
		this.itsTorrent = theTorrent;
		this.itsDhtNode = theDhtNode;
		this.itsDistributionNode = theDistributionNode;
	}
	
	@Override
	protected void execute() {
		this.step1();
	}
        
        public void start(){
            this.step1();
        }
	
	@Override
	public BTDocument getResult() {
		return this.itsResult;
	}
	
	protected void setResult(BTDocument theResult) {
		this.itsResult = theResult;
	}
	
	@Override
	protected void operationTimeoutOccured() {
		operationFinished(false);
	}
	
	/**
	 * When the operation is executed, it calls this method that starts a lookup operation.
	 * This will return a list of peers that are currently part of this torrent.
	 */
	private void step1() {
		this.itsLookupOperationID = this.itsDhtNode.valueLookup(this.itsTorrent, this); //Starts the search in the dht. Returns the search ID. The dht will call back, when it has found the information.
//		this.itsOperationManager.registerOperationEventHandler(lookupOperationID, this); //We store the search ID. We need it, when the search has finished and calls back, telling us the result.
	}
	
	/**
	 * When the lookup operation is finished, this method gets called.
	 * It starts the download, upload and the regular send of statistic data.
	 * @param theDocumentHolder the list of peers that should have parts of this document
	 */
	@SuppressWarnings("unchecked")
	private void step2(Collection<BTContact> theDocumentHolder) {
//		log.debug("Tracker successfully contacted. I received " + theDocumentHolder.size() + " new peers.");
		
		this.itsSendStatisticOperation = new BTOperationSendStatistic(this.itsDataBus, this.itsDistributionNode.getPort(), this.itsTorrent, this.itsDhtNode.getOverlayID(), this.itsDhtNode, this);
		//this.itsSendStatisticOperation.scheduleWithDelay(BTOperationSendStatistic.getPeriod());
		Thread tsso = new Thread(this.itsSendStatisticOperation);
                tsso.start();
                
		//this.itsDistributionNode.downloadDocument(this.itsTorrent.getKey(), null, this); //I don't pass addresses of other peers, as I have the peer managers for this job!
                BTOperationDownload btdo = this.itsDistributionNode.downloadDocument(this.itsTorrent.getKey(), null, this);
                Thread tdo = new Thread(btdo);
//		int downloadOperationID = this.itsDistributionNode.downloadDocument(this.itsTorrent.getKey(), null, this); //I don't pass addresses of other peers, as I have the peer managers for this job!
//		this.itsOperationManager.registerOperationEventHandler(downloadOperationID, this);
		
		//this.itsDistributionNode.uploadDocument(this.itsTorrent.getKey(), this);
                BTOperationUpload btuo = this.itsDistributionNode.uploadDocument(this.itsTorrent.getKey(), this);
                Thread tuo = new Thread(btuo);
//		int uploadOperationID = this.itsDistributionNode.uploadDocument(this.itsTorrent.getKey(), this);
//		this.itsOperationManager.registerOperationEventHandler(uploadOperationID, this);
	}
	
	@SuppressWarnings("unchecked")
	public void calledOperationSucceeded(Operation<BTDocument> theOperation) {
		//if (this.itsLookupOperationID == theOperation.getOperationID())
			this.step2((Collection<BTContact>)theOperation.getResult());
	}
	
	public void calledOperationFailed(Operation<BTDocument> theOperation) {
		//if (this.itsLookupOperationID == theOperation.getOperationID()) {
//			log.error("Failed connecting to tracker. Aborting.");
			this.operationFinished(false);
		//}
	}
	
}
