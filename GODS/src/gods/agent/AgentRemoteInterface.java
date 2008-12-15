/**
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.agent;

import gods.arch.Event;
import gods.arch.Task;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The <code>AgentRemoteInterface</code> is a remote interface to an Agent
 * both for the ControlCenter and slots(virtual nodes).
 * 
 * @author Ozair Kafray
 * @version $Id: AgentRemoteInterface.java 258 2006-11-28 13:05:40Z cosmin $
 */
public interface AgentRemoteInterface extends Remote {
	/**
	 * This remote method executes the task sent by ControlCenter synchronously
	 * and returns the resultant Object.
	 * 
	 * @param Task
	 *            to be executed by Agent for ControlCenter
	 */
	public Object executeTask(Task t) throws RemoteException;

	/**
	 * Notification of an event from Dsut to Agent
	 */
	public void notifyDSUTEvent(/* SLOT_ID, METHOD_ID, TSTAMP */)
			throws RemoteException;

	/**
	 * This method is for notification of events to the Agent by ControlCenter.
	 * These events might also be considered as asynchronous tasks requested by
	 * ControlCenter.
	 * 
	 * @param e
	 *            is an event which is being notified to the Agent
	 * @throws RemoteException
	 */
	public void notifyEvent(Event e) throws RemoteException;

	/**
	 * The update state method is to be called by a Dsut on Agent to update its
	 * state and/or update to ControlCenter if required
	 */
	public void updateDSUTState(/*
								 * SLOT_ID, STATE_ID, STATE_OLD_VALUE,
								 * STATE_NEW_VALUE, TSTAMP
								 */) throws RemoteException;
}
