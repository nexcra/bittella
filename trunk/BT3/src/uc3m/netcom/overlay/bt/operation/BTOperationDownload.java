package uc3m.netcom.overlay.bt.operation;

import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
//import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.random.RandomGenerator;
//import org.apache.log4j.Logger;

//import de.tud.kom.p2psim.api.common.Message;
import uc3m.netcom.common.OperationCallback;
import uc3m.netcom.common.DistributionStrategy;
//import de.tud.kom.p2psim.api.transport.TransInfo;
//import de.tud.kom.p2psim.api.transport.TransLayer;
//import de.tud.kom.p2psim.api.transport.TransMessageCallback;
//import de.tud.kom.p2psim.impl.simengine.Simulator;
//import de.tud.kom.p2psim.impl.util.logging.SimLogger;
import uc3m.netcom.overlay.bt.message.BTMessage;
import uc3m.netcom.transport.TransInfo;
import uc3m.netcom.transport.TransLayer;
import uc3m.netcom.transport.TransMessageCallback;
import uc3m.netcom.overlay.bt.BTConstants;
import uc3m.netcom.overlay.bt.BTContact;
import uc3m.netcom.overlay.bt.BTDataStore;
import uc3m.netcom.overlay.bt.BTDocument;
import uc3m.netcom.overlay.bt.BTInternRequest;
import uc3m.netcom.overlay.bt.BTInternStatistic;
import uc3m.netcom.overlay.bt.BTPeerDistributeNode;
import uc3m.netcom.overlay.bt.BTDocument.BTDocumentFinishedListener;
import uc3m.netcom.overlay.bt.BTDocument.BTPieceFinishedListener;
import uc3m.netcom.overlay.bt.algorithm.BTAlgorithmDownloadPeerSelection;
import uc3m.netcom.overlay.bt.algorithm.BTAlgorithmDownloadPeerSelectionJustAll;
import uc3m.netcom.overlay.bt.algorithm.BTAlgorithmDownloadSelection;
import uc3m.netcom.overlay.bt.algorithm.BTAlgorithmDownloadSelectionThreePhased;
import uc3m.netcom.overlay.bt.algorithm.BTAlgorithmInterested;
import uc3m.netcom.overlay.bt.manager.BTConnectionManager;
import uc3m.netcom.overlay.bt.message.BTMessage;
import uc3m.netcom.overlay.bt.message.BTPeerMessageCancel;
import uc3m.netcom.overlay.bt.message.BTPeerMessageHandshake;
import uc3m.netcom.overlay.bt.message.BTPeerMessageHave;
import uc3m.netcom.overlay.bt.message.BTPeerMessagePiece;
import uc3m.netcom.overlay.bt.message.BTPeerMessageRequest;
import uc3m.netcom.overlay.bt.message.BTPeerMessageUninterested;
//import uc3m.netcom.overlay.bt.message.BTMessage.Type;

/**
 * This method controls the download.
 * It gets regularly called.
 * @param <OwnerType> the class of the component that owns this operation
 * @author Jan Stolzenburg
 */
public class BTOperationDownload<OwnerType extends DistributionStrategy> extends BTOperation<OwnerType, BTDocument> implements TransMessageCallback, BTDocumentFinishedListener, BTPieceFinishedListener, Runnable {

    private BTDocument itsDocument;
    private TransLayer itsTransLayer;
    private BTContact itsOwnContact;
    /**
     * Maps from the communication ids to the requests.
     */
    private Map<Integer, BTInternRequest> itsRequests;
    private BTAlgorithmDownloadSelection itsDownloadAlgorithm;
    private BTAlgorithmDownloadPeerSelection itsDownloadPeerSelection;
    private BTAlgorithmInterested itsInterestAlgorithm;
    private long itsPeriod = BTConstants.PEER_DOWNLOAD_OPERATION_PERIOD;
    private BTConnectionManager itsConnectionManager;
    private RandomGenerator itsRandomGenerator;
    private BTInternStatistic itsStatistic;
    /**
     * This map saves the list of all peers, asked for a connection recently.
     * We should wait a bit, before we retry it.
     */
    private Map<BTContact, Long> itsRefusedConnections;
    private BTDataStore itsDataBus;
    private long itsStartTime;
    private long itsLastHandshakeTime;
    private long itsLastHandshakeTimeoutTime;
    private long itsLastRequestTimeoutTime;
    private static long theirHandshakeTimeout = BTConstants.PEER_HANDSHAKE_TIMEOUT;
    /**
     * The timeout for the send messages.
     */
    private static long theirReplyTimeout = BTConstants.PEER_REQUEST_TIMEOUT;
    private static long theirConnectRetry = BTConstants.PEER_CONNECT_RETRY;
    private static int theirNumberOfActiveNodes = 0;
    //static final Logger log = SimLogger.getLogger(BTOperationDownload.class);

    public BTOperationDownload(BTDataStore theDataBus, BTDocument theDocument, BTContact theOwnContact, OwnerType theOwningComponent, OperationCallback<BTDocument> theOperationCallback, BTConnectionManager theConnectionManager, BTInternStatistic theStatistic, RandomGenerator theRandomGenerator) {
        super(theOwningComponent, theOperationCallback);
        this.itsDataBus = theDataBus;
        this.itsDocument = theDocument;
        this.itsTransLayer = ((BTPeerDistributeNode)theOwningComponent).getTransLayer();
        this.itsOwnContact = theOwnContact;
        this.itsConnectionManager = theConnectionManager;
        this.itsRandomGenerator = theRandomGenerator;
        this.itsStatistic = theStatistic;
        this.itsStartTime = System.currentTimeMillis();
        this.itsLastHandshakeTime = System.currentTimeMillis();
        this.itsRequests = new HashMap<Integer, BTInternRequest>();
        this.itsRefusedConnections = new HashMap<BTContact, Long>();
        this.itsDownloadAlgorithm = new BTAlgorithmDownloadSelectionThreePhased();
        this.itsDownloadAlgorithm.setup(this.itsDocument, this.itsRandomGenerator);
        this.itsDownloadPeerSelection = new BTAlgorithmDownloadPeerSelectionJustAll();
        this.itsDownloadPeerSelection.setup(this.itsDocument);
        this.itsInterestAlgorithm = new BTAlgorithmInterested();
        this.itsInterestAlgorithm.setup(this.itsDocument);
        this.itsDocument.registerDocumentFinished(this);
        this.itsDocument.registerPieceFinished(this);
        this.itsStatistic.startDownload();
        this.updateDataBus();
        theirNumberOfActiveNodes += 1;
    }

    /**
     * This method gets called regularly and it initiates all actions of this operation.
     */
    @Override
    protected void execute() {
    }

    public void run() {

        while (!this.isFinished()) {
            if (this.itsDocument.getState() == BTDocument.State.COMPLETE) {
                this.operationFinished(true);
            }
            if (this.isFinished()) {
                this.itsDataBus.storePerTorrentData(this.itsDocument.getKey(), "Download Finished", true, (new Boolean(true)).getClass());
                long requiredSeconds = ((System.currentTimeMillis() - this.itsStartTime) / 60000);
                long requiredMinutes = requiredSeconds / 60;
                long requiredHours = requiredMinutes / 60;
                long requiredDays = requiredHours / 24;
                theirNumberOfActiveNodes -= 1;
                //log.info("+++Download finished at time " + System.currentTimeMillis() + " after '" + requiredDays + "' days, '" + (requiredHours % 24) + "' hours, '" + (requiredMinutes % 60) + "' minutes, '" + (requiredSeconds % 60) + "' seconds (" + requiredSeconds + ") at '" + this.itsOwnContact.getOverlayID() + "'. There are still " + theirNumberOfActiveNodes + " downloads active.");
                this.itsStatistic.stopDownload();
                return;
            }

            this.doHandshakes();
            this.doHandshakeTimeout();

            this.doRequests();
            this.doRequestTimeout();

//		log.debug("ID: " + this.itsOwnContact.getOverlayID() + "; Connections: " + this.itsConnectionManager.getNumberOfConnectedContacts() + "; Requests: " + this.itsRequests.size());

            this.updateDataBus();
            try {
                Thread.sleep(this.itsPeriod);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Update the number connected peers.
     */
    private void updateDataBus() {
        this.itsDataBus.storePerTorrentData(this.itsDocument.getKey(), "NumberOfConnections", this.itsConnectionManager.getNumberOfConnectedContacts(), (new Integer(0)).getClass());
    }

    @Override
    public BTDocument getResult() {
        return this.itsDocument;
    }

    public long getFinishedTime() {
        return ((System.currentTimeMillis() - this.itsStartTime) / (60000));
    }

    @Override
    protected void operationTimeoutOccured() {
        this.operationFinished(false);
        //log.warn("Downloading document timed out.");
    }

    public void messageTimeoutOccured(int theCommunicationId) {
        this.itsRequests.remove(theCommunicationId);
    }

    /**
     * This method receives the messages containing the data.
     * It doesn't receive and control messages.
     * It updates the statistic, the current document state and everything else that is neccessary.
     * @param theMessage the received message
     * @param theSenderAddress the sender of the message
     * @param theCommunicationID the communication id that could be used for replies.
     */
    public void receive(BTMessage theMessage, TransInfo theSenderAddress, int theCommunicationID) {
        if (!(theMessage instanceof BTMessage)) {
            String errorMessage = "Received a non-BitTorrent message: '" + theMessage.toString() + "'";
            //log.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
        BTMessage theBTMessage = (BTMessage) theMessage;
        if (theBTMessage.getType() != BTMessage.PIECE) {
            String errorMessage = "Received an unknown BitTorrent message: '" + theBTMessage.toString() + "'";
            //log.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
        BTPeerMessagePiece theAnswer = (BTPeerMessagePiece) theBTMessage;
        if (this.itsDocument.getKey().equals(theAnswer.getOverlayKey())) {
            //log.error("Received a block for an unexpected document: '" + this.itsDocument.toString() + "'!");
            return;
        }
        BTContact theOtherPeer = new BTContact(theAnswer.getSender(), theSenderAddress);
        this.itsStatistic.blockReceivedFromPeer(theOtherPeer);
        this.itsConnectionManager.getConnection(theOtherPeer).receivedMessage(theAnswer);
        if (!this.itsRequests.containsKey(theCommunicationID)) { //First "receivedMessage", then this, as we have to call "receivedMessage" in either case.
            return; //Most probably, we send an cancel message, but it came to late.
        }
        this.itsDocument.addBlock(theAnswer.getPieceNumber(), theAnswer.getBlockNumber(), theAnswer.getRawData());
        this.itsRequests.remove(theCommunicationID);
        if (this.itsDownloadAlgorithm.sendCancel()) {
            this.doCancel(theAnswer.getPieceNumber(), theAnswer.getBlockNumber());
        }
    }

    /**
     * If we send a request to multiple peers, we have to cancel the requests as soon as we receive the requested data.
     * @param thePieceNumber the number of the piece in which the requested block is located.
     * @param theBlockNumber the number of the block
     */
    private void doCancel(int thePieceNumber, int theBlockNumber) {
        Collection<Integer> requestsToCancel = new LinkedList<Integer>();
        for (Integer aRequestID : this.itsRequests.keySet()) {
            if ((this.itsRequests.get(aRequestID).getPieceNumber() == thePieceNumber) && (this.itsRequests.get(aRequestID).getBlockNumber() == theBlockNumber)) {
                requestsToCancel.add(aRequestID);
            }
        }
        for (Integer aRequestID : requestsToCancel) {
            BTContact anOtherPeer = this.itsRequests.get(aRequestID).getRequestedPeer();
            BTPeerMessageCancel cancelMessage = new BTPeerMessageCancel(this.itsRequests.get(aRequestID).getPieceNumber(), this.itsRequests.get(aRequestID).getBlockNumber(),this.itsRequests.get(aRequestID).getChunkLength(), this.itsDocument.getKey(), this.itsOwnContact.getOverlayID(), anOtherPeer.getOverlayID());
//			this.itsConnectionManager.getConnection(anOtherPeer).addMessage(cancelMessage);
            try{
                this.itsTransLayer.send(cancelMessage, this.itsDocument.getKey(),anOtherPeer.getTransInfo(), this.itsOwnContact.getTransInfo().getPort(), BTPeerMessageCancel.getStaticTransportProtocol()); //The handshake-reply should not return to the download operation.
             }catch(Exception e){
                 System.out.println(e.getMessage());
                 e.printStackTrace();
             }
            this.itsRequests.remove(aRequestID);
        }
    }

    /**
     * Try to establish new connections.
     * We do this only, if we know to less peers.
     * If a peer doesn't accept the connection, we try it again, later.
     */
    private void doHandshakes() {
        if (this.itsConnectionManager.getNumberOfConnectedContacts() >= BTConstants.PEER_MIN_NEIGHBOURS) {
            return;
        } //In this case, we already have enough connected contacts.
        if ((this.itsLastHandshakeTime + (5 * 1000)) > System.currentTimeMillis()) //TODO: Konstante
        {
            return;
        }
        this.itsLastHandshakeTime = System.currentTimeMillis();

        Collection<BTContact> interesstingPeers = new LinkedList<BTContact>();

        int counter = 0;
        //Find out, which peers we could contact:
        List<BTContact> otherPeers = new LinkedList<BTContact>(this.itsDataBus.getListOfPeersForTorrent(this.itsDocument.getKey()));
        Collections.shuffle(otherPeers); //To make sure, we don't send handshakes only to the first in the list.
        for (BTContact anOtherPeer : otherPeers) {
            if (this.itsConnectionManager.isConnectionRegisteredTo(anOtherPeer) && (this.itsConnectionManager.getConnection(anOtherPeer).isConnected() || this.itsConnectionManager.getConnection(anOtherPeer).isHandshaking())) {
                continue;
            }
            if (this.itsRefusedConnections.containsKey(anOtherPeer) && (this.itsRefusedConnections.get(anOtherPeer) > System.currentTimeMillis())) {
                continue;
            }
            interesstingPeers.add(anOtherPeer);
            counter += 1;

            //Takes care that we don't send a handshake to all the 2000 other peers at the same time:
            if (counter > BTConstants.PEER_MAX_NEIGHBOURS) //This can cause us, to request a bit too much handshakes. But some of the requests will fail, we guess.
            {
                break;
            }
        }

        this.itsDownloadPeerSelection.filterNewContacts(interesstingPeers, this.itsConnectionManager.getConnectedContacts(), this.itsRequests.values());

        //Send a handshake message to all the new peers, which the "download peer selection" found interessting enough.
        for (BTContact anOtherPeer : interesstingPeers) {
            this.itsConnectionManager.addConnection(anOtherPeer);
            BTPeerMessageHandshake handshakeMessage = new BTPeerMessageHandshake(this.itsDocument.getKey(), this.itsConnectionManager.getConnection(anOtherPeer), this.itsOwnContact.getOverlayID(), anOtherPeer.getOverlayID());
            this.itsConnectionManager.getConnection(anOtherPeer).handshaking();
            this.itsConnectionManager.getConnection(anOtherPeer).addMessage(handshakeMessage);
            System.out.println("Sending Handshake to "+anOtherPeer.getTransInfo().getNetId()+":"+anOtherPeer.getTransInfo().getPort());
            try{
                this.itsTransLayer.send(handshakeMessage, this.itsDocument.getKey(),anOtherPeer.getTransInfo(), this.itsOwnContact.getTransInfo().getPort(), BTPeerMessageHandshake.getStaticTransportProtocol()); //The handshake-reply should not return to the download operation. Therefore, we don't specify a receiver.
            }catch(Exception e){
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            this.itsRefusedConnections.put(anOtherPeer, System.currentTimeMillis() + theirConnectRetry);
        }

        //Take care, that the list gets cleaned up: after an entry timed out, we remove it.
        LinkedList<BTContact> remove = new LinkedList<BTContact>();
        for (BTContact anOtherPeer : this.itsRefusedConnections.keySet()) {
            if (this.itsRefusedConnections.get(anOtherPeer) < System.currentTimeMillis()) {
                remove.add(anOtherPeer);
            }
        }
        for (BTContact anOtherPeer : remove) {
            this.itsRefusedConnections.remove(anOtherPeer);
        }
    }

    /**
     * Take care of handshakes that have timed out.
     */
    private void doHandshakeTimeout() {
        if ((this.itsLastHandshakeTimeoutTime + (5000)) > System.currentTimeMillis()) //TODO: Konstante
        {
            return;
        }
        this.itsLastHandshakeTimeoutTime = System.currentTimeMillis();
        for (BTContact anOtherPeer : this.itsConnectionManager.getHandshakingContacts()) {
            if (System.currentTimeMillis() > (this.itsConnectionManager.getConnection(anOtherPeer).isHandshakingSince() + theirHandshakeTimeout)) {
                this.itsConnectionManager.getConnection(anOtherPeer).closeConnection();
            }
        }
    }

    /**
     * Send requests for data to other peers.
     * But take care of interest and choking states.
     */
    private void doRequests() {
        Map<BTContact, BitSet> interrestingPeers = new HashMap<BTContact, BitSet>();
        for (BTContact anOtherPeer : this.itsConnectionManager.getConnectedContacts()) {
            if (this.itsConnectionManager.getConnection(anOtherPeer).isInterestingForMe() && !this.itsConnectionManager.getConnection(anOtherPeer).amIChoked()) {
                interrestingPeers.put(anOtherPeer, (BitSet) this.itsDataBus.getPerPeerData(anOtherPeer, "BitSet"));
            }
        }
//		log.debug("ID: " + this.itsOwnContact.getOverlayID() + "; interresting peers: " + interrestingPeers.size());
        Collection<BTInternRequest> internRequests = this.itsDownloadAlgorithm.computeRequests(interrestingPeers, this.itsRequests.values());
        for (BTInternRequest anInternRequest : internRequests) {
            anInternRequest.setRequestingPeer(this.itsOwnContact);
            BTPeerMessageRequest request = new BTPeerMessageRequest(anInternRequest, this.itsOwnContact.getOverlayID(), anInternRequest.getRequestedPeer().getOverlayID());
//			this.itsConnectionManager.getConnection(anInternRequest.getRequestedPeer()).addMessage(request);
            System.out.println("Sending Request to "+anInternRequest.getRequestedPeer().getTransInfo().getNetId()+":"+anInternRequest.getRequestedPeer().getTransInfo().getPort());
            int communicationId = this.itsTransLayer.sendAndWait(request, this.itsOwnContact.getOverlayID(), anInternRequest.getRequestedPeer().getTransInfo(), this.itsOwnContact.getTransInfo().getPort(), BTPeerMessageRequest.getStaticTransportProtocol(), this, theirReplyTimeout);
            this.itsRequests.put(communicationId, anInternRequest);
        }
    }

    /**
     * Take care of requests that have timed out.
     */
    private void doRequestTimeout() {
        if ((this.itsLastRequestTimeoutTime + (theirReplyTimeout / 5)) > System.currentTimeMillis()) //TODO: Konstante
        {
            return;
        }
        this.itsLastRequestTimeoutTime = System.currentTimeMillis();
        Collection<Integer> requestsToBeRemoved = new LinkedList<Integer>();
        for (Integer aRequest : this.itsRequests.keySet()) {
            if (System.currentTimeMillis() > (this.itsRequests.get(aRequest).getCreationTime() + theirReplyTimeout + 1)) {
                requestsToBeRemoved.add(aRequest);
            }
        }
        for (Integer aRequest : requestsToBeRemoved) {
            this.itsRequests.remove(aRequest);
        }
    }

    /**
     * This method gets called (by the document) if the download finishes.
     * It then ends this operation.
     */
    public void documentFinished() {
        this.operationFinished(true); //This must not abort everything immediately. After this, there may come the last "piece finished" message that has to be working correct.
    }

    /**
     * This method gets called (by the document) if the download of a piece finishes.
     * It updates the interest state and send have messages to all connected peers.
     * @param thePieceNumber the number of the finished piece.
     */
    public void pieceFinished(int thePieceNumber) {
//		log.debug("Piece " + thePieceNumber + " finished at " + this.itsOwnContact.getOverlayID() + ".");
        if (this.itsOwnContact.getOverlayID().toString().equals("1")) { //TODO: Tells the user, how much the first peer downloaded. If we remove it, the user has no idea, how long the simulation will last.
            //log.info("Simulated Time: " + System.currentTimeMillis() + "; Real time: " + (new Date()).toString() + "; I completed circa " + ((100 * this.itsDocument.getNumberOfFinishedPieces()) / this.itsDocument.getNumberOfPieces()) + "% of the simulation. (More precise: " + ((100 * this.itsDocument.getNumberOfFinishedPieces()) / (double) this.itsDocument.getNumberOfPieces()) + "%, +- 10%)");
        }
        for (BTContact anOtherPeer : this.itsConnectionManager.getConnectedContacts()) {
            BTPeerMessageHave haveMessage = new BTPeerMessageHave(thePieceNumber, this.itsDocument.getKey(), this.itsOwnContact.getOverlayID(), anOtherPeer.getOverlayID());
//			this.itsConnectionManager.getConnection(anOtherPeer).addMessage(haveMessage);
            try{
                this.itsTransLayer.send(haveMessage, this.itsDocument.getKey(),anOtherPeer.getTransInfo(), this.itsOwnContact.getTransInfo().getPort(), BTPeerMessageRequest.getStaticTransportProtocol());
            }catch(Exception e){
                System.out.println(e.getMessage());
                e.printStackTrace();
            }                
        }
        
        for (BTContact anOtherPeer : this.itsConnectionManager.getConnectedContacts()) {
            if (!this.itsConnectionManager.getConnection(anOtherPeer).isInterestingForMe()) {
                continue;
            } //We can only loose and not gain interest, if we finish a piece.
            boolean newInterest = this.itsInterestAlgorithm.computeInterest((BitSet) this.itsDataBus.getPerPeerData(anOtherPeer, "BitSet"));
            if (newInterest) {
                continue;
            }
            BTMessage interestMessage = new BTPeerMessageUninterested(this.itsDocument.getKey(), this.itsOwnContact.getOverlayID(), anOtherPeer.getOverlayID());
//			this.itsConnectionManager.getConnection(anOtherPeer).addMessage(interestMessage);
            try{
                this.itsTransLayer.send(interestMessage,this.itsDocument.getKey() ,anOtherPeer.getTransInfo(), this.itsOwnContact.getTransInfo().getPort(), BTPeerMessageUninterested.getStaticTransportProtocol());
            }catch(Exception e){
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
            this.itsConnectionManager.getConnection(anOtherPeer).setInteresting(false);
        }
    }
}
