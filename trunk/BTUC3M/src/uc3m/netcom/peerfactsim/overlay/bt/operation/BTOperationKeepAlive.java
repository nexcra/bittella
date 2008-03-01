package uc3m.netcom.peerfactsim.overlay.bt.operation;

import org.apache.log4j.Logger;

import de.tud.kom.p2psim.api.common.OperationCallback;
import de.tud.kom.p2psim.api.overlay.DistributionStrategy;
import de.tud.kom.p2psim.overlay.bt.BTContact;
import de.tud.kom.p2psim.overlay.bt.BTDataStore;
import de.tud.kom.p2psim.overlay.bt.BTDocument;
import de.tud.kom.p2psim.overlay.bt.manager.BTConnectionManager;

/**
 * This operation sends keep alive messages and
 * closes unneccessary connections.
 * @param <OwnerType> the class of the component that owns this operation
 * @author Jan Stolzenburg
 */
public class BTOperationKeepAlive<OwnerType extends DistributionStrategy> extends de.tud.kom.p2psim.overlay.bt.operation.BTOperationKeepAlive<OwnerType> {
	

	
	
	
	public BTOperationKeepAlive(BTDataStore theDataBus, BTDocument theDocument, BTContact theOwnContact, OwnerType theOwningComponent, OperationCallback<Void> theOperationCallback, BTConnectionManager theConnectionManager) {
		super(theDataBus,theDocument,theOwnContact,theOwningComponent, theOperationCallback, theConnectionManager);
	}
	
	@Override
	protected void execute() {
            super.execute();
	}
	
}
