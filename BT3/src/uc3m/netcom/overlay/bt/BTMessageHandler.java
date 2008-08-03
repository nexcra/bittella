package uc3m.netcom.overlay.bt;

import java.util.BitSet;

//import org.apache.log4j.Logger;

//import de.tud.kom.p2psim.api.common.Component;
import uc3m.netcom.transport.TransLayer;
import uc3m.netcom.transport.TransMsgEvent;
//import de.tud.kom.p2psim.impl.util.logging.SimLogger;
import uc3m.netcom.overlay.bt.algorithm.BTAlgorithmInterested;
import uc3m.netcom.overlay.bt.manager.BTConnectionManager;
import uc3m.netcom.overlay.bt.message.BTMessage;
import uc3m.netcom.overlay.bt.message.BTPeerMessageBitField;
import uc3m.netcom.overlay.bt.message.BTPeerMessageCancel;
import uc3m.netcom.overlay.bt.message.BTPeerMessageChoke;
import uc3m.netcom.overlay.bt.message.BTPeerMessageHandshake;
import uc3m.netcom.overlay.bt.message.BTPeerMessageHave;
import uc3m.netcom.overlay.bt.message.BTPeerMessageInterested;
import uc3m.netcom.overlay.bt.message.BTPeerMessageRequest;
import uc3m.netcom.overlay.bt.message.BTPeerMessageUnchoke;
import uc3m.netcom.overlay.bt.message.BTPeerMessageUninterested;
import uc3m.netcom.overlay.bt.operation.BTOperationUpload;

/**
 * This class handles most types of messages a peer can receive from other peers.
 * @author Jan Stolzenburg
 */
public class BTMessageHandler {
	
	private BTDocument itsDocument;
	
	private BTContact itsOwnContact;
	
	@SuppressWarnings("unchecked")
	private BTOperationUpload itsUploadOperation;
	
	private TransLayer itsTransLayer;
	
	private BTConnectionManager itsConnectionManager;
	
	private BTAlgorithmInterested itsInterestAlgorithm;
	
	private BTDataStore itsDataBus;
	
	//static final Logger log = SimLogger.getLogger(BTMessageHandler.class);
	
	
	
	@SuppressWarnings("unchecked")
	public BTMessageHandler(BTDataStore theDataBus, BTDocument theDocument, BTContact theOwnContact, BTOperationUpload theUploadOperation, TransLayer transLayer, BTConnectionManager theConnectionManager) {
		this.itsDataBus = theDataBus;
		this.itsDocument = theDocument;
		this.itsOwnContact = theOwnContact;
		this.itsUploadOperation = theUploadOperation;
		this.itsTransLayer = transLayer;
		this.itsConnectionManager = theConnectionManager;
		this.itsInterestAlgorithm = new BTAlgorithmInterested();
		this.itsInterestAlgorithm.setup(this.itsDocument);
	}
	
	
	
	private boolean uploadFinished() {
		if (! this.itsDataBus.isPerTorrentDataStored(this.itsDocument.getKey(), "Upload Stopped"))
			return false;
		return (Boolean) this.itsDataBus.getPerTorrentData(this.itsDocument.getKey(), "Upload Stopped");
	}
	
	public void handleHaveMessage(BTPeerMessageHave theHaveMessage, BTContact theOtherPeer) {
		if ((! this.itsConnectionManager.isConnectionRegisteredTo(theOtherPeer)) || ! this.itsConnectionManager.getConnection(theOtherPeer).isConnected()) {
			//We just ignore this odd message.
			//log.debug("Received an unexpected have from '" + theOtherPeer + "' for '" + theHaveMessage.getOverlayKey() + "'!");
			return;
		}
		((BitSet) this.itsDataBus.getPerPeerData(theOtherPeer, "BitSet")).set(theHaveMessage.getPieceNumber());
		
		//Calculate interest:
		if (this.itsConnectionManager.getConnection(theOtherPeer).isInterestingForMe())
			return; //We can only gain and not loose interest, if an other peer has finished a piece.
		boolean newInterest = this.itsInterestAlgorithm.computeInterest((BitSet) this.itsDataBus.getPerPeerData(theOtherPeer, "BitSet"));
		if (! newInterest)
			return;
		BTMessage interestMessage = new BTPeerMessageInterested(this.itsDocument.getKey(), this.itsOwnContact.getOverlayID(), theOtherPeer.getOverlayID());
//		this.itsConnectionManager.getConnection(theOtherPeer).addMessage(interestMessage);
                try{
                    this.itsTransLayer.send(interestMessage,this.itsDocument.getKey() ,theOtherPeer.getTransInfo(), this.itsOwnContact.getTransInfo().getPort(), BTPeerMessageInterested.getStaticTransportProtocol());
                }catch(Exception e){
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
		this.itsConnectionManager.getConnection(theOtherPeer).setInteresting(true);
	}
	
	public void handleInterestMessage(BTPeerMessageInterested theInterestMessage, BTContact theOtherPeer) {
		if ((! this.itsConnectionManager.isConnectionRegisteredTo(theOtherPeer)) || ! this.itsConnectionManager.getConnection(theOtherPeer).isConnected()) {
			//We just ignore this odd message.
			//log.debug("Received an unexpected interest message from '" + theOtherPeer + "' for '" + theInterestMessage.getOverlayKey() + "'!");
			return;
		}
		this.itsConnectionManager.getConnection(theOtherPeer).setInterested(true);
		this.itsUploadOperation.recalcChoking();
		this.itsConnectionManager.getConnection(theOtherPeer).keepAliveReceived();
	}
	
	public void handleUninterestMessage(BTPeerMessageUninterested theUninterestMessage, BTContact theOtherPeer) {
		if ((! this.itsConnectionManager.isConnectionRegisteredTo(theOtherPeer)) || ! this.itsConnectionManager.getConnection(theOtherPeer).isConnected()) {
			//We just ignore this odd message.
			//log.debug("Received an unexpected uninterest message from '" + theOtherPeer + "' for '" + theUninterestMessage.getOverlayKey() + "'!");
			return;
		}
		this.itsConnectionManager.getConnection(theOtherPeer).setInterested(false);
		this.itsUploadOperation.recalcChoking();
	}
	
	public void handleChokeMessage(BTPeerMessageChoke theChokeMessage, BTContact theOtherPeer) {
		if ((! this.itsConnectionManager.isConnectionRegisteredTo(theOtherPeer)) || ! this.itsConnectionManager.getConnection(theOtherPeer).isConnected()) {
			//We just ignore this odd message.
			//log.debug("Received an unexpected choke message from '" + theOtherPeer + "' for '" + theChokeMessage.getOverlayKey() + "'!");
			return;
		}
		this.itsConnectionManager.getConnection(theOtherPeer).setChoked(true);
	}
	
	public void handleUnchokeMessage(BTPeerMessageUnchoke theUnchokeMessage, BTContact theOtherPeer) {
		if ((! this.itsConnectionManager.isConnectionRegisteredTo(theOtherPeer)) || ! this.itsConnectionManager.getConnection(theOtherPeer).isConnected()) {
			//We just ignore this odd message. It can happen, if I contacted him and he immediately unchokes me, before I received his handshake.
			//log.debug("Received an unexpected unchoke message from '" + theOtherPeer + "' for '" + theUnchokeMessage.getOverlayKey() + "'!");
			return;
		}
		this.itsConnectionManager.getConnection(theOtherPeer).setChoked(false);
	}
	
	public void handleKeepAliveMessage(BTContact theOtherPeer) {
		if ((! this.itsConnectionManager.isConnectionRegisteredTo(theOtherPeer)) || ! this.itsConnectionManager.getConnection(theOtherPeer).isConnected()) {
//			log.debug("Received an unexpected keep alive message at " + this.itsOwnContact.getOverlayID() + " from ??? '" + theOtherPeer.getOverlayID() + "' for '" + theUnchokeMessage.getOverlayKey() + "'!");
		}
		if (this.uploadFinished()) {
			//If the upload is stopped, we accept nothing.
			this.itsConnectionManager.getConnection(theOtherPeer).closeConnection();
			return;
		}
		this.itsConnectionManager.getConnection(theOtherPeer).keepAliveReceived();
	}
	
	public void handleHandshakeMessage(BTPeerMessageHandshake theHandshakeMessage, BTContact theOtherPeer) {
            System.out.println("MessageHandler processing handshake");
		if (this.itsConnectionManager.isConnectionRegisteredTo(theOtherPeer) && this.itsConnectionManager.getConnection(theOtherPeer).isConnected()) {
			//We just ignore this odd message.
			//log.debug("Received an unexpected handshake at '" + this.itsOwnContact + "' from '" + theOtherPeer + "' for '" + theHandshakeMessage.getOverlayKey() + "'!");
                        System.out.println("Connection already handshaked");
			return;
		}
                System.out.println(theOtherPeer.getOverlayID().toString()+" "+theOtherPeer.getTransInfo().getNetId()+":"+theOtherPeer.getTransInfo().getPort());
		if (! this.itsConnectionManager.isConnectionRegisteredTo(theOtherPeer)){
			this.itsConnectionManager.addConnection(theOtherPeer);
                        System.out.println("Connection was not created");
                        }
		BTConnection connection = this.itsConnectionManager.getConnection(theOtherPeer);
		connection.registerOtherSideConnection(theHandshakeMessage.getSenderConnection()); //Now the two connection sides can inform each other when they get disconnected.
		
		if (this.uploadFinished()) {
			//If the upload is stopped, we accept nothing.
			connection.closeConnection();
			return;
		}
		System.out.println("AKI");
		if (! connection.isHandshaking()) { //In diesem Fall, geht die Initiative vom Anderen aus, und wir m�ssen noch unser Handshake schicken.
                        System.out.println("Connection was not handshaking");
			this.itsDataBus.storePeer(this.itsDocument.getKey(), theOtherPeer);
			if (BTConstants.PEER_MAX_NEIGHBOURS <= this.itsConnectionManager.getNumberOfConnectedContacts()) {
				//We reject it, as we already have more than enough neighours.
				connection.closeConnection();
				return;
			}
			BTPeerMessageHandshake handshake = new BTPeerMessageHandshake(this.itsDocument.getKey(), connection, this.itsOwnContact.getOverlayID(), theOtherPeer.getOverlayID());
//			connection.addMessage(handshake);
                        System.out.println("Response to incoming handshake from ");
                        try{
                            this.itsTransLayer.send(handshake, this.itsDocument.getKey(),theOtherPeer.getTransInfo(), this.itsOwnContact.getTransInfo().getPort(), BTPeerMessageHandshake.getStaticTransportProtocol()); //We don't use "sendReply", as this would cause the handshake message to return to the DownloadOperation, and not to the DistributionNode.
                        }catch(Exception e){
                            System.out.println(e.getMessage());
                            e.printStackTrace();
                        }    
		}
		connection.connected(); //I received its handshake and checked it. For me, this connection is okay.
		if (! this.itsDataBus.isPerPeerDataStored(theOtherPeer, "BitSet"))
			//TODO
			this.itsDataBus.storePerPeerData(theOtherPeer, "BitSet", BTBitSetUtil.getEmptyBitset(this.itsDocument.getNumberOfPieces()), (new BitSet()).getClass());
		if (this.itsDocument.getState() != BTDocument.State.EMPTY) {
			BTPeerMessageBitField bitfield = new BTPeerMessageBitField(this.itsDocument.getFinishedPieces(), this.itsDocument.getKey(), this.itsOwnContact.getOverlayID(), theOtherPeer.getOverlayID());
//			connection.addMessage(bitfield);
                        System.out.println("Sending Bitfield to the Other side");
                        try{
                            this.itsTransLayer.send(bitfield, this.itsDocument.getKey(),theOtherPeer.getTransInfo(), this.itsOwnContact.getTransInfo().getPort(), BTPeerMessageBitField.getStaticTransportProtocol()); //look some lines above
                        }catch(Exception e){
                            System.out.println(e.getMessage());
                            e.printStackTrace();
                        }
		}
		this.itsConnectionManager.getConnection(theOtherPeer).keepAliveReceived();
	}
	
	public void handleBitfieldMessage(BTPeerMessageBitField theBitfieldMessage, BTContact theOtherPeer) {
		if (! this.itsConnectionManager.isConnectionRegisteredTo(theOtherPeer)) {
			//log.debug("Received an unexpected bitfield at '" + this.itsOwnContact + "' from '" + theOtherPeer + "' for '" + theBitfieldMessage.getOverlayKey() + "'!");
			return;
		}
		if (this.itsDocument.getFinishedPieces().size() != theBitfieldMessage.getBitset().size()) { //Checking against the number of pieces is impossible, as BitSets don't have any method for getting the size correctly.
			//log.debug("Received an wrong-sized bitfield from '" + theOtherPeer + "' for '" + theBitfieldMessage.getOverlayKey() + "'!");
			return;
		}
		this.itsDataBus.storePerPeerData(theOtherPeer, "BitSet", theBitfieldMessage.getBitset(), (new BitSet()).getClass());
		boolean newInterest = this.itsInterestAlgorithm.computeInterest((BitSet) this.itsDataBus.getPerPeerData(theOtherPeer, "BitSet"));
		if (! newInterest) {
			this.itsConnectionManager.getConnection(theOtherPeer).setInteresting(false);
			return;
		}
		BTMessage interestMessage = new BTPeerMessageInterested(this.itsDocument.getKey(), this.itsOwnContact.getOverlayID(), theOtherPeer.getOverlayID());
//		this.itsConnectionManager.getConnection(theOtherPeer).addMessage(interestMessage);
                try{
                    this.itsTransLayer.send(interestMessage, this.itsDocument.getKey(), theOtherPeer.getTransInfo(), this.itsOwnContact.getTransInfo().getPort(), BTPeerMessageInterested.getStaticTransportProtocol());
                }catch(Exception e){
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
		this.itsConnectionManager.getConnection(theOtherPeer).setInteresting(true);
	}
	
	public void handleRequestMessage(BTPeerMessageRequest theRequestMessage, BTContact theOtherPeer, TransMsgEvent theMessageEvent) {
		if ((! this.itsConnectionManager.isConnectionRegisteredTo(theOtherPeer)) || ! this.itsConnectionManager.getConnection(theOtherPeer).isConnected()) {
			//We just ignore this odd message.
			//log.debug("Received an unexpected request from '" + theOtherPeer + "' for '" + theRequestMessage.getRequest().getOverlayKey() + "'!");
			return;
		}
		if (this.uploadFinished()) {
			//If the upload is stopped, we accept nothing.
			this.itsConnectionManager.getConnection(theOtherPeer).closeConnection();
			return;
		}
		this.itsUploadOperation.handleRequest(theRequestMessage.getRequest(), theMessageEvent);
		this.itsConnectionManager.getConnection(theOtherPeer).keepAliveReceived();
	}
	
	public void handleCancelMessage(BTPeerMessageCancel theCancelMessage, BTContact theOtherPeer) {
		if ((! this.itsConnectionManager.isConnectionRegisteredTo(theOtherPeer)) || ! this.itsConnectionManager.getConnection(theOtherPeer).isConnected()) {
			//We just ignore this odd message.
			//log.debug("Received an unexpected cancel from '" + theOtherPeer + "' for '" + theCancelMessage.getOverlayKey() + "'!");
			return;
		}
		this.itsUploadOperation.handleCancel(theCancelMessage.getPieceNumber(), theCancelMessage.getBlockNumber(), theCancelMessage.getChunkSize(),theOtherPeer);
	}
	
}
