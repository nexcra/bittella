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
 * The <code>Subscription</code> class represents a single event subscription
 * which includes the event, its handler and the module subscribing the event.
 * 
 * @author Ozair Kafray
 * @version $Id: Subscription.java 258 2006-11-28 13:05:40Z cosmin $
 */
public class Subscription {

	/**
	 * The module which has subscribed to this event
	 */
	private Module module;

	/**
	 * The subscribed event class.
	 */
	private Class event;

	/**
	 * Class that implements the handler for the event.
	 */
	private Class eventHandler;

	/**
	 * A subscription is made by the object of the component that is responsible
	 * for that subscription and the method to be called for handling that
	 * event, plus the vent itself
	 */
	public Subscription(Module module, Class event, Class eventHandler) {
		this.module = module;
		this.eventHandler = eventHandler;
		this.event = event;
	}

	/**
	 * @return the Event class that is subscribed in this Subscription
	 */
	public Class getEventClass() {
		return event;
	}

	/**
	 * @return the Event Handler Class for this subscription
	 */
	public Class getEventHandlerClass() {
		return eventHandler;
	}

	/**
	 * @return the Module which has subscribed to the event in this Subscription
	 */
	public Module getModule() {
		return module;
	}
}
