/**
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.topology.cc;

import gods.agent.AgentRemoteInterface;
import gods.topology.common.MachineInformation;
import gods.topology.common.MachineStatus;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * The <code>AgentInformation</code> class represents an object for keeping
 * an Agent's information at the ControlCenter
 * 
 * @author Ozair Kafray
 * @version $Id: AgentInformation.java 258 2006-11-28 13:05:40Z cosmin $
 */
public class AgentInformation {

	/**
	 * Name of the agent as registered in rmiregistry
	 */
	private String agentName;

	/**
	 * A reference to the remote interface of the agent
	 */
	private AgentRemoteInterface agent = null;

	/**
	 * Information of the machine on which this agent is running
	 */
	private MachineInformation machineInformation = null;

	/**
	 * 
	 */
	public AgentInformation(String hostName, String agentName) {
		this.agentName = agentName;
		this.machineInformation = new MachineInformation(hostName);
	}

	public void findAgent() throws MalformedURLException, RemoteException,
			NotBoundException {
		agent = (AgentRemoteInterface) Naming.lookup(agentName);
	}

	/**
	 * @return the agent
	 */
	public AgentRemoteInterface getAgent() {
		return agent;
	}

	/**
	 * @return the agentName
	 */
	public String getAgentName() {
		return agentName;
	}

	/**
	 * @param agentName
	 *            the agentName to set
	 */
	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	/**
	 * @return {@link gods.topology.common.MachineInformation#getFirstSlotId()}
	 */
	public int getFirstSlotId() {
		return machineInformation.getFirstSlotId();
	}

	/**
	 * @return {@link gods.topology.common.MachineInformation#getHostName()}
	 */
	public String getHostName() {
		return machineInformation.getHostName();
	}

	/**
	 * @return {@link gods.topology.common.MachineInformation#getLastSlotId()}
	 */
	public int getLastSlotId() {
		return machineInformation.getLastSlotId();
	}

	/**
	 * @return {@link gods.topology.common.MachineInformation#getMachineStatus()}
	 */
	public MachineStatus getAgentStatus() {
		return machineInformation.getMachineStatus();
	}

	/**
	 * @return {@link gods.topology.common.MachineInformation#getNumberOfFreeSlots()}
	 */
	public int getNumberOfFreeSlots() {
		return machineInformation.getNumberOfFreeSlots();
	}

	/**
	 * @param firstSlotId
	 *            {@link gods.topology.common.MachineInformation#setFirstSlotId(int)}
	 */
	public void setFirstSlotId(int firstSlotId) {
		machineInformation.setFirstSlotId(firstSlotId);
	}

	/**
	 * @param lastSlotId
	 *            {@link gods.topology.common.MachineInformation#setLastSlotId(int)}
	 */
	public void setLastSlotId(int lastSlotId) {
		machineInformation.setLastSlotId(lastSlotId);
	}

	/**
	 * @param machineStatus
	 *            {@link gods.topology.common.MachineInformation#setMachineStatus(gods.topology.common.MachineStatus)}
	 */
	public void setAgentStatus(MachineStatus status) {
		machineInformation.setMachineStatus(status);
	}

	/**
	 * @param numberOfFreeSlots
	 *            {@link gods.topology.common.MachineInformation#setNumberOfFreeSlots(int)}
	 */
	public void setNumberOfFreeSlots(int numberOfFreeSlots) {
		machineInformation.setNumberOfFreeSlots(numberOfFreeSlots);
	}
}
