package uc3m.netcom.overlay.bt;

//import org.apache.log4j.Logger;

import uc3m.netcom.common.Operation;
import uc3m.netcom.common.OperationCallback;
import uc3m.netcom.common.Application;
import uc3m.netcom.overlay.bt.operation.BTOperation;
//import de.tud.kom.p2psim.impl.util.logging.SimLogger;

/**
 * This class represents the tracker application.
 * To instance it, you have to supply a <code>BTTrackerNode</code> to the constructor.
 * To make it work, you have to use the methods of <code>BTTrackerNode</code>.
 * @author Jan Stolzenburg
 */
public class BTTrackerApplication implements Application {
	
	
	
	/**
	 * This node handles the connections to the peers. It is the only node of tracker.
	 */
	BTTrackerNode itsPeerContactNode;
	
//	private Collection<ApplicationEventHandler> itsApplicationEventHandlers;
	
	//static final Logger log = SimLogger.getLogger(BTTrackerApplication.class);
	
	
	
	public BTTrackerApplication(BTTrackerNode thePeerContactNode) {
		this.itsPeerContactNode = thePeerContactNode;
//		this.itsApplicationEventHandlers = new LinkedList<ApplicationEventHandler>();
//		this.itsPeerContactNode.registerEventHandler(this);
		//log.debug("Tracker created!");
	}
	
	
	
//	public void registerEventHandler(ApplicationEventHandler theApplicationEventHandler) {
//		this.itsApplicationEventHandlers.add(theApplicationEventHandler);
//	}
	
	public void connect() {
		this.itsPeerContactNode.connect();
	}
        
        public int start(OperationCallback opc){
            return -1;
        }
        
        public int close(OperationCallback opc){
            return -1;
        }
	
	public Operation createOperation(String opName, String[] params, OperationCallback caller) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Method 'createOperation' in class 'BTTrackerApplication' not yet implemented!");
		//return null;
	}
//                @Override
	public void calledOperationFailed(Operation op) {
            if(op instanceof BTOperation){
                //logger.process(this.getClass().toString(),new Object[]{op,new Long(Simulator.getCurrentTime()),new Boolean(false)});
            }
	}

//        @Override
	public void calledOperationSucceeded(Operation opd) {
            if(opd instanceof BTOperation){
                BTOperation op = (BTOperation) opd;
                //logger.process(this.getClass().toString(),new Object[]{op,new Long(op.getFinishedTime()),new Boolean(true)});
            }
	}
        
}
