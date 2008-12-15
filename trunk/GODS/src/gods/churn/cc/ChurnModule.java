/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.churn.cc;

import gods.arch.AbstractModule;
import gods.arch.Subscription;
import gods.cc.ControlCenter;
import gods.churn.events.ApplicationKilledEvent;
import gods.churn.events.ApplicationLaunchedEvent;
import gods.churn.events.ApplicationStoppedEvent;
import gods.churn.events.KillApplicationEvent;
import gods.churn.events.LaunchApplicationEvent;
import gods.churn.events.StopApplicationEvent;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * The <code>ChurnModule</code> class
 *
 * @author Ozair Kafray
 * @version $Id$
 */
public class ChurnModule extends AbstractModule {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger.getLogger(ChurnModule.class);

	/**
	 * Sets the thread name to module name
	 */
	public ChurnModule() {
		super(ChurnModule.class.getSimpleName());
	}

	/**
	 * {@link gods.arch.AbstractModule#initialize()}
	 */
	@Override
	public void initialize() {
		// Initialize Modules Local variables here

		// Subscribe to events
		subscribe();
	}
	
	private void subscribe() {
		log.debug(" subscribing Events...");
		// Create List of subscriptions for events of interest
		List<Subscription> subscriptions = new LinkedList<Subscription>();

		// Fill in subscriptions including reference to this module, events and
		// corresponding EventHandlers

		// Add LaunchApplicationEvent to subscription list
		Subscription subscription = new Subscription(this, LaunchApplicationEvent.class,
				LaunchApplicationChurnEventHandler.class);
		subscriptions.add(subscription);

		// Add ApplicationLaunched to subscription list
		subscription = new Subscription(this, ApplicationLaunchedEvent.class,
				ApplicationLaunchedChurnEventHandler.class);
		subscriptions.add(subscription);
		
		// Add KillApplicationEvent to subscription list
		subscription = new Subscription(this, KillApplicationEvent.class,
				KillApplicationChurnEventHandler.class);
		subscriptions.add(subscription);
		
		// Add ApplicationKilled to subscription list
		subscription = new Subscription(this, ApplicationKilledEvent.class,
				ApplicationKilledChurnEventHandler.class);
		subscriptions.add(subscription);
		
		// Add StopApplicationEvent to subscription list
		subscription = new Subscription(this, StopApplicationEvent.class,
				StopApplicationChurnEventHandler.class);
		subscriptions.add(subscription);
		
		// Add ApplicationStoppedEvent to subscription list
		subscription = new Subscription(this, ApplicationStoppedEvent.class,
				ApplicationStoppedChurnEventHandler.class);
		subscriptions.add(subscription);
		
		// Subscribing Events
		ControlCenter.getInstance().subscribe(subscriptions);
	}
}
