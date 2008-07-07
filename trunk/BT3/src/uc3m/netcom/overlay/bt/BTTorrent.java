package uc3m.netcom.overlay.bt;



import de.tud.kom.p2psim.api.overlay.DHTObject;
import de.tud.kom.p2psim.api.overlay.OverlayID;
import de.tud.kom.p2psim.api.overlay.OverlayKey;
import de.tud.kom.p2psim.api.transport.TransInfo;


/**
 * This class represents a '.torrent' file.
 * It contains all the data, such a file would have:
 * The documents hash: Here as the OverlayKey
 * The tracker address: Here as OverlayID and TransportAddress
 * And the size of the document.
 * @author Jan Stolzenburg
 */
public class BTTorrent implements DHTObject {
	
	
	
	private OverlayKey itsDocumentKey;
	
	private long itsSize;
	
	private OverlayID itsTrackerID;
	
	private TransInfo itsTrackerAddress;
	
	
	
	public BTTorrent(OverlayKey theDocumentKey, long theSize, OverlayID theTrackerID, TransInfo theTrackerAddress) {
		this.itsDocumentKey = theDocumentKey;
		this.itsSize = theSize;
		this.itsTrackerID = theTrackerID;
		this.itsTrackerAddress = theTrackerAddress;
	}
	
	public OverlayKey getKey() {
		return this.itsDocumentKey;
	}
	
	public long getSize() {
		return this.itsSize;
	}
	
	public TransInfo getTrackerAddress() {
		return this.itsTrackerAddress;
	}
	
	public OverlayID getTrackerID() {
		return this.itsTrackerID;
	}
	
}
