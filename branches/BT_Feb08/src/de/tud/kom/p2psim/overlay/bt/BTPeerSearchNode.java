package de.tud.kom.p2psim.overlay.bt;

import org.apache.log4j.Logger;

import de.tud.kom.p2psim.api.common.ConnectivityEvent;
import de.tud.kom.p2psim.api.common.Operation;
import de.tud.kom.p2psim.api.common.OperationCallback;
import de.tud.kom.p2psim.api.overlay.DHTNode;
import de.tud.kom.p2psim.api.overlay.DHTObject;
import de.tud.kom.p2psim.api.overlay.OverlayID;
import de.tud.kom.p2psim.api.overlay.OverlayKey;
import de.tud.kom.p2psim.api.transport.TransLayer;
import de.tud.kom.p2psim.impl.overlay.AbstractOverlayNode;
import de.tud.kom.p2psim.impl.util.logging.SimLogger;
import de.tud.kom.p2psim.overlay.bt.operation.BTOperationValueLookup;

/**
 * This class is responsible for the communication with the tracker.
 * @author Jan Stolzenburg
 */
public class BTPeerSearchNode extends AbstractOverlayNode implements DHTNode {
	
	
	
	private short itsP2PPort;
	
	private BTDataStore itsDataBus;
	
	protected static Logger log = SimLogger.getLogger(BTPeerSearchNode.class);
	
	
	
	public BTPeerSearchNode(BTDataStore theDataBus, OverlayID theOverlayID, short thePeerSearchPort, short theP2PPort) {
		super(theOverlayID, thePeerSearchPort);
		this.itsDataBus = theDataBus;
		this.itsP2PPort = theP2PPort;
	}
	
	@SuppressWarnings("unchecked")
	public int nodeLookup(OverlayKey theDocument, OperationCallback theCallback) {
		throw new RuntimeException("The 'nodeLookup' operation is not supported by the '" + this.getClass().getSimpleName() + "'.");
	}
	
	@SuppressWarnings("unchecked")
	public int store(OverlayKey key, DHTObject obj, OperationCallback theCallback) {
		throw new RuntimeException("The 'store' operation is not supported by the '" + this.getClass().getSimpleName() + "'.");
	}
	
	/**
	 * This method asks the tracker for a peer set, telling it, that we are just starting the download.
	 * Use this method if you are starting the download and want to contact the tracker for the first time.
	 * @param theOverlayKey the key/hash of the torrent.
	 * @return the operation id of the started <code>BTOperationValueLookup<code>.
	 */
	public int valueLookup(OverlayKey theOverlayKey, OperationCallback theCallback) { //The OperationManager is set as the event handler. This class doesn't care about the events. That's up to the caller.
		if (! this.itsDataBus.isTorrentKnown(theOverlayKey)) {
			this.itsDataBus.addTorrent(theOverlayKey);
		}
		BTOperationValueLookup<BTPeerSearchNode> operation = new BTOperationValueLookup<BTPeerSearchNode>(this.itsDataBus, this.getPort(), this.itsP2PPort, (BTTorrent) this.itsDataBus.getPerTorrentData(theOverlayKey, "Torrent"), this.getOverlayID(), this, theCallback);
		operation.scheduleImmediately();
		return operation.getOperationID();
	}
	
	/**
	 * This method asks the tracker for a peer set, telling it, that we are just starting the download.
	 * Use this method if you are starting the download and want to contact the tracker for the first time.
	 * @param theTorrent the torrent file of the torrent. We need it to get the tracker address.
	 * @return the operation id of the started <code>BTOperationValueLookup<code>.
	 */
	public int valueLookup(BTTorrent theTorrent, OperationCallback theCallback) {
		if (! this.itsDataBus.isTorrentKnown(theTorrent.getKey()))
			this.itsDataBus.addTorrent(theTorrent.getKey());
		this.itsDataBus.storePerTorrentData(theTorrent.getKey(), "Torrent", theTorrent, theTorrent.getClass());
		return this.valueLookup(theTorrent.getKey(), theCallback);
	}
	
	@Override
	public TransLayer getTransLayer() {
		return this.getHost().getTransLayer();
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
	
	public void connectivityChanged(ConnectivityEvent ce) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Method 'connectivityChanged' in class 'BTPeerSearchNode' not yet implemented!");
		//
	}
	
}
