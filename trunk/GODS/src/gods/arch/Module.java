/**
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.arch;

/**
 * The <code>Module</code> interface represents an object encapsulating state,
 * to be modified by a set of events to which the module subscribes.
 * 
 * @author Ozair Kafray
 * @version $Id: Module.java 258 2006-11-28 13:05:40Z cosmin $
 */
public interface Module {
	/**
	 * call this method to add work for related to an event
	 * 
	 * @param event -
	 *            the work to be enqueued
	 */
	public void enqueue(EventHandler h);
}
