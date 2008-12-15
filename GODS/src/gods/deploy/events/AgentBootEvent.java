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
 * The <code>AgentBootEvent</code> class signifies the start of booting
 * sequence for agents.
 * 
 * @author Ozair Kafray
 * @version $Id: AgentBootEvent.java 258 2006-11-28 13:05:40Z cosmin $
 */
public class AgentBootEvent extends AbstractEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8442478963412359226L;

	/**
	 * Hostname of the machine on which ControlCenter is running. This
	 * information is required by an agent to connect to Control Center Remote
	 * Object.
	 */
	private String ccHostName;

	/**
	 * @param priority
	 */
	public AgentBootEvent(int priority) {
		super(priority);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the ccHostName
	 */
	public String getCcHostName() {
		return ccHostName;
	}

	/**
	 * @param ccHostName
	 *            the ccHostName to set
	 */
	public void setCcHostName(String ccHostName) {
		this.ccHostName = ccHostName;
	}

}
