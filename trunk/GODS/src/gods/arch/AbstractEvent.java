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

import gods.arch.EventTopic;

/**
 * The <code>AbstractEvent</code> class is an abstract implementation of the
 * {@link gods.arch.Event} interface. Any Event class should be extended from
 * this class.
 * 
 * @author Ozair Kafray
 * @version $Id: AbstractEvent.java 375 2007-07-20 09:08:21Z ozair $
 */
public abstract class AbstractEvent implements Event {

	/**
	 * An integer representing the priority of this event. Lower number has
	 * higher priority.
	 */
	private int priority;

	/**
	 * An instance of EventTopic, used to categorize events into groups. This
	 * variable is not being currently used but can be used later for
	 * subscribing whole categories of Events instead of single events.
	 */
	private EventTopic eventTopic;

	/**
	 * @param eventType
	 * @param priority
	 */
	public AbstractEvent(int priority) {
		this.priority = priority;
	}

	/**
	 * Compares a AbstractEvent instance with this instance of AbstractEvent.
	 * The comparison is on the basis of priority attribute of AbstractEvent.
	 * {@link java.lang.Comparable#compareTo(java.lang.Object)}
	 */
	public int compareTo(Event o) {
		int result = 0;

		if (priority < o.getPriority()) {
			result = -1;
		} else if(priority > o.getPriority()) {
			result = 1;
		}

		return result;
	}

	/**
	 * @param priority
	 *            the priority to set
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}

	/**
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * @return the eventTopic
	 */
	public EventTopic getEventTopic() {
		return eventTopic;
	}

	/**
	 * @param eventTopic
	 *            the eventTopic to set
	 */
	public void setEventTopic(EventTopic eventTopic) {
		this.eventTopic = eventTopic;
	}

}
