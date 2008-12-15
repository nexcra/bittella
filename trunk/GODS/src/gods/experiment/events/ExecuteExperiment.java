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
 * The <code>StartExperiment</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class ExecuteExperiment extends AbstractEvent {

	/**
	 * Path to the script for launching application under test
	 */
	private String appLaunchScript;

	/**
	 * 
	 */
	private static final long serialVersionUID = -8787682905332014795L;

	/**
	 * @param priority
	 */
	public ExecuteExperiment(int priority) {
		super(priority);
	}

	/**
	 * @return the appLaunchScript
	 */
	public String getAppLaunchScript() {
		return appLaunchScript;
	}

	/**
	 * @param appLaunchScript the appLaunchScript to set
	 */
	public void setAppLaunchScript(String appLaunchScript) {
		this.appLaunchScript = appLaunchScript;
	}

}
