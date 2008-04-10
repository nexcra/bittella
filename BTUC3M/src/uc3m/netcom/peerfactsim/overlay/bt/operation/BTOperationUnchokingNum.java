/*
 * BTOperationUnchokingNum.java
 *
 * Created on 17 de enero de 2008, 14:14
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package uc3m.netcom.peerfactsim.overlay.bt.operation;

/**
 *
 * @author JMCamacho
 */
import java.util.Collection;

import org.apache.commons.math.random.RandomGenerator;

import de.tud.kom.p2psim.api.common.OperationCallback;
import de.tud.kom.p2psim.api.overlay.DistributionStrategy;
import de.tud.kom.p2psim.api.transport.TransLayer;
import de.tud.kom.p2psim.overlay.bt.BTConstants;
import de.tud.kom.p2psim.overlay.bt.BTPeerDistributeNode;
import de.tud.kom.p2psim.overlay.bt.BTContact;
import de.tud.kom.p2psim.overlay.bt.BTDataStore;
import de.tud.kom.p2psim.api.overlay.OverlayID;
import de.tud.kom.p2psim.overlay.bt.BTDocument;
import de.tud.kom.p2psim.overlay.bt.BTInternStatistic;
import de.tud.kom.p2psim.overlay.bt.manager.BTConnectionManager;
import de.tud.kom.p2psim.overlay.bt.operation.BTOperation;

import org.dom4j.rule.NullAction;
import org.apache.log4j.Logger;
import de.tud.kom.p2psim.overlay.bt.BTConstants;

import uc3m.netcom.peerfactsim.overlay.bt.algorithm.BTAlgorithmUploadPeerNumSelection;
import uc3m.netcom.peerfactsim.overlay.bt.algorithm.BTAlgorithmChoking;
/**
 * This class takes care about the choking algorithm.
 * It gets regularly called, calls the choking algorithm and
 * tells the peers if they got (un)choked.
 * @param <OwnerType> the class of the component that owns this operation
 * @author Jan Stolzenburg
 */
	
public class BTOperationUnchokingNum<OwnerType extends DistributionStrategy> extends BTOperation<OwnerType, Void>{
    
    
	private BTDocument itsDocument;	
	private BTInternStatistic itsStatistic;	
	private RandomGenerator itsRandomGenerator;	
	private BTAlgorithmUploadPeerNumSelection itsAlgorithm;
        private BTAlgorithmChoking itsChokingAlgorithm;
	private BTConnectionManager itsConnectionManager;	
	private TransLayer itsTransportLayer;	
        private OverlayID itsOverlayID;
	private BTDataStore itsDataBus;
        //Check this out
	private final static long theirPeriod = BTConstants.CHOKING_REGULAR_CHOKING_RECALC_PERIOD;
    
    
    /** Creates a new instance of BTOperationUnchokingNum */
    public BTOperationUnchokingNum(BTDataStore theDataBus, BTDocument theDocument, OwnerType theOwningComponent, OperationCallback<Void> theOperationCallback, BTInternStatistic theStatistic, RandomGenerator theRandomGenerator, BTConnectionManager theConnectionManager, BTAlgorithmChoking chokingAlg) {
   
    		super(theOwningComponent, theOperationCallback);
		this.itsDataBus = theDataBus;
		this.itsDocument = theDocument;
		this.itsStatistic = theStatistic;
		this.itsRandomGenerator = theRandomGenerator;
		this.itsConnectionManager = theConnectionManager;
		this.itsTransportLayer = this.getComponent().getHost().getTransLayer();
                this.itsOverlayID = this.getComponent().getHost().getOverlay((new BTPeerDistributeNode(null,null,(short)0,null,null)).getClass()).getOverlayID();
                this.itsChokingAlgorithm = chokingAlg;
		
                this.itsAlgorithm = new BTAlgorithmUploadPeerNumSelection();        
		this.itsAlgorithm.setup(this.itsStatistic, this.itsRandomGenerator,this.getComponent().getHost().getNetLayer().getMaxUploadBandwidth(),this.getComponent().getHost().getNetLayer().getMaxDownloadBandwidth());
   
    }
    
    
    public void execute() {
	if (this.isFinished())
		return;
	if (this.uploadFinished()) {
                this.itsAlgorithm.printHits();
		this.operationFinished(true);
		return;
	}
	this.scheduleWithDelay(theirPeriod);
	this.run();
     }
    
    	/**
	 * @return Has the upload finished?
	 */
	private boolean uploadFinished() {
		if (! this.itsDataBus.isPerTorrentDataStored(this.itsDocument.getKey(), "Upload Stopped"))
			return false;
		return (Boolean) this.itsDataBus.getPerTorrentData(this.itsDocument.getKey(), "Upload Stopped");
	}
	
	
        
        /*Use contacted peers list to calculate DownloadRate and UploadRate.
         *Afterwards unchoked peers number algorithm is run. It returns the
         *amount of regular unchoked peers and the amount optimistic unchoked
         *peers.
         */

	public void run() {
            
		Collection<BTContact> contactedPeers = this.itsConnectionManager.getConnectedContacts();
		int[] u = this.itsAlgorithm.establishUnchokedNum(contactedPeers);
                //System.out.println("Node ID :"+ this.itsOverlayID +"; is Special: "+ itsDataBus.isGeneralDataStored("Special")+ "; its UR is: "+u[0]); 
                this.itsChokingAlgorithm.setRegularUnchokeNumber(u[0],true);
                //this.itsChokingAlgorithm.setOptimisticUnchokeNumber(u[1],true);
                
                
                //System.out.println("Node ID :"+ this.itsOverlayID +"; is Special: "+ itsDataBus.isGeneralDataStored("Special")+ "; its UO is: "+u[1]);
	}
	
        
        @Override
        public Void getResult() {
		return null;
	}

	@Override
	protected void operationTimeoutOccured() {
		this.operationFinished(false);
	}
        
        
        @Override
        public String toString(){
            
            return "Running Unchoking Num Op";
        }
    
    
    
}
