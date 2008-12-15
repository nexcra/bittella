/**
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.cc;

/**
 * The <code>GodsStatus</code> is an enumerated class for representing the
 * status of a GODS experiment any time during an experiment.
 * 
 * @author Ozair Kafray
 * @version $Id: GodsStatus.java 258 2006-11-28 13:05:40Z cosmin $
 */
public enum GodsStatus {

	/**
	 * Initial State of Gods i.e., while boot strapping
	 */
	INITIALIZING,

	/**
	 * When agents are joining
	 */
	JOINING,

	/**
	 * When agents are collectitng slots information at initialization
	 */
	PREPARING,

	/**
	 * Gods is ready to conduct experiment
	 */
	READY

}
