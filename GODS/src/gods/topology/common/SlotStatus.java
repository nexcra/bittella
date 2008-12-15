/**
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.topology.common;

/**
 * The <code>SlotStatus</code> is an enumerated class representing the status
 * of a virtual node(slot) in a Gods Experiment
 * 
 * @author Ozair Kafray
 * @version $Id: SlotStatus.java 258 2006-11-28 13:05:40Z cosmin $
 */
public enum SlotStatus {

	/**
	 * Just when the slots are booting up
	 */
	INITIALIZING,

	/**
	 * If a slot is busy in executing a task
	 */
	BUSY,

	/**
	 * If a slot is ready to run a task but not currently running one
	 */
	READY,

	/**
	 * Slot is not responding, might be overloaded or dead
	 */
	NOT_RESPONDING,
}
