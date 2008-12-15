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
 * The <code>SlotInformation</code> class represents the information of a slot
 * in Gods experiment. Each Slot in the Gods experiment is represented by an
 * instance of this class.
 * 
 * @author Ozair Kafray
 * @version $Id: SlotInformation.java 292 2007-01-30 15:35:40Z ozair $
 */
public class SlotInformation implements Serializable,
		Comparable<SlotInformation> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8644315707915926527L;

	/**
	 * A unique integer id of every slot assigned while initializing by Agent
	 * between the range provided to it by ControlCenter
	 */
	private int slotId;

	/**
	 * Hostname of the machine on which this slot is being emulated
	 */
	private String hostName;

	/**
	 * Virtual IP of the slot
	 */
	private String virtualNodeAddress;

	/**
	 * The status of the slot
	 */
	private SlotStatus slotStatus = SlotStatus.INITIALIZING;

	/**
	 * The command being executed on the slot
	 */
	private String command = null;
	
	/**
	 * The id of the process running on this slot.
	 */
	private int processId = 0;

	/**
	 * @param nodeAddress
	 * @param virtualNodeAddress
	 */
	public SlotInformation(int slotId, String hostName,
			String virtualNodeAddress) {

		this.slotId = slotId;
		this.hostName = hostName;
		this.virtualNodeAddress = virtualNodeAddress;
	}

	/**
	 * Compares a SlotInformation instance with this instance of
	 * SlotInformation. The comparison is on the basis of slotId attribute of
	 * SlotInformation {@link java.lang.Comparable#compareTo(java.lang.Object)}
	 */
	public int compareTo(SlotInformation o) {
		int result = 1;

		if (slotId == o.getSlotId()) {
			result = 0;
		} else if (slotId < o.getSlotId()) {
			result = -1;
		} else {
			result = 1;
		}

		return result;
	}

	/**
	 * @param hostName
	 *            of the machine on which this slot is located
	 * 
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	/**
	 * @return hostName of the machine on which this slot is located
	 */
	public String getHostName() {
		return hostName;
	}

	/**
	 * @param virtualNodeAddress
	 *            the virtualNodeAddress to set
	 */
	public void setVirtNodeAddress(String virtualNodeAddress) {
		this.virtualNodeAddress = virtualNodeAddress;
	}

	/**
	 * @return the virtualNodeAddress
	 */
	public String getVirtNodeAddress() {
		return virtualNodeAddress;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setSlotStatus(SlotStatus slotStatus) {
		this.slotStatus = slotStatus;
	}

	/**
	 * @return the status
	 */
	public SlotStatus getSlotStatus() {
		return slotStatus;
	}

	/**
	 * @param command
	 *            the command to set
	 */
	public void setCommand(String command) {
		this.command = command;
	}

	/**
	 * @return the command
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * @param slotId
	 *            the slotId to set
	 */
	public void setSlotId(int slotId) {
		this.slotId = slotId;
	}

	/**
	 * @return the slotId
	 */
	public int getSlotId() {
		return slotId;
	}

	/**
	 * Converts the state of this slot into a String formatted as
	 * SlotId:VNodeAddress
	 */
	@Override
	public String toString() {
		return "" + slotId + " :    " + virtualNodeAddress;
	}

	/**
	 * @return the processId
	 */
	public int getProcessId() {
		return processId;
	}

	/**
	 * @param processId the processId to set
	 */
	public void setProcessId(int processId) {
		this.processId = processId;
	}

	/**
	 * @return the virtualNodeAddress
	 */
	public String getVirtualNodeAddress() {
		return virtualNodeAddress;
	}
}
