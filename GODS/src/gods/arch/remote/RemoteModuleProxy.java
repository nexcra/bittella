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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * The <code>RemoteModuleProxy</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public interface RemoteModuleProxy extends Remote {

	/**
	 * This function provides an interface for the RemoteModule to subscribe to
	 * ControlCenter events. The proxy will subscribe to each Event with
	 * RemoteEventHandler. The handler will then notify of this Event to the
	 * RemoteModule.
	 * 
	 * @param events
	 * @throws RemoteException
	 */
	public void subscribe(List<Class> events) throws RemoteException;

	/**
	 * @return A reference to the remote module
	 * @throws RemoteException
	 */
	public RemoteModule getRemoteModule() throws RemoteException;
	
	/**
	 * @param module Remote Module to which this proxy belongs
	 */
	public void setRemoteModule(RemoteModule module) throws RemoteException;

}
