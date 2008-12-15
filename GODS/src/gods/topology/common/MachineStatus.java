/**
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.topology.common;

/**
 * The <code>MachineStatus</code> is an enumerated class representing the
 * status of a machine running Gods Agents
 * 
 * @author Ozair Kafray
 * @version $Id: MachineStatus.java 258 2006-11-28 13:05:40Z cosmin $
 */
public enum MachineStatus {

	/**
	 * A start agent call has been made to the agent but it has not responded
	 * yet with a JoinEvent
	 */
	INITIALIZING,

	/**
	 * An agent has been started on a machine and it has responded.
	 */
	JOINED,

	/**
	 * When an agent on machine has collected initial information from its slots
	 * and responded to 'cc' with a ReadyEvent
	 */
	READY,

	/**
	 * Machine is not responding, might be overloaded or dead
	 */
	NOT_RESPONDING,
}
