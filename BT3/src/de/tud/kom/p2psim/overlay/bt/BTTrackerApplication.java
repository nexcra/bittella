package de.tud.kom.p2psim.overlay.bt;

import org.apache.log4j.Logger;

import de.tud.kom.p2psim.api.common.Operation;
import de.tud.kom.p2psim.api.common.OperationCallback;
import de.tud.kom.p2psim.impl.application.AbstractApplication;
import de.tud.kom.p2psim.impl.util.logging.SimLogger;

/**
 * This class represents the tracker application.
 * To instance it, you have to supply a <code>BTTrackerNode</code> to the constructor.
 * To make it work, you have to use the methods of <code>BTTrackerNode</code>.
 * @author Jan Stolzenburg
 */
public class BTTrackerApplication extends AbstractApplication {
	
	
	
	/**
	 * This node handles the connections to the peers. It is the only node of tracker.
	 */
	BTTrackerNode itsPeerContactNode;
	
//	private Collection<ApplicationEventHandler> itsApplicationEventHandlers;
	
	static final Logger log = SimLogger.getLogger(BTTrackerApplication.class);
	
	
	
	public BTTrackerApplication(BTTrackerNode thePeerContactNode) {
		this.itsPeerContactNode = thePeerContactNode;
//		this.itsApplicationEventHandlers = new LinkedList<ApplicationEventHandler>();
//		this.itsPeerContactNode.registerEventHandler(this);
		log.debug("Tracker created!");
	}
	
	
	
//	public void registerEventHandler(ApplicationEventHandler theApplicationEventHandler) {
//		this.itsApplicationEventHandlers.add(theApplicationEventHandler);
//	}
	
	public void connect() {
		this.itsPeerContactNode.connect();
	}
	
	public Operation createOperation(String opName, String[] params, OperationCallback caller) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Method 'createOperation' in class 'BTTrackerApplication' not yet implemented!");
		//return null;
	}
	
}
