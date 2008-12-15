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
 * The <code>BootEvent</code> is the first event created by the bootstrapper
 * (gods.Gods) and enqueued in the ControlCenter, which makes it to deploy Gods
 * and get ready for experiments
 * 
 * @author Ozair Kafray
 * @version $Id: BootEvent.java 258 2006-11-28 13:05:40Z cosmin $
 */
public class BootEvent extends AbstractEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4593526313675856230L;

	/**
	 * Boolean parameter specifying whether agents should be automatically
	 * deployed during boot and started or this process will be done manually
	 */
	private boolean autoStartAgents;

	/**
	 * @param eventType
	 * @param priority
	 */
	public BootEvent(int priority) {
		super(priority);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the autoStartAgents
	 */
	public boolean isAutoStartAgents() {
		return autoStartAgents;
	}

	/**
	 * @param autoStartAgents
	 *            the autoStartAgents to set
	 */
	public void setAutoStartAgents(boolean autoStartAgents) {
		this.autoStartAgents = autoStartAgents;
	}
}
