/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.experiment.agent;

import gods.agent.Agent;
import gods.arch.AbstractModule;
import gods.arch.Subscription;
import gods.experiment.ExperimentEvent;
import gods.experiment.events.ExecuteExperiment;
import gods.experiment.events.PrepareForExperiment;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * The <code>AgentExperimentExecutor</code> class
 *
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class AgentExperimentExecutor extends AbstractModule {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger
			.getLogger(AgentExperimentExecutor.class);
	
	private Vector<ExperimentEvent> experimentEvents = null;
	/**
	 * @param moduleName
	 */
	public AgentExperimentExecutor() {
		super("AgentExperimentExecutor");
	}

	/* (non-Javadoc)
	 * @see gods.arch.AbstractModule#initialize()
	 */
	@Override
	protected void initialize() {
		subscribe();
	}
	
	private void subscribe(){
		log.debug(this.getClass() + " subscribing Events...");
		// Create List of subscriptions for events of interest
		List<Subscription> subscriptions = new LinkedList<Subscription>();

		// Fill in subscriptions including reference to this module, events and
		// corresponding EventHandlers

		// Add PrepareExperiment to subscription list
		Subscription subscription = new Subscription(this,
				PrepareForExperiment.class, PrepareForExperimentHandler.class);
		subscriptions.add(subscription);
		
		// Add ExecuteExperiment to subscription list		
		subscription = new Subscription(this,
				ExecuteExperiment.class, ExecuteExperimentHandler.class);
		subscriptions.add(subscription);
		
		// Subscribing Events
		Agent.getInstance().subscribe(subscriptions);
	}

	/**
	 * @return the experimentEvents
	 */
	public Vector<ExperimentEvent> getExperimentEvents() {
		return experimentEvents;
	}

	/**
	 * @param experimentEvents the experimentEvents to set
	 */
	public void setExperimentEvents(Vector<ExperimentEvent> experimentEvents) {
		this.experimentEvents = experimentEvents;
	}

}
