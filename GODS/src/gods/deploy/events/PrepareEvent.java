/**
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.deploy.events;

import gods.arch.AbstractEvent;

/**
 * The <code>PrepareEvent</code> class represetns an Event sent by the
 * ControlCenter during deployment to the Agent to prepare for an experiment
 * after the Agent has send the JoinedEvent.
 * 
 * @author Ozair Kafray
 * @version $Id: PrepareEvent.java 258 2006-11-28 13:05:40Z cosmin $
 */
public class PrepareEvent extends AbstractEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2130400424512883548L;

	/**
	 * This array contains the first and last ids that are to be assigned by the
	 * recepient machine to its virtual nodes, in its first and second indices
	 * respectively
	 */
	private int[] slotsRange = new int[2];

	/**
	 * @param eventType
	 * @param priority
	 */
	public PrepareEvent(int priority) {
		super(priority);
	}

	/**
	 * @return
	 */
	public int[] getSlotsRange() {
		return slotsRange;
	}

	/**
	 * @param slotsRange
	 */
	public void setSlotsRange(int[] slotsRange) {
		this.slotsRange = slotsRange;
	}

}
