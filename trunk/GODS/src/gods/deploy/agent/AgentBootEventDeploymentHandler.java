/**
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.deploy.agent;

import org.apache.log4j.Logger;

import gods.agent.Agent;
import gods.arch.Event;
import gods.arch.EventHandler;
import gods.arch.Module;
import gods.deploy.events.AgentBootEvent;
import gods.deploy.events.JoinedEvent;

/**
 * The <code>AgentBootEventDeploymentHandler</code> class is responsible for
 * handling the AgentBootEvent for AgentDeploymentModule
 * 
 * @author Ozair Kafray
 * @version $Id$
 */
public class AgentBootEventDeploymentHandler extends EventHandler {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger
			.getLogger(AgentBootEventDeploymentHandler.class);

	public AgentBootEventDeploymentHandler(Event event, Module module) {
		super(event, module);
		// TODO Auto-generated constructor stub
	}

	/**
	 * {@link gods.arch.EventHandler#handle()}
	 */
	@Override
	public Object handle() throws ClassCastException {

		log.debug("");
		AgentBootEvent agentBoot = (AgentBootEvent) event;

		Agent.getInstance().initialize(agentBoot.getCcHostName());

		JoinedEvent joinedEvent = new JoinedEvent(1);
		joinedEvent.setHostName(Agent.getMachineInformation()
				.getHostName());

		Agent.getInstance().notifyControlCenter(joinedEvent);

		return null;
	}

}
