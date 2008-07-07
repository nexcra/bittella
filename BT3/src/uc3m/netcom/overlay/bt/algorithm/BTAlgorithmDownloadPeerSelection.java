package uc3m.netcom.overlay.bt.algorithm;

import java.util.Collection;

import uc3m.netcom.overlay.bt.BTContact;
import uc3m.netcom.overlay.bt.BTDocument;
import uc3m.netcom.overlay.bt.BTInternRequest;

/**
 * This interface is for a class that selects, which peers should be contacted for downloading,
 * therewith they can be asked for uploading pieces later.
 * It just decides about the handshakes, not about the actual requests.
 * @author Jan Stolzenburg
 */
public interface BTAlgorithmDownloadPeerSelection extends BTAlgorithm {
	
	public void setup(BTDocument theDocument);
	
	public void filterNewContacts(Collection<BTContact> theNewContacts, Collection<BTContact> theOldContacts, Collection<BTInternRequest> thePendingRequests);
	
}
