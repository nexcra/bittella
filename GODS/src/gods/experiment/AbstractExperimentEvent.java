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
 * The <code>ExperimentEvent</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public abstract class AbstractExperimentEvent implements ExperimentEvent{

	/**
	 * The time from To (T-not) at which this event should be triggered
	 */
	protected TimeStamp timeToHappen;

	/**
	 * @param timeToHappen the timeToHappen to set
	 */
	public void setTimeToHappen(TimeStamp timeToHappen) {
		this.timeToHappen = timeToHappen;
	}

	/**
	 * @return the timeToHappen
	 */
	public TimeStamp getTimeToHappen() {
		return timeToHappen;
	}
}
