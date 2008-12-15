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

import gods.agent.Agent;
import gods.arch.AbstractModule;
import gods.arch.Subscription;
import gods.deploy.events.AgentBootEvent;
import gods.deploy.events.PrepareEvent;
import gods.experiment.agent.SyncClocksHandler;
import gods.experiment.events.SyncClocks;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * The <code>AgentDeploymentModule</code> class is the Agent's module
 * responsible for deployment of the agent which includes collection of all
 * pre-experiment information and sending related notifications to ControlCenter
 * 
 * @author Ozair Kafray
 * @version $Id$
 */
public class AgentDeploymentModule extends AbstractModule {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger.getLogger(AgentDeploymentModule.class);

	/**
	 * Sets the thread name to module name
	 */
	public AgentDeploymentModule(){
		super(AgentDeploymentModule.class.getSimpleName());
	}
	
	/**
	 * {@link gods.arch.AbstractModule#initialize()}
	 */
	@Override
	public void initialize() {
		// Initialize Modules local variables here

		// Subscribe to events
		subscribe();
	}

	private void subscribe() {
		log.debug(this.getClass() + " subscribing Events...");
		// Create List of subscriptions for events of interest
		List<Subscription> subscriptions = new LinkedList<Subscription>();

		/*
		 * Fill in subscriptions including reference to this module, events and
		 * corresponding EventHandlers
		 */

		// Add AgentBootEvent to subscription list
		Subscription subscription = new Subscription(this,
				AgentBootEvent.class, AgentBootEventDeploymentHandler.class);
		subscriptions.add(subscription);

		// Add PrepareEvent to subscription list
		subscription = new Subscription(this, PrepareEvent.class,
				PrepareEventAgentDeploymentHandler.class);
		subscriptions.add(subscription);
		
		// Add SyncClock to subscription list
		subscription = new Subscription(this, SyncClocks.class,
				SyncClocksHandler.class);
		subscriptions.add(subscription);

		// Subscribing Events
		Agent.getInstance().subscribe(subscriptions);
	}
}
