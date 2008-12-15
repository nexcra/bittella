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
 * The <code>JoinedEvent</code> class represents an Event sent by an Agent who
 * has just joined to the ControlCenter
 * 
 * @author Ozair Kafray
 * @version $Id: JoinedEvent.java 258 2006-11-28 13:05:40Z cosmin $
 */
public class JoinedEvent extends AbstractEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6566069872280673269L;

	/**
	 * Hostname of the machine on which the Agent sending this event is running
	 */
	private String hostName;

	/**
	 * @param eventType
	 * @param priority
	 */
	public JoinedEvent(int priority) {
		super(priority);
	}

	/**
	 * @param hostName
	 *            the hostName to set
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	/**
	 * @return the hostName
	 */
	public String getHostName() {
		return hostName;
	}

}
