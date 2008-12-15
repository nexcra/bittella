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
import gods.experiment.Experiment;

/**
 * The <code>PrepareExperiment</code> class
 *
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class RunExperiment extends AbstractEvent {

	/**
	 * Path to the file storing experiment information
	 */
	private Experiment experiment;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -527686780545641705L;

	/**
	 * @param priority
	 */
	public RunExperiment(int priority) {
		super(priority);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * @return the experimentFile
	 */
	public Experiment getExperiment() {
		return experiment;
	}

	/**
	 * @param experimentFile the experimentFile to set
	 */
	public void setExperiment(Experiment experiment) {
		this.experiment = experiment;
	}

}
