/**
 * 
 */
package uc3m.netcom.overlay.bt;

import java.util.List;

import de.tud.kom.p2psim.api.application.Application;
import de.tud.kom.p2psim.api.common.Component;
import de.tud.kom.p2psim.api.common.ComponentFactory;
import de.tud.kom.p2psim.api.common.Host;
import de.tud.kom.p2psim.api.overlay.OverlayNode;

/**
 * @author Jan Stolzenburg
 *
 */
public class BTFactory implements ComponentFactory {

	public Application newApplication(List<OverlayNode> overlays) {
		throw new RuntimeException("Method not yet implemented!");
	}

	public BTTrackerApplication newServer(BTTrackerNode theTrackerNode) {
		return new BTTrackerApplication(theTrackerNode);
	}
	
	public BTClientApplication newClient(BTDataStore theDataBus, BTPeerSearchNode theDhtNode, BTPeerDistributeNode theDistributionStrategy) {
		return new BTClientApplication(theDataBus, theDhtNode, theDistributionStrategy);
	}

	public Component createComponent(Host host) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Method 'createComponent' in class 'BTFactory' not yet implemented!");
		//return null;
	}

	/* Currently not used.
	public Application newClient(List<OverlayNode> overlays) {
		DHTNode dhtNode = (DHTNode) findOverlay(overlays, DHTNode.class);
		DistributionStrategy strategy = (DistributionStrategy) findOverlay(overlays, DistributionStrategy.class);
		BTClientApplication client = new BTClientApplication(dhtNode, strategy);
		return client;
	}
	
	private OverlayNode findOverlay(List<OverlayNode> overlays, Class overlayApi) {
		for (OverlayNode node : overlays) {
			if(overlayApi.isInstance(node)) return node;
		}
		return null;
	}
	*/
}
