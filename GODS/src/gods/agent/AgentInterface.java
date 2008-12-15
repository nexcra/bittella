/**
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.agent;

import gods.arch.Event;
import gods.arch.Subscription;

import java.util.List;

/**
 * The <code>AgentInterface</code> The interface which specifies the methods
 * for communication amongst Agents external modules
 * 
 * @author Ozair Kafray
 * @version $Id: AgentInterface.java 258 2006-11-28 13:05:40Z cosmin $
 */
public interface AgentInterface {
	/**
	 * This method will be called by an external Agent module to enqueue an
	 * event in Agent
	 * 
	 * @param e -
	 *            Event to be enqueued
	 */
	public void enqueueEvent(Event e);

	/**
	 * This method is called by an external module to subscribe to various
	 * Agents internal events
	 * 
	 * @param module -
	 *            the subscriber module. Handlers for published events will be
	 *            enqueued in this module's queue
	 * @param events -
	 *            the set of events the module subscribes to
	 */
	public void subscribe(List<Subscription> subscriptions);
}
