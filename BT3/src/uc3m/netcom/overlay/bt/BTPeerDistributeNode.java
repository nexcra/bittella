package uc3m.netcom.overlay.bt;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.random.RandomGenerator;
//import org.apache.log4j.Logger;

//import de.tud.kom.p2psim.api.common.ConnectivityEvent;
//import de.tud.kom.p2psim.api.common.Message;
import uc3m.netcom.common.Operation;
import uc3m.netcom.common.OperationCallback;
//import uc3m.netcom.overlay.bt.BTID;
import uc3m.netcom.common.ContentStorage;

import uc3m.netcom.common.AbstractOverlayNode;
import uc3m.netcom.common.DistributionStrategy;
//import de.tud.kom.p2psim.impl.simengine.Simulator;
//import de.tud.kom.p2psim.impl.util.logging.SimLogger;
import uc3m.netcom.transport.TransInfo;
import uc3m.netcom.transport.TransLayer;
import uc3m.netcom.transport.TransMessageListener;
import uc3m.netcom.transport.TransMsgEvent;
import uc3m.netcom.overlay.bt.manager.BTConnectionManager;
import uc3m.netcom.overlay.bt.message.BTMessage;
import uc3m.netcom.overlay.bt.message.BTPeerMessageBitField;
import uc3m.netcom.overlay.bt.message.BTPeerMessageCancel;
import uc3m.netcom.overlay.bt.message.BTPeerMessageChoke;
import uc3m.netcom.overlay.bt.message.BTPeerMessageHandshake;
import uc3m.netcom.overlay.bt.message.BTPeerMessageHave;
import uc3m.netcom.overlay.bt.message.BTPeerMessageInterested;
import uc3m.netcom.overlay.bt.message.BTPeerMessageKeepAlive;
import uc3m.netcom.overlay.bt.message.BTPeerMessageRequest;
import uc3m.netcom.overlay.bt.message.BTPeerMessageUnchoke;
import uc3m.netcom.overlay.bt.message.BTPeerMessageUninterested;
import uc3m.netcom.overlay.bt.operation.BTOperationDownload;
import uc3m.netcom.overlay.bt.operation.BTOperationUpload;

/**
 * This class is responsible for the communication with other peers.
 * It has methods to start a download, upload and it receives most types of the messages from other peers.
 * @author Jan Stolzenburg
 */
public class BTPeerDistributeNode extends AbstractOverlayNode implements TransMessageListener, DistributionStrategy {
	
	
	
	/**
	 * It stores all documents we know.
	 */
	private ContentStorage itsContentStorage; //TODO: Das bereits im Konstruktor setzen? Bisher wird es von der Application-Klasse gesetzt.
	
	/**
	 * This class makes the handling of operations easier.
	 */
//	private BTOperationManager itsOperationManager;
	
	/**
	 * My P2P address.
	 */
	private BTContact itsOwnContact;
	
	/**
	 * This class is our interface to the network. We use it for sending and receiving messages.
	 */
	private Map<String, BTConnectionManager> itsConnectionManagers;
	
	/**
	 * Here we store the message handler for every torrent.
	 * The message handler handles most types of messages that we receive from other peers.
	 */
	private Map<String, BTMessageHandler> itsCurrentlyUploadedDocuments; //TODO: Beim beenden wieder hieraus entfernen.
	
	/**
	 * The upload operation for every torrent.
	 */
	private Collection<BTOperationUpload<BTPeerDistributeNode>> itsUploadOperations;
	
	/**
	 * The download operation for every torrent.
	 */
	private Collection<BTOperationDownload<BTPeerDistributeNode>> itsDownloadOperations;
	
	/**
	 * The statistic class stores, at which time we received how much data from every peer.
	 */
	private BTInternStatistic itsStatistic;
	
	/**
	 * A random source.
	 */
	private RandomGenerator itsRandomGenerator;
	
	/**
	 * The universal data store. We store different kind of information in it.
	 */
	private BTDataStore itsDataBus;
	
//	static final Logger log = SimLogger.getLogger(BTPeerDistributeNode.class);
	//private UC3MLogBT logger = GNP_BTSimulation_Periodical.logger;
	private TransLayer transLayer;

	
	public BTPeerDistributeNode(BTDataStore theDataBus, BTID theBTID, short thePeerDistributionPort, BTInternStatistic theStatistic, RandomGenerator theRandomGenerator) {
		super(theBTID, thePeerDistributionPort);
		this.itsDataBus = theDataBus;
		this.itsStatistic = theStatistic;
		this.itsRandomGenerator = theRandomGenerator;
		this.itsConnectionManagers = new HashMap<String, BTConnectionManager>();
		this.itsCurrentlyUploadedDocuments = new HashMap<String, BTMessageHandler>();
		this.itsDownloadOperations = new LinkedList<BTOperationDownload<BTPeerDistributeNode>>();
		this.itsUploadOperations = new  LinkedList<BTOperationUpload<BTPeerDistributeNode>>();
	}
	
	/**
	 * This method starts a download by creating and activating a download operation.
	 * @param theString the key/hash of the document that we want to download.
	 * @param theOtherPeers a list of peers start also participate in this torrent. We normaly get this list from the tracker.
	 * @return the operation id of the started download operation.
	 */
	public BTOperationDownload downloadDocument(String theString, List<TransInfo> theOtherPeers, OperationCallback theCallback) {
		//log.debug("Time: " + Simulator.getCurrentTime() + "; Starting download at '" + this.itsOwnContact + "'.");
		BTDocument document;
		BTConnectionManager connectionManager;
		if (! this.itsContentStorage.containsDocument(theString)) {
			document = new BTDocument(theString, ((BTTorrent) this.itsDataBus.getPerTorrentData(theString, "Torrent")).getSize());
			this.itsContentStorage.storeDocument(document);
		}
		else {
			document = (BTDocument)this.itsContentStorage.loadDocument(theString);
		}
		if (! this.itsConnectionManagers.containsKey(theString)) {
			connectionManager = new BTConnectionManager(this.itsOwnContact);
			this.itsConnectionManagers.put(theString, connectionManager);
		}
		else {
			connectionManager = this.itsConnectionManagers.get(theString);
		}
		BTOperationDownload<BTPeerDistributeNode> downloadOperation = new BTOperationDownload<BTPeerDistributeNode>(this.itsDataBus, document, this.itsOwnContact, this, this, connectionManager, this.itsStatistic, this.itsRandomGenerator);
		this.itsDownloadOperations.add(downloadOperation);
		//downloadOperation.scheduleImmediately();
		return downloadOperation;//.getOperationID();
	}
	
	/**
	 * This method starts a upload by creating and activating a download operation.
	 * @param theString the key/hash of the document that we want to upload.
	 * @return the operation id of the started upload operation.
	 */
	public BTOperationUpload uploadDocument(String theString, OperationCallback theCallback) {
		//log.debug("Starting upload at '" + this.itsOwnContact + "'.");
		BTDocument document;
		BTConnectionManager connectionManager;
		if (! this.itsContentStorage.containsDocument(theString)) {
			throw new RuntimeException("You tried to upload an unknown document.");
		}
		else {
			document = (BTDocument)this.itsContentStorage.loadDocument(theString);
		}
		if (! this.itsConnectionManagers.containsKey(theString)) {
			connectionManager = new BTConnectionManager(this.itsOwnContact);
			this.itsConnectionManagers.put(theString, connectionManager);
		}
		else {
			connectionManager = this.itsConnectionManagers.get(theString);
		}
		BTOperationUpload<BTPeerDistributeNode> uploadOperation = new BTOperationUpload<BTPeerDistributeNode>(this.itsDataBus, document, this.itsOwnContact, this, this, connectionManager, this.itsStatistic, this.itsRandomGenerator);
		this.itsUploadOperations.add(uploadOperation);
		uploadOperation.scheduleImmediately();
		if (! this.itsCurrentlyUploadedDocuments.containsKey(theString)) {
			this.itsCurrentlyUploadedDocuments.put(theString, new BTMessageHandler(this.itsDataBus, document, this.itsOwnContact, uploadOperation, this.getTransLayer(), connectionManager));
		}
		return uploadOperation;//.getOperationID();
	}
	
	public void setContentStorage(ContentStorage theContentStorage) {
		this.itsContentStorage = theContentStorage;
	}
	
	/**
	 * Most types of P2P messages in BitTorrent are no direct replies.
	 * They are all received by this method. It casts them to a subtype and
	 * forwards them to the message handler.
	 * @param theMessageEvent the message event that is received.
	 */
	public void messageArrived(TransMsgEvent theMessageEvent) {
		
		BTMessage theMessage = theMessageEvent.getPayload();
		if (! (theMessage instanceof BTMessage)) {
			String errorMessage = "Received a non-BitTorrent message: '" + theMessage.toString() + "'";
			//log.error(errorMessage);
			throw new RuntimeException(errorMessage);
		}
		
		BTMessage theBTMessage = (BTMessage) theMessage;
		BTContact theOtherPeer = new BTContact(theBTMessage.getSender(), theMessageEvent.getSenderTransInfo());
		
		//A table lookup for the message type would be better than this case statement. But this is much to much overhead in Java, as we don't have first class functions.
		//TODO: Aufr�umen, so dass man nicht jedes mal das gleiche macht. Am besten nur nachschauen von wem die Nachricht ist, nachschlagen in welchem Torrent dieser Peer ist und fertig.
		switch (theBTMessage.getType()) {
			case BTMessage.REQUEST: {
				BTPeerMessageRequest theRequest = (BTPeerMessageRequest) theBTMessage;
				String theDocument = theRequest.getRequest().getOverlayKey();
				if (! this.itsCurrentlyUploadedDocuments.containsKey(theDocument)) {
					//We just ignore this message.
					//log.warn("Received an unexpected request from '" + theOtherPeer + "' for '" + theDocument + "'!");
					return;
				}
				if (this.itsConnectionManagers.get(theDocument).getConnection(theOtherPeer) != null)
					this.itsConnectionManagers.get(theDocument).getConnection(theOtherPeer).receivedMessage(theRequest);
				this.itsCurrentlyUploadedDocuments.get(theDocument).handleRequestMessage(theRequest, theOtherPeer, theMessageEvent);
				return;
			}
			case BTMessage.CANCEL: {
				BTPeerMessageCancel theCancelMessage = (BTPeerMessageCancel) theBTMessage;
				String theDocument = theCancelMessage.getOverlayKey();
				if (! this.itsCurrentlyUploadedDocuments.containsKey(theCancelMessage.getOverlayKey())) {
					//We just ignore this message.
					//log.warn("Received an unexpected cancel message from '" + theOtherPeer + "' for '" + theCancelMessage.getString() + "'!");
					return;
				}
				if (this.itsConnectionManagers.get(theDocument).getConnection(theOtherPeer) != null)
					this.itsConnectionManagers.get(theDocument).getConnection(theOtherPeer).receivedMessage(theCancelMessage);
				this.itsCurrentlyUploadedDocuments.get(theCancelMessage.getOverlayKey()).handleCancelMessage(theCancelMessage, theOtherPeer);
				return;
			}
			case BTMessage.HAVE: {
				BTPeerMessageHave theHaveMessage = (BTPeerMessageHave) theBTMessage;
				String theDocument = theHaveMessage.getOverlayKey();
				if (! this.itsCurrentlyUploadedDocuments.containsKey(theHaveMessage.getOverlayKey())) {
					//We just ignore this message.
					//log.warn("Received an unexpected have message from '" + theOtherPeer + "' for '" + theHaveMessage.getString() + "'!");
					return;
				}
				if (this.itsConnectionManagers.get(theDocument).getConnection(theOtherPeer) != null)
					this.itsConnectionManagers.get(theDocument).getConnection(theOtherPeer).receivedMessage(theHaveMessage);
				this.itsCurrentlyUploadedDocuments.get(theHaveMessage.getOverlayKey()).handleHaveMessage(theHaveMessage, theOtherPeer);
				return;
			}
			case BTMessage.HANDSHAKE: {
				BTPeerMessageHandshake theHandshake = (BTPeerMessageHandshake) theBTMessage;
				String theDocument = theHandshake.getOverlayKey();
				if (! this.itsCurrentlyUploadedDocuments.containsKey(theHandshake.getOverlayKey())) {
					//We just ignore this message.
					//log.debug("Received an unexpected handshake at '" + this.itsOwnContact.getBTID() + "' from '" + theOtherPeer.getBTID() + "' for '" + theHandshake.getString() + "'!");
					return;
				}
				if (this.itsConnectionManagers.get(theDocument).getConnection(theOtherPeer) != null)
					this.itsConnectionManagers.get(theDocument).getConnection(theOtherPeer).receivedMessage(theHandshake);
				this.itsCurrentlyUploadedDocuments.get(theHandshake.getOverlayKey()).handleHandshakeMessage(theHandshake, theOtherPeer);
				return;
			}
			case BTMessage.BITFIELD: {
				BTPeerMessageBitField theBitfield = (BTPeerMessageBitField) theBTMessage;
				String theDocument = theBitfield.getOverlayKey();
				if (! this.itsCurrentlyUploadedDocuments.containsKey(theBitfield.getOverlayKey())) {
					//We just ignore this message.
					//log.warn("Received an unexpected bitfield from '" + theOtherPeer + "' for '" + theBitfield.getString() + "'!");
					return;
				}
				if (this.itsConnectionManagers.get(theDocument).getConnection(theOtherPeer) != null)
					this.itsConnectionManagers.get(theDocument).getConnection(theOtherPeer).receivedMessage(theBitfield);
				this.itsCurrentlyUploadedDocuments.get(theBitfield.getOverlayKey()).handleBitfieldMessage(theBitfield, theOtherPeer);
				return;
			}
			case BTMessage.INTERESTED: {
				BTPeerMessageInterested theInterestMessage = (BTPeerMessageInterested) theBTMessage;
				String theDocument = theInterestMessage.getOverlayKey();
				if (! this.itsCurrentlyUploadedDocuments.containsKey(theInterestMessage.getOverlayKey())) {
					//We just ignore this message.
					//log.warn("Received an unexpected interest message from '" + theOtherPeer + "' for '" + theInterestMessage.getString() + "'!");
					return;
				}
				if (this.itsConnectionManagers.get(theDocument).getConnection(theOtherPeer) != null)
					this.itsConnectionManagers.get(theDocument).getConnection(theOtherPeer).receivedMessage(theInterestMessage);
				this.itsCurrentlyUploadedDocuments.get(theInterestMessage.getOverlayKey()).handleInterestMessage(theInterestMessage, theOtherPeer);
				return;
			}
			case BTMessage.UNINTERESTED: {
				BTPeerMessageUninterested theUninterestMessage = (BTPeerMessageUninterested) theBTMessage;
				String theDocument = theUninterestMessage.getOverlayKey();
				if (! this.itsCurrentlyUploadedDocuments.containsKey(theUninterestMessage.getOverlayKey())) {
					//We just ignore this message.
					//log.warn("Received an unexpected uninterest message from '" + theOtherPeer + "' for '" + theUninterestMessage.getOverlayKey() + "'!");
					return;
				}
				if (this.itsConnectionManagers.get(theDocument).getConnection(theOtherPeer) != null)
					this.itsConnectionManagers.get(theDocument).getConnection(theOtherPeer).receivedMessage(theUninterestMessage);
				this.itsCurrentlyUploadedDocuments.get(theUninterestMessage.getOverlayKey()).handleUninterestMessage(theUninterestMessage, theOtherPeer);
				return;
			}
			case BTMessage.CHOKE: {
				BTPeerMessageChoke theChokeMessage = (BTPeerMessageChoke) theBTMessage;
				String theDocument = theChokeMessage.getOverlayKey();
				if (! this.itsCurrentlyUploadedDocuments.containsKey(theChokeMessage.getOverlayKey())) {
					//We just ignore this message.
					//log.warn("Received an unexpected choke message from '" + theOtherPeer + "' for '" + theChokeMessage.getOverlayKey() + "'!");
					return;
				}
				if (this.itsConnectionManagers.get(theDocument).getConnection(theOtherPeer) != null)
					this.itsConnectionManagers.get(theDocument).getConnection(theOtherPeer).receivedMessage(theChokeMessage);
				this.itsCurrentlyUploadedDocuments.get(theChokeMessage.getOverlayKey()).handleChokeMessage(theChokeMessage, theOtherPeer);
				return;
			}
			case BTMessage.UNCHOKE: {
				BTPeerMessageUnchoke theUnchokeMessage = (BTPeerMessageUnchoke) theBTMessage;
				String theDocument = theUnchokeMessage.getString();
				if (! this.itsCurrentlyUploadedDocuments.containsKey(theUnchokeMessage.getString())) {
					//We just ignore this message.
					//log.warn("Received an unexpected unchoke message from '" + theOtherPeer + "' for '" + theUnchokeMessage.getString() + "'!");
					return;
				}
				if (this.itsConnectionManagers.get(theDocument).getConnection(theOtherPeer) != null)
					this.itsConnectionManagers.get(theDocument).getConnection(theOtherPeer).receivedMessage(theUnchokeMessage);
				this.itsCurrentlyUploadedDocuments.get(theUnchokeMessage.getString()).handleUnchokeMessage(theUnchokeMessage, theOtherPeer);
				return;
			}
			case BTMessage.KEEPALIVE: {
				BTPeerMessageKeepAlive theKeepAliveMessage = (BTPeerMessageKeepAlive) theBTMessage;
				String theDocument = theKeepAliveMessage.getString();
				if (! this.itsCurrentlyUploadedDocuments.containsKey(theKeepAliveMessage.getString())) {
					//We just ignore this message.
					//log.warn("Received an unexpected keep alive message from '" + theOtherPeer + "' for '" + theKeepAliveMessage.getString() + "'!");
					return;
				}
				if (this.itsConnectionManagers.get(theDocument).getConnection(theOtherPeer) != null)
					this.itsConnectionManagers.get(theDocument).getConnection(theOtherPeer).receivedMessage(theKeepAliveMessage);
				this.itsCurrentlyUploadedDocuments.get(theKeepAliveMessage.getString()).handleKeepAliveMessage(theOtherPeer);
				return;
			}
			case BTMessage.PIECE: {
//				BTPeerMessagePiece thePieceMessage = (BTPeerMessagePiece) theBTMessage;
				/*
				 * I wasn't able to find the reason for these failures:
				 * It seams that the requests themselves get sometimes delayed and arrive much to late.
				 * But this failure accures also, if the requests are not delayed.
				 * There are not nearly-late piece messages: Either they arrive within some seconds or they are too late. (> 180 sec.)
				 * But nothing in between...
				 */
//				log.warn("Received an piece-message that has timed out! It needed '" + ((BTUtil.getTime() - thePieceMessage.getTimestamp()) / BTUtil.getSecond()) + "' seconds till the answer of the request arrived.");
//				log.warn("Received an piece-message that has timed out! Size: '" + thePieceMessage.getMessageSize() + "'; it needed '" + ((BTUtil.getTime() - thePieceMessage.getTimestamp()) / BTUtil.getSecond()) + "' seconds; it needed '" + ((BTUtil.getTime() - thePieceMessage.getRequestTime()) / BTUtil.getSecond()) + "' seconds till the answer of the request arrived. Piece: '" + thePieceMessage.getPieceNumber() + "'; Block: '" + thePieceMessage.getBlockNumber() + "';");
				return;
			}
			default: {
				String errorMessage = "Received an unknown BitTorrent message: '" + theBTMessage.toString() + "'";
				//log.error(errorMessage);
				throw new RuntimeException(errorMessage);
			}
		}
	}
	
	@Override
	public TransLayer getTransLayer() {
		return this.transLayer;
	}

	public TransInfo getTransInfo() {
		return this.itsOwnContact.getTransInfo();
	}

        public void setTransLayer(TransLayer tl){
            this.transLayer = tl;
        }
        
	public void connect(ContentStorage cs) {
		//this.getTransLayer().addTransMsgListener(this, this.getPort());
		this.itsContentStorage = cs;
		this.itsOwnContact = new BTContact(this.getOverlayID(), this.getTransLayer().getLocalTransInfo(this.getPort()));
	}

	public Operation createOperation(String opName, String[] params, OperationCallback caller) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Method 'createOperation' in class 'BTPeerDistributeNode' not yet implemented!");
		//return null;
	}

        /*
	public void connectivityChanged(ConnectivityEvent ce) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Method 'connectivityChanged' in class 'BTPeerDistributeNode' not yet implemented!");
		//
	}
*/
                @Override
	public void calledOperationFailed(Operation op) {
            if(op instanceof BTOperationDownload){
                //logger.process(this.getClass().toString(),new Object[]{op,new Long(Simulator.getCurrentTime()),new Boolean(false)});
            }
	}

        @Override
	public void calledOperationSucceeded(Operation opd) {
            if(opd instanceof BTOperationDownload){
                BTOperationDownload op = (BTOperationDownload) opd;
                //logger.process(this.getClass().toString(),new Object[]{op,new Long(op.getFinishedTime()),new Boolean(true)});
            }
	}
        
}
