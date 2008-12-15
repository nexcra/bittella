/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.cc;

import gods.agent.AgentRemoteInterface;
import gods.arch.Event;

import java.rmi.RemoteException;

/**
 * The <code>ExecuteExperimentRunnable</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class NotifyAgentRunnable implements Runnable {

	/**
	 * 
	 */
	private AgentRemoteInterface agent;

	/**
	 * 
	 */
	private Event event;

	/**
	 * 
	 */
	public NotifyAgentRunnable() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			agent.notifyEvent(event);
			
		} catch (RemoteException re) {
			System.out.println(re.getMessage());
		}

	}

	/**
	 * @return the agent
	 */
	public AgentRemoteInterface getAgent() {
		return agent;
	}

	/**
	 * @param agent the agent to set
	 */
	public void setAgent(AgentRemoteInterface agent) {
		this.agent = agent;
	}

	/**
	 * @return the event
	 */
	public Event getEvent() {
		return event;
	}

	/**
	 * @param event the event to set
	 */
	public void setEvent(Event event) {
		this.event = event;
	}

}
