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
 * The <code>JoinExperimentEvent</code> class
 *
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class JoinExperimentEvent extends AbstractChurnExperimentEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3350407998399410727L;
	
	/**
	 * Command line arguments for this instance of application node 
	 */
	private String appArgs = null;

	/**
	 * Default Constructor
	 */
	public JoinExperimentEvent() {
		
	}
	
	/* (non-Javadoc)
	 * {@link java.lang.Object#toString()}
	 */
	@Override
	public String toString() {
		return getTimeToHappen().toString()
		+ String.format("\t%12d J", vnid);
	}

	/**
	 * @return the appArgs
	 */
	public String getAppArgs() {
		return appArgs;
	}

	/**
	 * @param appArgs the appArgs to set
	 */
	public void setAppArgs(String appArgs) {
		this.appArgs = appArgs;
	}

}
