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

import gods.experiment.AbstractExperimentEvent;

/**
 * The <code>AbstractChurnExperimentEvent</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public abstract class AbstractChurnExperimentEvent extends
		AbstractExperimentEvent implements ChurnExperimentEvent {

	/**
	 * Id of the virtual node on which the churn event is to take place
	 */
	protected int vnid;

	/**
	 * Default Constructor
	 */
	public AbstractChurnExperimentEvent() {

	}

	/**
	 * @param vnid
	 *            the vnid to set
	 */
	public void setVnid(int vnid) {
		this.vnid = vnid;
	}

	/**
	 * @return the vnid
	 */
	public int getVnid() {
		return vnid;
	}

}
