package uc3m.netcom.overlay.bt.operation;

//import org.apache.log4j.Logger;

import uc3m.netcom.common.OperationCallback;
import uc3m.netcom.common.DistributionStrategy;
import uc3m.netcom.transport.TransLayer;
//import de.tud.kom.p2psim.impl.simengine.Simulator;
//import de.tud.kom.p2psim.impl.util.logging.SimLogger;
import uc3m.netcom.overlay.bt.BTConstants;
import uc3m.netcom.overlay.bt.BTContact;
import uc3m.netcom.overlay.bt.BTDataStore;
import uc3m.netcom.overlay.bt.BTDocument;
import uc3m.netcom.overlay.bt.BTPeerDistributeNode;
import uc3m.netcom.overlay.bt.manager.BTConnectionManager;
import uc3m.netcom.overlay.bt.message.BTPeerMessageKeepAlive;

/**
 * This operation sends keep alive messages and
 * closes unneccessary connections.
 * @param <OwnerType> the class of the component that owns this operation
 * @author Jan Stolzenburg
 */
public class BTOperationKeepAlive<OwnerType extends DistributionStrategy> extends BTOperation<OwnerType, Void> implements Runnable {
	
	private BTDocument itsDocument;
	
	private TransLayer itsTransLayer;
	
	private BTContact itsOwnContact;
	
	private BTConnectionManager itsConnectionManager;
	
	private BTDataStore itsDataBus;
	
	private int itsMaximumNumberOfContacts = BTConstants.PEER_MAX_NEIGHBOURS;
	
	private long itsPeriod = BTConstants.PEER_KEEP_ALIVE_OPERATION_PERIOD;
	
	private long itsConnectionTimeout = BTConstants.PEER_CONNECTION_TIMEOUT;
	
//	static final Logger log = SimLogger.getLogger(BTOperationKeepAlive.class);
	
	
	
	public BTOperationKeepAlive(BTDataStore theDataBus, BTDocument theDocument, BTContact theOwnContact, OwnerType theOwningComponent, OperationCallback<Void> theOperationCallback, BTConnectionManager theConnectionManager) {
		super(theOwningComponent, theOperationCallback);
		this.itsDataBus = theDataBus;
		this.itsDocument = theDocument;
		this.itsTransLayer = ((BTPeerDistributeNode)theOwningComponent).getTransLayer();
		this.itsOwnContact = theOwnContact;
		this.itsConnectionManager = theConnectionManager;
	}
	
	@Override
	protected void execute() {
        }
        
    public void run() {

        while (!this.isFinished()) {


            if (this.uploadFinished()) {
                this.operationFinished(true);
            } else if (this.isFinished()) {
                //We stopped uploading. This only happens after stopping the download.
                //This means, we don't need any connects. We go offline!
                for (BTContact anOtherPeer : this.itsConnectionManager.getConnectedContacts()) {
                    this.itsConnectionManager.getConnection(anOtherPeer).closeConnection();
                }
            } else {

                //Send 'keep alive' messages to interesting peers:
                for (BTContact anOtherPeer : this.itsConnectionManager.getConnectedContacts()) {
                    if (this.itsConnectionManager.getConnection(anOtherPeer).isInterestingForMe()) {
                        BTPeerMessageKeepAlive keepAlive = new BTPeerMessageKeepAlive(this.itsDocument.getKey(), this.itsOwnContact.getOverlayID(), anOtherPeer.getOverlayID());
//				this.itsConnectionManager.getConnection(anOtherPeer).addMessage(keepAlive);
                        try {
                            this.itsTransLayer.send(keepAlive, this.itsDocument.getKey(), anOtherPeer.getTransInfo(), this.itsOwnContact.getTransInfo().getPort(), BTPeerMessageKeepAlive.getStaticTransportProtocol());
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }

                if (this.itsDataBus.getListOfPeersForTorrent(this.itsDocument.getKey()).size() >= this.itsMaximumNumberOfContacts) { //If we can have connections to all known peers at the same time, we don't need to close any connections: There is no new peer that could be contacted, after some connections had been closed.
                    //Close unneccessary connections:
                    for (BTContact anOtherPeer : this.itsConnectionManager.getConnectedContacts()) {
                        if (this.itsConnectionManager.getConnection(anOtherPeer).isInterestingForMe()) {
                            continue;
                        } //It would be stupid, to close a connection that is interesting for me.
                        if ((this.itsConnectionManager.getConnection(anOtherPeer).getTimeOfLastKeepAlive() + this.itsConnectionTimeout) < System.currentTimeMillis()) {
                            this.itsConnectionManager.getConnection(anOtherPeer).closeConnection();
                        }
                    }
                }

                try {
                    Thread.sleep(this.itsPeriod);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
	
	private boolean uploadFinished() {
		if (! this.itsDataBus.isPerTorrentDataStored(this.itsDocument.getKey(), "Upload Stopped"))
			return false;
		return (Boolean) this.itsDataBus.getPerTorrentData(this.itsDocument.getKey(), "Upload Stopped");
	}
	
	@Override
	public Void getResult() {
		return null;
	}
	
	@Override
	protected void operationTimeoutOccured() {
		this.operationFinished(true); //The timeout of the upload operation is the normal way to stop it. Therefore, success.
	}
	
}
