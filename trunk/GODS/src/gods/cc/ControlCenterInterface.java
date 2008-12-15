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

import gods.arch.Event;
import gods.arch.Subscription;

import java.util.List;

/**
 * The interface which specifies the methods for communication amongst
 * ControlCenters external modules
 * 
 * @author Ozair Kafray
 * @version $Id: ControlCenterInterface.java 258 2006-11-28 13:05:40Z cosmin $
 */
public interface ControlCenterInterface {

	/**
	 * This method will be called by an external module to enqueue an internal
	 * event in ControlCenter
	 * 
	 * @param e -
	 *            Event to be enqueued
	 */
	public void enqueueEvent(Event e);

	/**
	 * This method is called by an external module to subscribe to various
	 * Control Center internal events
	 * 
	 * @param module -
	 *            the subscriber module. Published events will be enqueued in
	 *            this module's queue
	 * @param events -
	 *            the set of events the module subscribes to
	 */
	public void subscribe(List<Subscription> subscriptions);
}
