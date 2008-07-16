package uc3m.netcom.overlay.bt;

//import org.apache.log4j.Logger;

//import de.tud.kom.p2psim.api.common.ConnectivityEvent;
import uc3m.netcom.common.Operation;
import uc3m.netcom.common.OperationCallback;
import uc3m.netcom.common.DHTNode;
import uc3m.netcom.common.DHTObject;
//import uc3m.netcom.overlay.bt.BTID;
import uc3m.netcom.transport.TransLayer;
import uc3m.netcom.common.AbstractOverlayNode;
//import de.tud.kom.p2psim.impl.util.logging.SimLogger;
import uc3m.netcom.overlay.bt.operation.BTOperationValueLookup;

/**
 * This class is responsible for the communication with the tracker.
 * @author Jan Stolzenburg
 */
public class BTPeerSearchNode extends AbstractOverlayNode implements DHTNode {
	
	
	
	private short itsP2PPort;
	
	private BTDataStore itsDataBus;
	
	//protected static Logger log = SimLogger.getLogger(BTPeerSearchNode.class);
	
	private TransLayer transLayer;
        
	
	public BTPeerSearchNode(BTDataStore theDataBus, BTID theOverlayID, short thePeerSearchPort, short theP2PPort) {
		super(theOverlayID, thePeerSearchPort);
		this.itsDataBus = theDataBus;
		this.itsP2PPort = theP2PPort;
	}
	
        public TransLayer getTransLayer(){
            return transLayer;
        }
        
        public void setTransLayer(TransLayer transLayer){
            this.transLayer = transLayer;
        }
        
	@SuppressWarnings("unchecked")
	public int nodeLookup(String theDocument, OperationCallback theCallback) {
		throw new RuntimeException("The 'nodeLookup' operation is not supported by the '" + this.getClass().getSimpleName() + "'.");
	}
	
	@SuppressWarnings("unchecked")
	public int store(String key, DHTObject obj, OperationCallback theCallback) {
		throw new RuntimeException("The 'store' operation is not supported by the '" + this.getClass().getSimpleName() + "'.");
	}
	
	/**
	 * This method asks the tracker for a peer set, telling it, that we are just starting the download.
	 * Use this method if you are starting the download and want to contact the tracker for the first time.
	 * @param theOverlayKey the key/hash of the torrent.
	 * @return the operation id of the started <code>BTOperationValueLookup<code>.
	 */
	public int valueLookup(BTTorrent torrent, OperationCallback theCallback) { //The OperationManager is set as the event handler. This class doesn't care about the events. That's up to the caller.
		
                String theOverlayKey = torrent.getKey();
                
                if (! this.itsDataBus.isTorrentKnown(theOverlayKey)) {
			this.itsDataBus.addTorrent(torrent);
		}
		BTOperationValueLookup<BTPeerSearchNode> operation = new BTOperationValueLookup<BTPeerSearchNode>(this.itsDataBus, this.getPort(), this.itsP2PPort, (BTTorrent) this.itsDataBus.getPerTorrentData(theOverlayKey, "Torrent"), this.getOverlayID(), this, theCallback);
		//operation.scheduleImmediately();
                operation.execute();
		return operation.getOperationID();
	}
	
	/**
	 * This method asks the tracker for a peer set, telling it, that we are just starting the download.
	 * Use this method if you are starting the download and want to contact the tracker for the first time.
	 * @param theTorrent the torrent file of the torrent. We need it to get the tracker address.
	 * @return the operation id of the started <code>BTOperationValueLookup<code>.
	 */
	public int valueLookup(String theTorrent, OperationCallback theCallback) {
                System.out.println("Calling the wrong method");
                return -1;
            //		if (! this.itsDataBus.isTorrentKnown(theTorrent.getKey()))
//			this.itsDataBus.addTorrent(theTorrent);
//		this.itsDataBus.storePerTorrentData(theTorrent.getKey(), "Torrent", theTorrent, theTorrent.getClass());
//		return this.valueLookup(theTorrent, theCallback);
	}
	
	
	public void connect() {
		//Nothing to do...
	}
	
	public int join(OperationCallback callback) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Method 'join' in class 'BTPeerSearchNode' not yet implemented!");
		//return 0;
	}
	
	public int leave(OperationCallback callback) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Method 'leave' in class 'BTPeerSearchNode' not yet implemented!");
		//return 0;
	}
	
	public Operation createOperation(String opName, String[] params, OperationCallback caller) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Method 'createOperation' in class 'BTPeerSearchNode' not yet implemented!");
		//return null;
	}
	
//	public void connectivityChanged(ConnectivityEvent ce) {
//		// TODO Auto-generated method stub
//		throw new RuntimeException("Method 'connectivityChanged' in class 'BTPeerSearchNode' not yet implemented!");
//		//
//	}
	
}
