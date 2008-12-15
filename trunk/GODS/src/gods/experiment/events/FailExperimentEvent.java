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

/**
 * The <code>FailChurnEvent</code> class
 *
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class FailExperimentEvent extends AbstractChurnExperimentEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2438856120424626631L;

	/**
	 * Default Constructor
	 */
	public FailExperimentEvent() {

	}

	/* (non-Javadoc)
	 * @see gods.experiment.events.AbstractChurnExperimentEvent#toString()
	 */
	@Override
	public String toString() {
		return getTimeToHappen().toString()
		+ String.format("\t%12d F", vnid);
	}
}
