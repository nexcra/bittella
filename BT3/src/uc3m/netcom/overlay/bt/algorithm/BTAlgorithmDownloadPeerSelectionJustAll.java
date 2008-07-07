package uc3m.netcom.overlay.bt.algorithm;

import java.util.Collection;

import uc3m.netcom.overlay.bt.BTContact;
import uc3m.netcom.overlay.bt.BTDocument;
import uc3m.netcom.overlay.bt.BTInternRequest;

/**
 * This algorithm doesn't reject any peers and returns the "theNewContacts" collection unchanged.
 * @author Jan Stolzenburg
 */
public class BTAlgorithmDownloadPeerSelectionJustAll implements BTAlgorithmDownloadPeerSelection {
	
	public void setup(BTDocument theDocument) {
		//This algorithm doesn't need any setup.
	}
	
	public void filterNewContacts(Collection<BTContact> theNewContacts, Collection<BTContact> theOldContacts, Collection<BTInternRequest> thePendingRequests) {
		return;
	}
	
	public boolean isSetup() {
		return true;
	}
	
}
