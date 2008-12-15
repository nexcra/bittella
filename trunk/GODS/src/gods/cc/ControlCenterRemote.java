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

import static gods.Gods.bindRemoteObject;

import gods.GodsExitCodes;
import gods.arch.Event;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;

/**
 * The <code>ControlCenterRemote</code> is a remote object type for the
 * ControlCenterRemoteInterface. This class provides functionality requested by
 * agents.
 * 
 * @author Ozair Kafray
 * @version $Id: ControlCenterRemote.java 396 2007-08-01 14:45:00Z ozair $
 */
public class ControlCenterRemote extends UnicastRemoteObject implements
		ControlCenterRemoteInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 338642912176232224L;

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger.getLogger(ControlCenter.class);

	/**
	 * Binds the ControlCenter with its name and remote reference in the RMI
	 * registry
	 * 
	 * @param ccName
	 *            name with which the ControlCenter is to be bounded
	 * @throws RemoteException
	 * @throws MalformedURLException
	 */
	public void bind(String ccName) {

		try {
			// Binding Control Center Remote Object with its name
			bindRemoteObject(ccName, this);
			log.info("ControlCenter bound with name:" + ccName);
			
		} catch (MalformedURLException mfue) {
			log.error(mfue.getMessage());
			System.exit(GodsExitCodes.REMOTE_OBJECT_REGISTRATION_ERROR);
		}

	}

	/**
	 * @throws RemoteException
	 */
	public ControlCenterRemote() throws RemoteException {

	}

	/**
	 * {@link gods.cc.ControlCenterRemoteInterface#notifyEvent(gods.arch.Event)}
	 */
	public void notifyEvent(Event e) throws RemoteException {
		ControlCenter.getInstance().enqueueEvent(e);
	}

	/**
	 * {@link gods.cc.ControlCenterRemoteInterface#updateState()}
	 */
	public void updateState() throws RemoteException {

	}

}
