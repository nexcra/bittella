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
 * The <code>EventTopic</code> is a class to categorize or group events. This
 * class is not in use currently.
 * 
 * @author Ozair Kafray
 * @version $Id: EventTopic.java 258 2006-11-28 13:05:40Z cosmin $
 */
public class EventTopic implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5550166258914810466L;

	/**
	 * * Overrides toString from Object to return the topic in EventTopic.
	 * {@link java.lang.Object#toString()}
	 */
	@Override
	public String toString() {
		return this.topic;
	}

	/**
	 * The event topic or group.
	 */
	String topic;

	/**
	 * Description of the topic
	 */
	String description;

	/**
	 * Constructs a new event topic with the given attributes
	 * 
	 * @param topic
	 *            the event topic
	 * @param description
	 *            the event topic description
	 */
	public EventTopic(String topic, String description) {
		super();
		this.topic = topic;
		this.description = description;
	}
}
