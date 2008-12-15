/**
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.deploy.cc;

import gods.arch.Event;
import gods.arch.EventHandler;
import gods.arch.Module;
import gods.cc.ControlCenter;
import gods.deploy.agent.PrepareEventAgentDeploymentHandler;
import gods.deploy.events.ReadyEvent;
import gods.topology.events.SlotInformationChanged;

import org.apache.log4j.Logger;

/**
 * The <code>ReadyEventDeploymentHandler</code> class is responsible for
 * handling a ReadyEvent received by ControlCenter for its DeploymentModule
 * 
 * @author Ozair Kafray
 * @version $Id$
 */
public class ReadyEventDeploymentHandler extends EventHandler {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger
			.getLogger(PrepareEventAgentDeploymentHandler.class);

	/**
	 * @param event
	 * @param module
	 */
	public ReadyEventDeploymentHandler(Event event, Module module) {
		super(event, module);
	}

	/**
	 * Handles the Ready Event for DeploymentModule of ControlCenter
	 * 
	 * {@link gods.arch.EventHandler#handle()}
	 */
	@Override
	public Object handle() throws ClassCastException {

		log.debug("");

		ReadyEvent readyEvent = (ReadyEvent) event;
		DeploymentModule deploymentModule = (DeploymentModule) module;

		log.info("Received Ready Event from Agent on host: "
				+ readyEvent.getHostName());

		// Change Agent's status to READY
		deploymentModule.agentReady(readyEvent.getHostName(), readyEvent
				.getSlotsInformation());

		SlotInformationChanged slotInfoChanged = new SlotInformationChanged(1);
		slotInfoChanged.setChangedSlots(ControlCenter.getInstance()
				.getAllSlots());
		
		ControlCenter.getInstance().enqueueEvent(slotInfoChanged);

		return null;
	}

}
