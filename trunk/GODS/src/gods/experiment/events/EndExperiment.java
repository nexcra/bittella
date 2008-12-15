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
 * The <code>EndExperiment</code> class signifies the end of an experiment and
 * performs post experiment functions
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class EndExperiment extends AbstractEvent {

	/**
	 * The experiment that has just ended
	 */
	Experiment experiment;

	/**
	 * 
	 */
	private static final long serialVersionUID = 885833591122170222L;

	/**
	 * @param priority
	 */
	public EndExperiment(int priority) {
		super(priority);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the experiment
	 */
	public Experiment getExperiment() {
		return experiment;
	}

	/**
	 * @param experiment the experiment to set
	 */
	public void setExperiment(Experiment experiment) {
		this.experiment = experiment;
	}
}
