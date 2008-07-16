package uc3m.netcom.overlay.bt;



import uc3m.netcom.transport.TransInfo;
import jBittorrentAPI.TorrentFile;

/**
 * This class represents a '.torrent' file.
 * It contains all the data, such a file would have:
 * The documents hash: Here as the OverlayKey
 * The tracker address: Here as OverlayID and TransportAddress
 * And the size of the document.
 * @author Jan Stolzenburg
 */
public class BTTorrent {
	
	
	private BTID itsTrackerID;
	
	private TransInfo itsTrackerAddress;
	
	private TorrentFile tf;
        
	
	public BTTorrent(TorrentFile tf, long theSize, BTID theTrackerID, TransInfo theTrackerAddress) {

                this.tf = tf;
		this.itsTrackerID = theTrackerID;
		this.itsTrackerAddress = theTrackerAddress;
	}
	
	public String getKey() {
                return new String(this.tf.info_hash_as_binary);
	}
	
	public long getSize() {
		return this.tf.total_length;
	}
	
	public TransInfo getTrackerAddress() {
            
		return this.itsTrackerAddress;
	}
	
	public BTID getTrackerID() {
		return this.itsTrackerID;
	}
	
        public TorrentFile getFile(){
            return this.tf;
        }
}
