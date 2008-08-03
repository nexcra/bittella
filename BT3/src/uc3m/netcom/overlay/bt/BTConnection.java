package uc3m.netcom.overlay.bt;

import java.util.Collection;
import java.util.LinkedList;

//import org.apache.log4j.Logger;

//import de.tud.kom.p2psim.impl.simengine.Simulator;
//import de.tud.kom.p2psim.impl.util.logging.SimLogger;
import uc3m.netcom.overlay.bt.message.BTMessage;

/**
 * This class stores the knowledge about the connection between two peers.
 * It stores far more than just if they are connected or not.
 * In particular it stores the BitTorrent specific details like choking state, interest and keep alive state.
 * @author Jan Stolzenburg
 */
public class BTConnection {
	
	public enum State {DISCONNECTED, CONNECTED, HANDSHAKING}
	
	private BTContact itsOtherSideContact;
	
	private BTContact itsOwnContact;
	
	private BTConnection itsOtherSideConnection;
	
	private State itsState;
	
	/**
	 * Is this contact interesting for me?
	 */
	private boolean itsInteresting;
	
	/**
	 * Is this contact interested in me?
	 */
	private boolean itsInterested;
	
	/**
	 * Am I choking this contact?
	 */
	private boolean itsChoking;
	
	/**
	 * Am I being choked by this contact?
	 */
	private boolean itsChoked;
	
	private long itsLastUnchoking;
	
	private long itsHandshakingTime;
	
	private long itsLastKeepAlive;
	
	/**
	 * This messages have been send, but have not been received.
	 * TODO: If a message times out, remove it!
	 */
	private Collection<BTMessage> itsMessageQueue;
	
	private static long theirHandshakingTimeFalseValue = Long.MAX_VALUE;
	
	//static final Logger log = SimLogger.getLogger(BTConnection.class);
	
	
	
	public BTConnection(BTContact thisSide, BTContact theOtherSide) {
		this.itsOwnContact = thisSide;
		this.itsOtherSideContact = theOtherSide;
		this.itsState = State.DISCONNECTED; //every connection starts disconnected.
		this.itsHandshakingTime = theirHandshakingTimeFalseValue;
		this.itsInterested = false;
		this.itsInteresting = false;
		this.itsChoking = true;
		this.itsChoked = true;
		this.itsMessageQueue = new LinkedList<BTMessage>();
		this.itsLastUnchoking = Long.MIN_VALUE;
		this.itsLastKeepAlive = System.currentTimeMillis();
	}
	
	/**
	 * @return the contact data for the other side of this connection.
	 */
	public BTContact getOtherSide() {
		return this.itsOtherSideContact;
	}
	
	/**
	 * @return the connection object of the peer at the other side. It's  version of "this".
	 */
	public BTConnection getOtherSideConnection() {
		return this.itsOtherSideConnection;
	}
	
	/**
	 * Sets the connection of the peer at the other side of this connection.
	 * @param theOtherSide the connection of the peer at the other side.
	 */
	public void registerOtherSideConnection(BTConnection theOtherSide) {
		if (theOtherSide == null) {
			throw new RuntimeException("Tried to register 'null' as a counter-connection.");
		}
		if (! this.itsOwnContact.equals(theOtherSide.itsOtherSideContact)) {
			throw new RuntimeException("Tried to register false connections as counter-connection.");
		}
		if (! this.itsOtherSideContact.equals(theOtherSide.itsOwnContact)) {
			throw new RuntimeException("Tried to register false connections as counter-connection.");
		}
		this.registerOtherSideConnectionIntern(theOtherSide);
		theOtherSide.registerOtherSideConnectionIntern(this);
	}
	
	
	private void registerOtherSideConnectionIntern(BTConnection theOtherSide) {
		this.itsOtherSideConnection = theOtherSide;
	}
	
	/**
	 * Tell this class, that the connection has been closed.
	 */
	public void closeConnection() {
		if (this.itsOtherSideConnection != null) {
			this.itsOtherSideConnection.disconnectIntern();
		}
		this.disconnectIntern();
	}
	
	private void disconnectIntern() {
		this.itsState = State.DISCONNECTED;
		this.itsHandshakingTime = theirHandshakingTimeFalseValue;
	}
	
	/**
	 * Tell this class, that the connection is in handshaking state.
	 */
	public void handshaking() {
		this.itsState = State.HANDSHAKING;
		this.itsHandshakingTime = System.currentTimeMillis();
	}
	
	/**
	 * Tell this class, that the connection has been established.
	 */
	public void connected() {
		this.itsState = State.CONNECTED;
		this.itsHandshakingTime = theirHandshakingTimeFalseValue;
	}
	
	/**
	 * @return is this connection established?
	 */
	public boolean isConnected() {
		return (this.itsState == State.CONNECTED);
	}
	
	/**
	 * @return Am I interested in the offered data of the other peer.
	 */
	public boolean isInterestingForMe() {
		return this.itsInteresting;
	}
	
	/**
	 * @return Is the other peer interested in my data?
	 */
	public boolean isInterestedInMe() {
		return this.itsInterested;
	}
	
	/**
	 * sets whether I am interested in the data of the other peer.
	 * @param interesting The interest state.
	 */
	public void setInteresting(boolean interesting) {
		this.itsInteresting = interesting;
	}
	
	/**
	 * Sets whether the other peer is interested in my data.
	 * @param interested
	 */
	public void setInterested(boolean interested) {
		this.itsInterested = interested;
	}
	
	/**
	 * @return Am I choking the other peer?
	 */
	public boolean amIChoking() {
		return this.itsChoking;
	}
	
	/**
	 * @return Am I beeing choked by the other peer.
	 */
	public boolean amIChoked() {
		return this.itsChoked;
	}
	
	/**
	 * Sets whether I am choking the other peer.
	 * @param theNewChokingState the choking state.
	 */
	public void setChoking(boolean theNewChokingState) {
		if (this.itsChoking && ! theNewChokingState) //If it gets unchoked...
			this.itsLastUnchoking = System.currentTimeMillis();
		this.itsChoking = theNewChokingState;
	}
	
	/**
	 * Sets whether the other peer is choking me.
	 * @param theNewChokingState the choking state.
	 */
	public void setChoked(boolean theNewChokingState) {
		this.itsChoked = theNewChokingState;
	}
	
	/**
	 * When has the other peer been unchoked?
	 * @return the time of the last unchoke.
	 */
	public long getLastUnchoking() {
		return this.itsLastUnchoking;
	}
	/**
	 * @return is this peer in handshaking state?
	 */
	public boolean isHandshaking() {
		return (this.itsState == State.HANDSHAKING);
	}
	
	/**
	 * @return since when is the connection in handshaking state?
	 */
	public long isHandshakingSince() {
		return this.itsHandshakingTime;
	}
	
	/**
	 * @return the value of <code>isHandshakingSince</code> that indicates, that this connection is NOT handshaking.
	 */
	public static long getHandshakingTimeFalseValue() {
		return theirHandshakingTimeFalseValue;
	}
	
	/**
	 * @return When has the last keep alive like message been received?
	 */
	public long getTimeOfLastKeepAlive() {
		return this.itsLastKeepAlive;
	}
	
	/**
	 * Call this method, whenever you receive a message,
	 * that is equivalent to a keep alive.
	 * For example, if you receive a request or an 'interest' message,
	 * you can be sure that the the other wants this connection to stay open.
	 * The same, if he sends an handshake and you accept it.
	 */
	public void keepAliveReceived() {
		this.itsLastKeepAlive = System.currentTimeMillis();
	}
	
	@Deprecated
	public void addMessage(BTMessage theMessage) { //Is not usable, at the moment.
		this.itsMessageQueue.add(theMessage);
	}
	
	/**
	 * Call this method if you receive a message from the other peer.
	 * This message can be used for for different purposes:
	 * Keeping track of the messages still on the wire.
	 * Keeping track of all messages received from a peer (for debugging).
	 * @param theMessage the message you received.
	 */
	public void receivedMessage(BTMessage theMessage) {
//		log.debug("ID: " + this.itsOwnContact.getOverlayID() + "; Message: " + theMessage);
		if (this.itsOtherSideConnection != null) {
                        System.out.println("Initiating Message Removed");
			this.itsOtherSideConnection.receiveMessageIntern(theMessage);
		}
	}
	
	private void receiveMessageIntern(BTMessage theMessage) {
		this.itsMessageQueue.remove(theMessage);
	}
	
	@Deprecated
	public void removeMessage(BTMessage theMessage) {
		this.itsMessageQueue.remove(theMessage);
	}
	
	@Deprecated
	public int getNumberOfMessagesInQueue() {
		return this.itsMessageQueue.size();
	}
	
	@Deprecated
	public long getNumberOfBytesInQueue() {
		long result = 0;
		for (BTMessage aMessage : this.itsMessageQueue)
			result += aMessage.getSize();
		return result;
	}
	
	@Override
	public boolean equals(Object theOther) {
		if (! (theOther instanceof BTConnection))
			return false;
		BTConnection aOther = (BTConnection) theOther;
		return this.getOtherSide().equals(aOther.getOtherSide());
	}
	
	@Override
	public int hashCode() {
		return this.itsOtherSideContact.hashCode(); //This makes sure, changing the connection state doesn't change the hash code.
	}
	
}
