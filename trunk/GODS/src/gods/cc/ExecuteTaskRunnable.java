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
import gods.arch.Task;

import java.rmi.RemoteException;

/**
 * The <code>SuncClocksRunnable</code> class
 *
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class ExecuteTaskRunnable implements Runnable {

	/**
	 * 
	 */
	private AgentRemoteInterface agent;
	
	/**
	 * 
	 */
	private Task task;
	
	/**
	 * 
	 */
	public ExecuteTaskRunnable() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try{
		agent.executeTask(task);
		}catch(RemoteException re){
			re.getMessage();
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
	 * @return the task
	 */
	public Task getTask() {
		return task;
	}

	/**
	 * @param task the task to set
	 */
	public void setTask(Task task) {
		this.task = task;
	}

}
