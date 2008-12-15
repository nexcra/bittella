/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.experiment;

/**
 * The <code>ChurnEventGenerator</code> class
 *
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public interface ChurnEventGenerator {

	
	/**
	 * @return - ExperimentEvent which is the next generated event for the
	 *         experiment
	 */
	public AbstractExperimentEvent getNextChurnEvent();
	
	/**
	 * @return no of total nodes joining the ring
	 */
	public int getJoinedNodesCount();
	
}
