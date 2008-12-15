/**
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.deploy.events;

import gods.arch.AbstractEvent;
import gods.topology.common.SlotInformation;

import java.lang.NullPointerException;

/**
 * The <code>ReadyEvent</code> class represents an Event sent by an Agent to
 * the ControlCenter to signify that it is ready for conducting experiments with
 * an array of slots
 * 
 * @author Ozair Kafray
 * @version $Id: ReadyEvent.java 258 2006-11-28 13:05:40Z cosmin $
 */
public class ReadyEvent extends AbstractEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7548082790740951320L;

	/**
	 * Host name of the machine sending this Event
	 */
	private String hostName;

	/**
	 * An array of slots on the machine sending this Event
	 */
	private SlotInformation[] slotsInformation = null;

	/**
	 * @param priority
	 *            of the Event
	 * @param slotsInformation
	 *            information of the on the machine sending this Event
	 */
	public ReadyEvent(int priority, SlotInformation[] slotsInformation)
			throws NullPointerException {
		super(priority);
		if (slotsInformation != null) {
			this.slotsInformation = slotsInformation;
		} else {
			throw new NullPointerException("Slots Information cannot be null");
		}
	}

	/**
	 * @return the slotsInformation
	 */
	public SlotInformation[] getSlotsInformation() {
		return slotsInformation;
	}

	/**
	 * @return the hostName
	 */
	public String getHostName() {
		return hostName;
	}

	/**
	 * @param hostName
	 *            the hostName to set
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
}
