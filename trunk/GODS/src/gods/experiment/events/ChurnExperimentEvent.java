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

import gods.experiment.ExperimentEvent;

/**
 * The <code>ChurnExperimentEvent</code> interface
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public interface ChurnExperimentEvent extends ExperimentEvent {

	/**
	 * @return virtual node id on which the application under test is to be
	 *         launched or is running.
	 */
	public int getVnid();

}
