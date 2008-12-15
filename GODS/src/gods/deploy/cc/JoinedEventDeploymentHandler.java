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
import gods.deploy.events.JoinedEvent;

import org.apache.log4j.Logger;

/**
 * The <code>JoinedEventDeploymentHandler</code> class is responsible for
 * handling the JoinedEvent by any Agent to the ControlCenter for its
 * DeploymentModule
 * 
 * @author Ozair Kafray
 * @version $Id$
 */
public class JoinedEventDeploymentHandler extends EventHandler {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger
			.getLogger(JoinedEventDeploymentHandler.class);

	/**
	 * @param event
	 * @param module
	 */
	public JoinedEventDeploymentHandler(Event event, Module module) {
		super(event, module);
		// TODO Auto-generated constructor stub
	}

	/** 
	 * {@link gods.arch.EventHandler#handle()}
	 */
	@Override
	public Object handle() throws ClassCastException {

		log.debug("");
		JoinedEvent joinedEvent = (JoinedEvent) event;
		DeploymentModule deploymentModule = (DeploymentModule) module;

		log.info("Agent joined from host..." + joinedEvent.getHostName());

		// Change Agent's status to JOINED
		deploymentModule.agentJoined(joinedEvent.getHostName());

		return null;
	}

}
