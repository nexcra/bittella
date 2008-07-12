package uc3m.netcom.overlay.bt;



import uc3m.netcom.transport.TransInfo;


/**
 * This class represents a '.torrent' file.
 * It contains all the data, such a file would have:
 * The documents hash: Here as the OverlayKey
 * The tracker address: Here as OverlayID and TransportAddress
 * And the size of the document.
 * @author Jan Stolzenburg
 */
public class BTTorrent {
	
	
	
	private String itsDocumentKey;
	
	private long itsSize;
	
	private BTID itsTrackerID;
	
	private TransInfo itsTrackerAddress;
	
	
	
	public BTTorrent(String theDocumentKey, long theSize, BTID theTrackerID, TransInfo theTrackerAddress) {
		this.itsDocumentKey = theDocumentKey;
		this.itsSize = theSize;
		this.itsTrackerID = theTrackerID;
		this.itsTrackerAddress = theTrackerAddress;
	}
	
	public String getKey() {
		return this.itsDocumentKey;
	}
	
	public long getSize() {
		return this.itsSize;
	}
	
	public TransInfo getTrackerAddress() {
		return this.itsTrackerAddress;
	}
	
	public BTID getTrackerID() {
		return this.itsTrackerID;
	}
	
}
