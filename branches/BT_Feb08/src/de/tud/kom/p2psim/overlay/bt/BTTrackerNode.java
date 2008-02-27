package de.tud.kom.p2psim.overlay.bt;

import java.util.Collection;

import org.apache.commons.math.random.RandomGenerator;
import org.apache.log4j.Logger;

import de.tud.kom.p2psim.api.common.ConnectivityEvent;
import de.tud.kom.p2psim.api.common.Message;
import de.tud.kom.p2psim.api.common.Operation;
import de.tud.kom.p2psim.api.common.OperationCallback;
import de.tud.kom.p2psim.api.overlay.OverlayID;
import de.tud.kom.p2psim.api.overlay.OverlayKey;
import de.tud.kom.p2psim.api.transport.TransLayer;
import de.tud.kom.p2psim.api.transport.TransMessageListener;
import de.tud.kom.p2psim.api.transport.TransMsgEvent;
import de.tud.kom.p2psim.impl.overlay.AbstractOverlayNode;
import de.tud.kom.p2psim.impl.util.logging.SimLogger;
import de.tud.kom.p2psim.overlay.bt.algorithm.BTAlgorithmTrackerPeerSelection;
import de.tud.kom.p2psim.overlay.bt.message.BTPeerToTrackerRequest;
import de.tud.kom.p2psim.overlay.bt.message.BTTrackerToPeerReply;

/**
 * This class handles the trackers issues with the peers.
 * It stores the peer managers for every document.
 * If one of the peers send a message, whatever may be the reason: This class handles it!
 * To add a document to the tracker (which means, it will accept requests for it),
 * just call <code>addDocument</code>. After that, the tracker is responsible for this torrent
 * and will accept requests from peers for other peers.
 * To check, if the tracker is responsible for a certain document, call <code>hasDocument</code>.
 * To list all documents, call <code>listDocuments</code>.
 * @author Jan Stolzenburg
 */
public class BTTrackerNode extends AbstractOverlayNode implements TransMessageListener {
	
	
	
	/**
	 * This class encapsulates the algorithm that is used for creating a new set of peers.
	 * For example, it should make sure, that the requesting peer is not part of this set.
	 */
	private BTAlgorithmTrackerPeerSelection itsSelectionAlgorithm;
	
	/**
	 * If we need a random source, we use this class.
	 */
	private RandomGenerator itsRandomGenerator;
	
	/**
	 * This is a big data store for every kind of information.
	 */
	private BTDataStore itsDataBus;
	
	/**
	 * The number of peers, we send to a client, if it didn't specify an amount.
	 */
	private final static short theirPeerAmount = BTConstants.TRACKER_REPLY_AMOUNT_OF_PEERS;
	
	private static Logger log = SimLogger.getLogger(BTTrackerNode.class);
	
	
	
	public BTTrackerNode(BTDataStore theDataBus, OverlayID theTrackerOverlayID, short theTrackerPort, RandomGenerator theRandomGenerator) {
		super(theTrackerOverlayID, theTrackerPort);
		this.itsDataBus = theDataBus;
		this.itsRandomGenerator = theRandomGenerator;
		this.itsSelectionAlgorithm = new BTAlgorithmTrackerPeerSelection();
		this.itsSelectionAlgorithm.setup(this.itsRandomGenerator);
	}
	
	/**
	 * Is this tracker responsible for the given document?
	 * @param theDocument Has the tracker THIS document?
	 * @return Has the tracker the document?
	 */
	public boolean hasDocument(OverlayKey theDocument) {
		return this.itsDataBus.isTorrentKnown(theDocument);
	}
	
	/**
	 * Tell the tracker, it is responsible for this document and its torrent from now on.
	 * @param theDocument The document of the torrent.
	 * @return If it is already responsible for this document, it returns <code>false</code>. Otherwise <code>true</code>.
	 */
	public boolean addDocument(OverlayKey theDocument) {
		if (this.hasDocument(theDocument))
			return false;
		this.itsDataBus.addTorrent(theDocument);
		return true;
	}
	

	/**
	 * This is the most important method of the tracker.
	 * If it receives a messages, this method gets called.
	 * If a client tells us that is leaves the network, this method removes
	 * the client from the <code>BTPeerManager</code> for that torrent.
	 * In every other case, it tells the <code>BTPeerManager</code> to make sure,
	 * this client is stored for that torrent.
	 * If a client asks for a new set of peers, this method generates a new one and sends it to the client.
	 * @param theMessageEvent the message event that is received.
	 */
	public void messageArrived(TransMsgEvent theMessageEvent) {
		
		//Check if it is an unexpected message type.
		Message theMessage = theMessageEvent.getPayload();
		if ( !(theMessage instanceof BTPeerToTrackerRequest)) {
			String errorMessage = "Received an unknown message!";
			log.error(errorMessage);
			throw new RuntimeException(errorMessage);
		}
		
		//Cast the message into a more usefull subtype, extract the P2P adress of the peer and select the responsible peer manager.
		BTPeerToTrackerRequest theRequest = (BTPeerToTrackerRequest) theMessage;
		BTContact requestingPeerP2PAddress = theRequest.getP2PAddress(); //We have to differentiate here between the address peers use for contacting the tracker, and the address they use between each other.
		if (! this.hasDocument(theRequest.getDocument())) {
			log.warn("Received an request for an unknown document: '" + theRequest.getDocument() + "'!");
			return;
		}
		
		//switch over the different reasons, why a client could contact the tracker.
                
		switch (theRequest.getReason()) {
			case STOPPED :
				this.itsDataBus.removePeer(theRequest.getDocument(), requestingPeerP2PAddress);
				break; //TODO: Statistik auslesen und speichern.
			case COMPLETED :
				this.itsDataBus.storePeer(theRequest.getDocument(), requestingPeerP2PAddress); //Just to be sure. But it should allready be known.
				break; //TODO: Statistik auslesen und speichern.
			case EMPTY : //In diesem Fall sendet der Peer nur Statistikdaten.
				this.itsDataBus.storePeer(theRequest.getDocument(), requestingPeerP2PAddress); //Just to be sure. But it should allready be known.
				break; //TODO: Statistik auslesen und speichern.
			case STARTED :
				this.itsDataBus.storePeer(theRequest.getDocument(), requestingPeerP2PAddress);
				break;
			default: {
				String errorMessage = "A message I(a tracker) received has an unknown reason: " + theRequest.getReason();
				log.error(errorMessage);
				throw new RuntimeException(errorMessage);
			}
		}
		
		//if the client requested a new set of peers, we create one and send it.
		if (theRequest.getNumberOfRequestedPeers() != 0) {
			int numberOfRequestedPeers = theRequest.getNumberOfRequestedPeers();
			if (numberOfRequestedPeers < 0) //A Number smaller than 0 means: Use the standard value.
				numberOfRequestedPeers = theirPeerAmount;
			Collection<BTContact> otherPeers = this.itsSelectionAlgorithm.computePeerSelection(this.itsDataBus.getListOfPeersForTorrent(theRequest.getDocument()), requestingPeerP2PAddress, numberOfRequestedPeers);
			BTTrackerToPeerReply reply = new BTTrackerToPeerReply(otherPeers, this.getOverlayID(), theRequest.getSender());
			this.getTransLayer().sendReply(reply, theMessageEvent, this.getPort(), BTTrackerToPeerReply.getStaticTransportProtocol());
		}
	}
	
	/**
	 * Returns all documents for which the tracker is responsible.
	 * @return The list of all the document, for whicht the tracker is responsible.
	 */
	public Collection<OverlayKey> listDocuments() {
		return this.itsDataBus.getListOfTorrents();
	}
	
	@Override
	public TransLayer getTransLayer() {
		return this.getHost().getTransLayer();
	}
	
	public void connect() {
		this.getTransLayer().addTransMsgListener(this, this.getPort());
	}

	public Operation createOperation(String opName, String[] params, OperationCallback caller) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Method 'createOperation' in class 'BTTrackerNode' not yet implemented!");
		//return null;
	}
	
	public void connectivityChanged(ConnectivityEvent ce) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Method 'connectivityChanged' in class 'BTTrackerNode' not yet implemented!");
		//
	}
	
}
