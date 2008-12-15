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

import static gods.Gods.bindRemoteObject;

import gods.GodsExitCodes;
import gods.arch.Event;
import gods.arch.Task;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.Logger;

/**
 * The <code>AgentRemote</code> is a remote object type for the
 * AgentRemoteInterface. This class provides functionality requested by
 * ControlCenter and Slots.
 * 
 * @author Ozair Kafray
 * @version $Id: AgentRemote.java 396 2007-08-01 14:45:00Z ozair $
 */
public class AgentRemote extends UnicastRemoteObject implements
		AgentRemoteInterface {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger.getLogger(AgentRemote.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 2497584195117919237L;

	/**
	 * @throws RemoteException
	 */
	public AgentRemote() throws RemoteException {

	}

	/**
	 * Binds the agent with its name and remote reference in the RMI registry
	 * 
	 * @throws RemoteException
	 * @throws MalformedURLException
	 */
	public void bind() {

		// Binding Remote Object with its name
		log.debug("=====================================");
		log.debug("Going to bind Agent with name: "
				+ Agent.getInstance().getAgentName());

		try {

			bindRemoteObject(Agent.getInstance().getAgentName(), this);
			
			log.info("Agent bound with name:"
					+ Agent.getInstance().getAgentName());
			log.debug("=====================================");
			
		} catch (MalformedURLException mfue) {
			log.debug(mfue.getMessage());
			System.exit(GodsExitCodes.REMOTE_OBJECT_REGISTRATION_ERROR);
		}
	}

	/**
	 * {@link gods.agent.Agent#executeTask(gods.common.tasks.Task)}
	 */
	public Object executeTask(Task t) throws RemoteException {
		return t.execute();
	}

	/**
	 * {@link gods.agent.AgentRemoteInterface#notifyEvent(gods.arch.Event)}
	 */
	public void notifyEvent(Event e) throws RemoteException {
		Agent.getInstance().enqueueEvent(e);
	}

	/**
	 * {@link gods.agent.AgentRemoteInterface#updateDSUTState()}
	 */
	public void updateDSUTState(/* Data d */) throws RemoteException {

	}

	/**
	 * {@link gods.agent.AgentRemoteInterface#notifyDSUTEvent()}
	 */
	public void notifyDSUTEvent(/* Event e */) throws RemoteException {

	}
}
