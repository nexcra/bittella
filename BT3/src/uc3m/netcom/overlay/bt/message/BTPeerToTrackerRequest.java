package uc3m.netcom.overlay.bt.message;

import uc3m.netcom.overlay.bt.BTID;
import uc3m.netcom.overlay.bt.BTUtil;
import uc3m.netcom.overlay.bt.BTContact;
import uc3m.netcom.overlay.bt.BTInternStatistic;
import uc3m.netcom.transport.TransProtocol;
import jBittorrentAPI.TorrentFile;

public class BTPeerToTrackerRequest extends BTMessage {
	
    
	public enum Reason {started, stopped, completed,empty}
	
	private BTInternStatistic itsStatistic; //no se si lo utilizaremos
	
	private String event;
        private Reason t_event;
	private String info_hash;
        private BTID peer_id;;
        private long uploaded = 0;
        private long downloaded = 0;
        private long left = 0;
        private String compact = "1";
        private BTID tracker_id;
        private TorrentFile tf;
	
	public BTPeerToTrackerRequest(Reason theReason,TorrentFile tf, BTID tracker_id,BTID peer_id, BTInternStatistic theStatistic){
            super(BTMessage.Type.TRACKER_REQUEST,BTUtil.TCP,true,0,peer_id,tracker_id);
            this.tf = tf;
            this.tracker_id = tracker_id;
            this.peer_id = peer_id;
            this.info_hash = tf.info_hash_as_url;
            this.itsStatistic = theStatistic;
            this.t_event = theReason;
            
            switch(theReason){
                case started:
                    event = "&event=started";
                    break;
                case stopped:
                    event = "&event=stopped";
                    break;
                case completed:
                    event = "&event=completed";
                    break;
                case empty:
                    event = "&event=empty";
                    break;
            }
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
	public TorrentFile getDocument(){
            return this.tf;
        }
        
	public String getReason() {
            
		return this.event;
	}
        
        public Reason getTReason(){
            return this.t_event;
        }
	
	public BTInternStatistic getStatistic() {
		return this.itsStatistic;
	}
	
	public String getDocID() {
		return this.info_hash;
	}
	
	public BTID getPeerID() {
		return this.peer_id;
	}
	
	public BTID getTrackerID(){
            return this.tracker_id;
        }
        
	public static Type getStaticType() {
		return BTMessage.Type.TRACKER_REQUEST;
	}
	
	public static TransProtocol getStaticTransportProtocol() {
		return BTUtil.TCP;
	}
	
        public void setUploaded(long uploaded){
            this.uploaded = uploaded;
        }
        
        public void setDownloaded(long downloaded){
            this.downloaded = downloaded;
        }
        public void setLeft(long left){
            this.left = left;
        }
        
        public long getUploaded(){
            return uploaded;
        }
        
        public long getDownloaded(){
            return downloaded;
        }
        
        public long getLeft(){
            return left;
        }
        
}
