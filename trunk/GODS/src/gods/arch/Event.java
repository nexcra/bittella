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

import java.io.Serializable;

/**
 * The <code>Event</code> interface represents an Event in Gods that can be shared
 * among Gods modules. It implements the Serializable and Comparable interfaces.
 * Comparison of events is based on their priority.
 * 
 * @author Ozair Kafray
 * @version $Id: Event.java 318 2007-05-02 12:25:22Z ozair $
 */
public interface Event extends Serializable, Comparable<Event> {

	/**
	 * @return the topic of the event
	 */
	public EventTopic getEventTopic();

	/**
	 * @return int priority with which the event should be handled
	 */
	public int getPriority();
}
