package uc3m.netcom.overlay.bt.operation;

import org.apache.log4j.Logger;

import de.tud.kom.p2psim.api.common.OperationCallback;
import de.tud.kom.p2psim.api.common.SupportOperations;
import de.tud.kom.p2psim.impl.common.AbstractOperation;
import de.tud.kom.p2psim.impl.util.logging.SimLogger;

/**
 * The abstract super class of all BitTorrent Operations.
 * @author Jan Stolzenburg
 * @param <OwnerType> the class of the component that owns this operation
 * @param <ResultType> the class of the result of this operation
 */
public abstract class BTOperation<OwnerType extends SupportOperations, ResultType extends Object> extends AbstractOperation<OwnerType, ResultType> {
	
	static final Logger log = SimLogger.getLogger(BTOperation.class);
	
	public BTOperation(OwnerType theOwningComponent, OperationCallback<ResultType> theCallback) {
		super(theOwningComponent, theCallback);
		log.debug("Operation started: " + this.getClass().getSimpleName());
	}
	
	/**
	 * Abort the operation.
	 * This is very usefull for operations that get executed regularly.
	 * @param success Was the operation successfull?
	 */
	public void stop(boolean success) {
		this.operationFinished(success);
		log.debug("Operation stopped. Success? " + success);
	}
	
}
