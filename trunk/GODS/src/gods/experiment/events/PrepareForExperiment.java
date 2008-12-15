/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.experiment.events;

import gods.arch.AbstractEvent;
import gods.experiment.ExperimentEvent;

import java.util.Vector;

/**
 * The <code>PrepareForExperiment</code> class
 *
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class PrepareForExperiment extends AbstractEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4263578341475407845L;
	
	/**
	 * 
	 */
	private Vector<ExperimentEvent> experimentEvents = null;
	
	/**
	 * 
	 */
	private String initScript = null; 

	/**
	 * @param priority
	 */
	public PrepareForExperiment(int priority) {
		super(priority);
	}

	/**
	 * @param priority
	 */
	public PrepareForExperiment(int priority, Vector<ExperimentEvent> events) {
		super(priority);
		this.experimentEvents = events; 
	}

	/**
	 * @return the events
	 */
	public Vector<ExperimentEvent> getExperimentEvents() {
		return experimentEvents;
	}

	/**
	 * @param events the events to set
	 */
	public void setExperimentEvents(Vector<ExperimentEvent> events) {
		this.experimentEvents = events;
	}

	/**
	 * @return the initScript
	 */
	public String getInitScript() {
		return initScript;
	}

	/**
	 * @param initScript the initScript to set
	 */
	public void setInitScript(String initScript) {
		this.initScript = initScript;
	}

	
}
