package uc3m.netcom.overlay.bt.operation;

import java.util.Collection;
import java.util.Date;

//import org.apache.log4j.Logger;

//import de.tud.kom.p2psim.api.common.Message;
import uc3m.netcom.common.OperationCallback;
import uc3m.netcom.common.DHTNode;
//import de.tud.kom.p2psim.api.overlay.OverlayID;
//import de.tud.kom.p2psim.api.transport.TransInfo;
//import de.tud.kom.p2psim.api.transport.TransLayer;
//import de.tud.kom.p2psim.api.transport.TransMessageCallback;
//import de.tud.kom.p2psim.impl.simengine.Simulator;
//import de.tud.kom.p2psim.impl.util.logging.SimLogger;
import uc3m.netcom.overlay.bt.BTID;
import uc3m.netcom.transport.TransInfo;
import uc3m.netcom.transport.TransLayer;
import uc3m.netcom.transport.TransMessageCallback;
import uc3m.netcom.overlay.bt.BTConstants;
import uc3m.netcom.overlay.bt.BTContact;
import uc3m.netcom.overlay.bt.BTDataStore;
import uc3m.netcom.overlay.bt.BTTorrent;
import uc3m.netcom.overlay.bt.BTPeerDistributeNode;
import uc3m.netcom.overlay.bt.message.BTMessage;
import uc3m.netcom.overlay.bt.message.BTPeerToTrackerRequest;
import uc3m.netcom.overlay.bt.message.BTTrackerToPeerReply;

/**
 * This operation sends periodically statistic data to the tracker.
 * It reactivates itself every <code>this.itsPeriod</code> timesteps.
 * Use the method <code>stop</code> to stop it.
 * It is also responsible for requesting more peers from the tracker.
 * @param <OwnerType> the class of the component that owns this operation
 * @author Jan Stolzenburg
 *
 */
public class BTOperationSendStatistic<OwnerType extends DHTNode> extends BTOperation<OwnerType, Void> implements TransMessageCallback, Runnable {
	
	
	
	private BTTorrent itsTorrent;
	
	private TransLayer itsTransLayer;
	
	private BTID itsOverlayID;
	
	private int itsCommunicationID;
	
	private short itsP2PPort;
	
	private long itsLastRequest;
	
	private BTDataStore itsDataBus;
	
	private static long theirCallPeriod = BTConstants.PEER_SEND_STATISTIC_OPERATION_CALL_PERIOD;
	
	private static long theirRequestPeriod = BTConstants.PEER_SEND_STATISTIC_OPERATION_REQUEST_PERIOD;
	
	private static long theirMinimumNumberOfContacts = BTConstants.PEER_MIN_NEIGHBOURS;
	
	private static long theirMessageTimeout = BTConstants.PEER_TO_TRACKER_MESSAGE_TIMEOUT;
	
//	static final Logger log = SimLogger.getLogger(BTOperationSendStatistic.class);
	
	
	
	public BTOperationSendStatistic(BTDataStore theDataBus, short theP2PPort, BTTorrent theTorrent, BTID theOverlayID, OwnerType theOwningComponent, OperationCallback<Void> theOperationCallback) {
		super(theOwningComponent, theOperationCallback);
		this.itsDataBus = theDataBus;
		this.itsP2PPort = theP2PPort;
		this.itsTorrent = theTorrent;
		this.itsTransLayer = ((BTPeerDistributeNode)theOwningComponent).getTransLayer();
		this.itsOverlayID = theOverlayID;
		this.itsLastRequest = System.currentTimeMillis();
	}
	
        @Override
        protected void execute(){
            this.run();
        }
        
	
	public void run() {
            
            while(!this.isFinished()){
		if (this.isFinished()) {
                        String announceURL = "http://"+this.itsTorrent.getTrackerAddress().getNetId()+":"+this.itsTorrent.getTrackerAddress().getPort()+"/";
			BTPeerToTrackerRequest request = new BTPeerToTrackerRequest(BTPeerToTrackerRequest.Reason.stopped, announceURL,this.itsTorrent.getTrackerID(),this.itsTorrent.getKey(),new BTContact(this.itsOverlayID, this.itsTransLayer.getLocalTransInfo(this.itsP2PPort)),(short)-1,-1,null);
			this.itsTransLayer.send(request, this.itsTorrent.getTrackerAddress(), this.itsP2PPort, BTPeerToTrackerRequest.getStaticTransportProtocol());
			return;
		}
		if (this.uploadFinished()) {
			
                        String announceURL = "http://"+this.itsTorrent.getTrackerAddress().getNetId()+":"+this.itsP2PPort+"/";
			BTPeerToTrackerRequest request = new BTPeerToTrackerRequest(BTPeerToTrackerRequest.Reason.stopped, announceURL,this.itsTorrent.getTrackerID(),this.itsTorrent.getKey(),new BTContact(this.itsOverlayID, this.itsTransLayer.getLocalTransInfo(this.itsP2PPort)),(short)-1,-1,null);
			this.itsTransLayer.send(request, this.itsTorrent.getTrackerAddress(), this.itsP2PPort, BTPeerToTrackerRequest.getStaticTransportProtocol());
			this.operationFinished(true);
                        return;
		}
		
		if (this.requestMorePeers()) {
//			log.debug("Too less contacts: '" + ((Map<OverlayKey, Map<String, Object>>)this.itsDataBus.get("Torrents")).get(this.itsTorrent.getKey()).get("NumberOfConnections") + "'; requesting more! At: " + this.itsOverlayID);
                        String announceURL = "http://"+this.itsTorrent.getTrackerAddress().getNetId()+":"+this.itsP2PPort+"/";
			BTPeerToTrackerRequest request = new BTPeerToTrackerRequest(BTPeerToTrackerRequest.Reason.empty, announceURL,this.itsTorrent.getTrackerID(),this.itsTorrent.getKey(),new BTContact(this.itsOverlayID, this.itsTransLayer.getLocalTransInfo(this.itsP2PPort)),(short)-1,-1,null);
			this.itsCommunicationID = this.itsTransLayer.sendAndWait(request, this.itsTorrent.getTrackerAddress(), this.itsP2PPort, BTPeerToTrackerRequest.getStaticTransportProtocol(), this, theirMessageTimeout);
		}
		else if ((this.itsLastRequest + (theirRequestPeriod * 0.95)) <= System.currentTimeMillis()) { //Is it (nearly) time for the next message to the tracker?
                        String announceURL = "http://"+this.itsTorrent.getTrackerAddress().getNetId()+":"+this.itsP2PPort+"/";
			BTPeerToTrackerRequest request = new BTPeerToTrackerRequest(BTPeerToTrackerRequest.Reason.empty, announceURL,this.itsTorrent.getTrackerID(),this.itsTorrent.getKey(),new BTContact(this.itsOverlayID, this.itsTransLayer.getLocalTransInfo(this.itsP2PPort)),(short)-1,-1,null);
			this.itsTransLayer.send(request, this.itsTorrent.getTrackerAddress(), this.itsP2PPort, BTPeerToTrackerRequest.getStaticTransportProtocol());
		}

		if (this.itsOverlayID.toString().equals("0")) { //TODO: Tells the user, what time it is in the simulation.
//			log.info("Simulated Time: " + (System.currentTimeMillis() / (1000*60)) + " Minutes; Real time: " + (new Date()).toString() + ";");
		}
                
                try{
                    Thread.sleep(BTOperationSendStatistic.theirCallPeriod);
                    }catch(InterruptedException e){
                        System.out.println(e.getMessage());
                        e.printStackTrace();
                    }
		
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
//		log.warn("Sending statistic data timed out.");
	}
	
	public void messageTimeoutOccured(int commId) {
		//Nothing to do.
	}
	
	public void receive(BTMessage theMessage, TransInfo theSenderInfo, int theCommunicationID) {
		assert (this.itsCommunicationID == theCommunicationID);
		assert (this.itsTorrent.getTrackerAddress().equals(theSenderInfo));
		if (!(theMessage instanceof BTTrackerToPeerReply)) {
//			log.warn("Expected a 'BTTrackerToPeerReply', but got a '" + theMessage.getClass().getSimpleName() + "'!");
			return;
		}
		Collection<BTContact> theNewPeers = ((BTTrackerToPeerReply)theMessage).getNewPeerSet();
		for (BTContact anOtherPeer : theNewPeers) {
			this.itsDataBus.storePeer(this.itsTorrent.getKey(), anOtherPeer);
		}
	}
	
}
