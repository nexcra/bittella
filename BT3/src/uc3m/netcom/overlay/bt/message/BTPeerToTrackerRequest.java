package uc3m.netcom.overlay.bt.message;

import uc3m.netcom.overlay.bt.BTID;
import uc3m.netcom.overlay.bt.BTUtil;
import uc3m.netcom.overlay.bt.BTContact;
import uc3m.netcom.overlay.bt.BTInternStatistic;
import uc3m.netcom.transport.TransProtocol;

public class BTPeerToTrackerRequest extends BTMessage {
	
    
	public enum Reason {started, stopped, completed,empty}
	
	private BTInternStatistic itsStatistic; //no se si lo utilizaremos
	
	private Reason event;
	private String info_hash;
        private BTContact peer_id;
        private short port = peer_id.getTransInfo().getPort();
        private String uploaded = "0";
        private String downloaded = "0";
        private String left = "0";
        private String compact = "1";
        private String ip = peer_id.getTransInfo().getNetId();
        private int numwant = 50;
        private BTID tracker_id;
        private String announceURL;
	
	public BTPeerToTrackerRequest(Reason theReason,String announceURL, BTID tracker_id, String info_hash,BTContact peer_id,short port,int numwant, BTInternStatistic theStatistic){
            super(BTMessage.Type.TRACKER_REQUEST,BTUtil.TCP,true,0,peer_id.getOverlayID(),tracker_id);
            this.event = theReason;
            this.announceURL = announceURL;
            this.tracker_id = tracker_id;
            this.info_hash = info_hash;
            this.itsStatistic = theStatistic;
        }
 /*       
	public BTPeerToTrackerRequest(Reason theReason, int theNumberOfRequestedPeers, OverlayKey theDocument, BTContact theP2PAddress, BTInternStatistic theStatistic, OverlayID theSender, OverlayID theReceiver) {
		super(theirType, theirTransportProtocol, true, 0, theSender, theReceiver);
		//TODO: Calculate the size!
		//Size calculation is much more difficult for tracker communication. But it only is a small part of the whole protocol overhead.
		this.initialize(theReason, theNumberOfRequestedPeers, theDocument, theP2PAddress, theStatistic);
	}
	
	public BTPeerToTrackerRequest(Reason theReason, int theNumberOfRequestedPeers, OverlayKey theDocument, BTContact theP2PAddress, OverlayID theSender, OverlayID theReceiver) {
		super(theirType, theirTransportProtocol, true, 0, theSender, theReceiver);
		this.initialize(theReason, theNumberOfRequestedPeers, theDocument, theP2PAddress, null);
	}
	*/
	/**
	 * This constructor creates a message that doesn't request for new contacts.
	 * @param theReason
	 * @param theDocument
	 * @param theP2PAddress
	 * @param theStatistic
	 * @param theSender
	 * @param theReceiver
	 */
        
        /*
	public BTPeerToTrackerRequest(Reason theReason, OverlayKey theDocument, BTContact theP2PAddress, BTInternStatistic theStatistic, OverlayID theSender, OverlayID theReceiver) {
		super(theirType, theirTransportProtocol, true, 0, theSender, theReceiver);
		this.initialize(theReason, 0, theDocument, theP2PAddress, theStatistic);
	}
	
	private void initialize(Reason theReason, int theNumberOfRequestedPeers, OverlayKey theDocument, BTContact theP2PAddress, BTInternStatistic theStatistic) {
		if (theReason == null)
			throw new RuntimeException("'theReason' must not be 'null'!");
		if (theDocument == null)
			throw new RuntimeException("'theDocument' must not be 'null'!");
		if (theP2PAddress == null)
			throw new RuntimeException("'theP2PAddress' must not be 'null'!");
		this.itsReason = theReason;
		this.itsNumberOfRequestedPeers = theNumberOfRequestedPeers;
		this.itsDocument = theDocument;
		this.itsP2PAddress = theP2PAddress;
		this.itsStatistic = theStatistic;
	}
	
*/	
	
	public Reason getReason() {
		return this.event;
	}
	
	public BTInternStatistic getStatistic() {
		return this.itsStatistic;
	}
	
	public String getDocument() {
		return this.info_hash;
	}
	
	public BTContact getPeerID() {
		return this.peer_id;
	}
	
	public int getNumberOfWanted() {
		return this.numwant;
	}
	
	public static Type getStaticType() {
		return BTMessage.Type.TRACKER_REQUEST;
	}
	
	public static TransProtocol getStaticTransportProtocol() {
		return BTUtil.TCP;
	}
	
        public void setUploaded(String uploaded){
            this.uploaded = uploaded;
        }
        
        public void setDownloaded(String downloaded){
            this.downloaded = downloaded;
        }
        public void setLeft(String left){
            this.left = left;
        }
        
        public String getFormedURL(){
            
            String url = "http://"+announceURL+"/?info_hash="+info_hash+"&peer_id="+peer_id+"&port="+port+"&uploaded="+uploaded+"&downloaded="+downloaded+"&left="+left+"&compact=1&numwant="+numwant+"&event="+event+"&ip="+ip;
            if(tracker_id != null) url += tracker_id;
            return url;
        }
}
