/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.experiment.cc;

import gods.arch.AbstractModule;
import gods.arch.Subscription;
import gods.cc.ControlCenter;
import gods.experiment.Experiment;
import gods.experiment.events.ClockSynced;
import gods.experiment.events.EndExperiment;
import gods.experiment.events.ExperimentFinished;
import gods.experiment.events.ReadyForExperiment;
import gods.experiment.events.RunExperiment;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * The <code>ExperimentExecutorModule</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class ExperimentExecutorModule extends AbstractModule {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger
			.getLogger(ExperimentExecutorModule.class);

	/**
	 * 
	 */
	private Experiment experiment;

	/**
	 * Agents that are responsible for executing this experiment. All agents
	 * might not be involved in an experiment.
	 */
	private Vector<String> responsibleAgents = null;

	/**
	 * @param moduleName
	 */
	public ExperimentExecutorModule() {
		super("ExperimentExecutor");
	}

	/**
	 * {@link gods.arch.AbstractModule#initialize()}
	 */
	@Override
	public void initialize() {

		subscribe();
	}

	private void subscribe() {
		log.debug(this.getClass() + " subscribing Events...");
		// Create List of subscriptions for events of interest
		List<Subscription> subscriptions = new LinkedList<Subscription>();

		// Fill in subscriptions including reference to this module, events and
		// corresponding EventHandlers

		// Add PrepareExperiment to subscription list
		Subscription subscription = new Subscription(this, RunExperiment.class,
				RunExperimentHandler.class);
		subscriptions.add(subscription);

		// Add ClockSynced to subscription list
		subscription = new Subscription(this, ClockSynced.class,
				ClockSyncedHandler.class);
		subscriptions.add(subscription);

		// Add ExecuteExperiment to subscription list
		subscription = new Subscription(this, ReadyForExperiment.class,
				ReadyForExperimentHandler.class);
		subscriptions.add(subscription);

		// Add ExperimentFinished to subscription list
		subscription = new Subscription(this, ExperimentFinished.class,
				ExperimentFinishedHandler.class);
		subscriptions.add(subscription);

		// Add EndExperiment to subscription list
		subscription = new Subscription(this, EndExperiment.class,
				EndExperimentHandler.class);
		subscriptions.add(subscription);

		// Subscribing Events
		ControlCenter.getInstance().subscribe(subscriptions);
	}

	/**
	 * @return the experiment
	 */
	public Experiment getExperiment() {
		return experiment;
	}

	/**
	 * @param experiment
	 *            the experiment to set
	 */
	public void setExperiment(Experiment experiment) {
		this.experiment = experiment;
	}

	/**
	 * @return the responsibleAgents
	 */
	public Vector<String> getResponsibleAgents() {
		return responsibleAgents;
	}

	/**
	 * @param responsibleAgents the responsibleAgents to set
	 */
	public void setResponsibleAgents(Vector<String> responsibleAgents) {
		this.responsibleAgents = responsibleAgents;
	}

}
