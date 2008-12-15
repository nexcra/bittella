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
 * The <code>SyncClocks</code> class
 *
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class SyncClocks extends AbstractEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3151384716999160220L;

	/**
	 * @param priority
	 */
	public SyncClocks(int priority) {
		super(priority);
		// TODO Auto-generated constructor stub
	}

}
