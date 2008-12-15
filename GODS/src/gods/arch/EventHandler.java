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

import java.util.concurrent.Callable;

/**
 * The <code>EventHandler</code> class is an abstract class which needs to be
 * extended for handling any {@link gods.arch.Event}. It also implements the 
 * Callable and Comparable interfaces. EventHandlers are compared on the basis 
 * of priority. Also see {@link gods.arch.Subscription}
 * 
 * @author Ozair Kafray
 * @version $Id: EventHandler.java 378 2007-07-20 11:34:04Z ozair $
 */
public abstract class EventHandler implements Callable,
		Comparable<EventHandler> {

	/**
	 * The parameters required to construct an EventHandler
	 */
	public static Class[] handlerConstructorParameters = { Event.class,
			Module.class };
	
	/**
	 * The event occurence which is to be handled.
	 */
	protected Event event;

	/**
	 * The module that is processing the event
	 */
	protected Module module;

	/**
	 * Creating the general work to be done
	 */
	public EventHandler(Event event, Module module) {
		this.event = event;
		this.module = module;
	}

	/**
	 * @return Object that is the result of processing the event
	 * 
	 */
	public abstract Object handle() throws ClassCastException;

	/**
	 * This provides functionality redundant to handle so that the EventHandlers
	 * can also be executed by java Threads which only take Runnable and
	 * Callable type objects
	 * 
	 * @return Object that is the result of processing the event
	 */
	public Object call() {
		return handle();
	}

	/**
	 * Compares a EventHandler instance with this instance of EventHandler. The
	 * comparison is on the basis of event attribute of EventHandler.
	 * {@link gods.arch.AbstractEvent#compareTo(Event)}
	 */
	public int compareTo(EventHandler o) {
		int result = -1;

		if (event.getPriority() == o.getEvent().getPriority()) {
			result = 0;
		} else if (event.getPriority() < o.getEvent().getPriority()) {
			result = -1;
		} else {
			result = 1;
		}

		return result;
	}

	public Event getEvent() {
		return event;
	}

	public Module getModule() {
		return module;
	}

}
