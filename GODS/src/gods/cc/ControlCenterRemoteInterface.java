/**
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.cc;

import gods.arch.Event;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The <code>ControlCenterRemoteInterface</code> is an interface to the
 * ControlCenter for GODS' Remote Agents
 * 
 * @author Ozair Kafray
 * @version $Id: ControlCenterRemoteInterface.java 258 2006-11-28 13:05:40Z cosmin $
 */
public interface ControlCenterRemoteInterface extends Remote {

	/**
	 * Called by an Agent to notify the ControlCenter of an Event.
	 * 
	 * @param e
	 *            Event of which the Control Center is to be notified
	 */
	public void notifyEvent(Event e) throws RemoteException;

	/**
	 * Is to be called on Control Center Remote Object by an Agent to update its
	 * state
	 */
	public void updateState(/* Data d */) throws RemoteException;

}
