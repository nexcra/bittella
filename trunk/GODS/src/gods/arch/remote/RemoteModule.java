/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.arch.remote;

import gods.arch.Event;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The <code>RemoteModule</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public interface RemoteModule extends Remote {

	/**
	 * This method provides an interface for the ControlCenter to notify of an
	 * Event to a RemoteModule. A class providing its implementation must create
	 * the EventHandler corresponding to this Events subscription and enqueue it
	 * in the actual implementation of RemoteModule
	 * 
	 * @param e
	 *            Event of interest
	 */
	public void notifyEvent(Event e) throws RemoteException;

	/**
	 * @return A reference to the proxy of this module in ControlCenter
	 */
	public RemoteModuleProxy getRemoteModuleProxy() throws RemoteException;

	/**
	 * @param proxy
	 *            for this RemoteModule at the ControlCenter
	 */
	public void setRemoteModuleProxy(RemoteModuleProxy proxy)
			throws RemoteException;
}
