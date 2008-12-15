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

/**
 * The <code>ReadyForExperiment</code> class
 *
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class ReadyForExperiment extends AbstractEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8392458646488015644L;
	
	/**
	 * Host name of the machine sending this Event
	 */
	private String hostName;

	/**
	 * @param priority
	 */
	public ReadyForExperiment(int priority) {
		super(priority);
	}

	/**
	 * @return the hostName
	 */
	public String getHostName() {
		return hostName;
	}

	/**
	 * @param hostName the hostName to set
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

}
