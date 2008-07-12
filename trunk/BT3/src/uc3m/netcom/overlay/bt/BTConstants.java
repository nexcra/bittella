package uc3m.netcom.overlay.bt;

import uc3m.netcom.transport.TransProtocol;
import de.tud.kom.p2psim.impl.simengine.Simulator;

/**
 * This class encapsulates all constants of this BitTorrent implementation.
 * This makes it very easy to change the major parameters.
 * @author Jan Stolzenburg
 */
public class BTConstants {
	
	/**
	 * The amount of peers, a tracker sends to a client.
	 */
	public final static short TRACKER_REPLY_AMOUNT_OF_PEERS = 50;
	
	/**
	 * The maximum number of neighours that a peer accepts.
	 * The mainline client (version 3) refuses more than 55.
	 */
	public final static short PEER_MAX_NEIGHBOURS = 55;
	
	/**
	 * If a client has less neighours, it actively tries to get more.
	 */
	public final static short PEER_MIN_NEIGHBOURS = 30;
	
	/**
	 * This value determines the number of bytes per block:
	 * 2 ^ itsBlockExponent = number of bytes per block.
	 */
	public final static byte DOCUMENT_DEFAULT_BLOCK_EXPONENT = 14; //14 is the BitTorrent default.
	
	/**
	 * This value determines the number of bytes per piece.
	 * 2 ^ itsPieceExponent = number of bytes per piece.
	 */
	public final static byte DOCUMENT_DEFAULT_PIECE_EXPONENT = 19; //19 is the BitTorrent default.
	
	/**
	 * The timeout for the messages send to the tracker.
	 */
	public final static long PEER_TO_TRACKER_MESSAGE_TIMEOUT = Simulator.MINUTE_UNIT;
	
	/**
	 * How often should it be retried, if it fails? (Contacting the tracker)
	 */
	public final static byte PEER_TO_TRACKER_MAX_MESSAGE_RETRY = 3;
	
	/**
	 * The overall timeout for the contacting the tracker..
	 */
	public final static long PEER_TO_TRACKER_CONTACT_TIMEOUT = 5 * Simulator.MINUTE_UNIT;
	
	/**
	 * How often gets the upload operation called?
	 */
	public final static long PEER_UPLOAD_OPERATION_PERIOD = Math.max(Simulator.SECOND_UNIT / 5, 1); //If the simulator has intervals smaller than a second, we use this. Otherwise, we take the minimum: once per second.
	
	/**
	 * How often does a peer send statistic data?
	 */
	public final static long PEER_SEND_STATISTIC_OPERATION_REQUEST_PERIOD = 30 * Simulator.MINUTE_UNIT;
	
	/**
	 * In what intervals can a peer request further peer sets from the tracker?
	 */
	public final static long PEER_SEND_STATISTIC_OPERATION_CALL_PERIOD = 5 * Simulator.MINUTE_UNIT;
	
	/**
	 * How often gets the download operation called?
	 */
	public final static long PEER_DOWNLOAD_OPERATION_PERIOD = Math.max(Simulator.SECOND_UNIT / 5, 1); //If the simulator has intervals smaller than a second, we use this. Otherwise, we take the minimum: once per second.
	
	/**
	 * Timeout for a handshake to other peers.
	 */
	public final static long PEER_HANDSHAKE_TIMEOUT = Simulator.MINUTE_UNIT;
	
	/**
	 * Timeout for a request to other peers.
	 */
	public final static long PEER_REQUEST_TIMEOUT = 45 * Simulator.SECOND_UNIT;
	
	/**
	 * If a peer cannot reply to an request from another peer in this time, it will discard the request.
	 */
	public final static long PEER_REPLY_TIMEOUT = 10 * Simulator.SECOND_UNIT;
	
	/**
	 * How long should a peer wait between connection attempts?
	 * A very low value will result in a flood of requests!
	 */
	public final static long PEER_CONNECT_RETRY = 5 * Simulator.MINUTE_UNIT;
	
	/**
	 * How often gets the keep alive operation called?
	 */
	public final static long PEER_KEEP_ALIVE_OPERATION_PERIOD = 2 * Simulator.MINUTE_UNIT;
	
	/**
	 * If a peer doesn't receive a keep alive message for this time, it will close the connection.
	 */
	public final static long PEER_CONNECTION_TIMEOUT = 5 * Simulator.MINUTE_UNIT;
	
	/**
	 * How often should the regular unchoke recalculation take place?
	 */
	public final static long CHOKING_REGULAR_CHOKING_RECALC_PERIOD = 10 * Simulator.SECOND_UNIT;
        
        /**
	 * How often should the algorithm to calculate the number of Unchoked Peers take place?
	 */
	public final static long PEER_NUM_SELECTION_RECALC_PERIOD = 10 * Simulator.SECOND_UNIT;
	
	/**
	 * How often should the optimistic unchoke recalculation take place?
	 */
	public final static long CHOKING_OPTIMISTIC_CHOKING_RECALC_PERIOD = 30 * Simulator.SECOND_UNIT;
	
	/**
	 * How many peers should be unchoked as regular unchokes?
	 */
     public final static byte CHOKING_NUMBER_OF_REGULAR_UNCHOKES = 3;
	
	/**
	 * How many peers should be unchoked as optimistic unchokes?
	 */
	public final static byte CHOKING_NUMBER_OF_OPTIMISTIC_UNCHOKES = 1 ;
	
	/**
	 * How many requests should be send to a peer at the same time?
	 * TODO: The simulator cannot handle more than one. Otherwise, the bandwith will be exceeded massivly!
	 */
	public final static byte DOWNLOAD_QUEUE_REQUEST_SIZE = 1; //The simulator can't handle it correctly, if this number is greater than 1.
	
	/**
	 * How long should a peer continue to upload, if its download has finished?
	 */
	public final static long DURATION_OF_UPLOAD_AFTER_DOWNLOAD_FINISHED = 0; //2 * 24 * Simulator.HOUR_UNIT;
	
	/**
	 * The message categories for the messages.
	 * BitTorrent uses TCP for the P2P data transfer
	 * and UDP for contacting the tracker.
	 */
	
	public final static TransProtocol MESSAGE_SERVICE_CATEGORY_TRACKER_TO_PEER = BTUtil.UDP;
	
	public final static TransProtocol MESSAGE_SERVICE_CATEGORY_PEER_TO_TRACKER = BTUtil.UDP;
	
	public final static TransProtocol MESSAGE_SERVICE_CATEGORY_UNINTERESTED = BTUtil.TCP;
	
	public final static TransProtocol MESSAGE_SERVICE_CATEGORY_UNCHOKE = BTUtil.TCP;
	
	public final static TransProtocol MESSAGE_SERVICE_CATEGORY_REQUEST = BTUtil.TCP;
	
	public final static TransProtocol MESSAGE_SERVICE_CATEGORY_PIECE = BTUtil.TCP;
	
	public final static TransProtocol MESSAGE_SERVICE_CATEGORY_CANCEL = BTUtil.TCP;
	
	public final static TransProtocol MESSAGE_SERVICE_CATEGORY_INTERESTED = BTUtil.TCP;
	
	public final static TransProtocol MESSAGE_SERVICE_CATEGORY_HAVE = BTUtil.TCP;
	
	public final static TransProtocol MESSAGE_SERVICE_CATEGORY_HANDSHAKE = BTUtil.TCP;
	
	public final static TransProtocol MESSAGE_SERVICE_CATEGORY_CHOKE = BTUtil.TCP;
	
	public final static TransProtocol MESSAGE_SERVICE_CATEGORY_BITFIELD = BTUtil.TCP;
	
	public final static TransProtocol MESSAGE_SERVICE_CATEGORY_KEEP_ALIVE = BTUtil.TCP;
	
}
