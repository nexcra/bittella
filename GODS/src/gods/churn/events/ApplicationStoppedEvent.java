/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.churn.events;

import gods.arch.AbstractEvent;

/**
 * The <code>ApplicationStoppedEvent</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class ApplicationStoppedEvent extends AbstractEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3499570061876960743L;

	/**
	 * The id of the stopped process
	 */
	private int processIds[];

	/**
	 * Id of the slot on which this application was running
	 */
	private int slotIds[];

	/**
	 * @param priority
	 */
	public ApplicationStoppedEvent(int priority) {
		super(priority);
	}

	/**
	 * @return the processId of the stopped application
	 */
	public int[] getProcessIds() {
		return processIds;
	}

	/**
	 * @param processId
	 *            of the stopped application
	 */
	public void setProcessIds(int processIds[]) {
		this.processIds = processIds;
	}

	/**
	 * @return the slotId of the slot on which the stopped process was running
	 */
	public int[] getSlotIds() {
		return slotIds;
	}

	/**
	 * @param slotId
	 *            of the slot on which the stopped process was running
	 */
	public void setSlotIds(int slotIds[]) {
		this.slotIds = slotIds;
	}

}
