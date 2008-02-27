package de.tud.kom.p2psim.overlay.bt.algorithm;

import java.util.Collection;

import de.tud.kom.p2psim.overlay.bt.BTContact;
import de.tud.kom.p2psim.overlay.bt.BTDocument;
import de.tud.kom.p2psim.overlay.bt.BTInternRequest;

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
