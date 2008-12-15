/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.topology.events;

import gods.arch.AbstractEvent;
import gods.topology.common.SlotInformation;

/**
 * The <code>SlotInformationChanged</code> is an event sent by Agents to
 * ControlCenter whenever there is a change in the attributes of a slot on their
 * host machine.
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class SlotInformationChanged extends AbstractEvent {

	/**
	 * The information about all slots whose attributes have changed.
	 */
	private SlotInformation[] changedSlots;

	/**
	 * 
	 */
	private static final long serialVersionUID = 3364150175481333354L;

	/**
	 * @param priority
	 */
	public SlotInformationChanged(int priority) {
		super(priority);
	}

	/**
	 * @return the changedSlots
	 */
	public SlotInformation[] getChangedSlots() {
		return changedSlots;
	}

	/**
	 * @param changedSlots the changedSlots to set
	 */
	public void setChangedSlots(SlotInformation[] changedSlots) {
		this.changedSlots = changedSlots;
	}

}
