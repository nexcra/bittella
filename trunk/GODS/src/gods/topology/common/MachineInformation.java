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

import java.io.Serializable;

/**
 * The <code>MachineInformation</code> class represents the information of a
 * machine in Gods experiment. An instance of this class is kept by each Gods
 * Agent for the machine it is running on.
 * 
 * @author Ozair Kafray
 * @version $Id: MachineInformation.java 337 2007-07-06 16:25:08Z ozair $
 */
public class MachineInformation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4876073201697490299L;

	/**
	 * {@link MachineStatus}
	 */
	private MachineStatus machineStatus;

	/**
	 * Id of the first Slot on this host machine.
	 */
	private int firstSlotId = 0;

	/**
	 * Id of the last Slot on this host machine.
	 */
	private int lastSlotId = 0;

	/**
	 * Number of free slots in the node
	 */
	private int numberOfFreeSlots = 0;

	/**
	 * Host Name of the node
	 */
	private String hostName;

	/**
	 * @param hostName
	 * @param agentName
	 */
	public MachineInformation(String hostName) {
		setHostName(hostName);
	}

	/**
	 * @param numberOfFreeSlots
	 *            the numberOfFreeSlots to set
	 */
	public void setNumberOfFreeSlots(int numberOfFreeSlots) {
		this.numberOfFreeSlots = numberOfFreeSlots;
	}

	/**
	 * @return the numberOfFreeSlots
	 */
	public int getNumberOfFreeSlots() {
		return numberOfFreeSlots;
	}

	/**
	 * @param hostName
	 *            the hostName to set
	 */
	private void setHostName(String hostName) {
		this.hostName = hostName;
	}

	/**
	 * @return the hostName
	 */
	public String getHostName() {
		return hostName;
	}

	/**
	 * @param machineStatus
	 *            the machineStatus to set
	 */
	public void setMachineStatus(MachineStatus nodeStatus) {
		this.machineStatus = nodeStatus;
	}

	/**
	 * @return the machineStatus
	 */
	public MachineStatus getMachineStatus() {
		return machineStatus;
	}

	/**
	 * @return the firstSlotId
	 */
	public int getFirstSlotId() {
		return firstSlotId;
	}

	/**
	 * @param firstSlotId
	 *            the firstSlotId to set
	 */
	public void setFirstSlotId(int firstSlotId) {
		this.firstSlotId = firstSlotId;
	}

	/**
	 * @return the lastSlotId
	 */
	public int getLastSlotId() {
		return lastSlotId;
	}

	/**
	 * @param lastSlotId
	 *            the lastSlotId to set
	 */
	public void setLastSlotId(int lastSlotId) {
		this.lastSlotId = lastSlotId;
	}
}
