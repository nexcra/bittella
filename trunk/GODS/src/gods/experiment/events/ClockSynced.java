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
 * The <code>ClockSynced</code> class
 *
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class ClockSynced extends AbstractEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5035546179824588378L;

	/**
	 * @param priority
	 */
	public ClockSynced(int priority) {
		super(priority);
		// TODO Auto-generated constructor stub
	}

}
