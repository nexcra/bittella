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
 * The <code>StopApplicationEvent</code> class extends the AbstractEvent class
 * for stopping a Distributed System Under Test(Application)
 * {@link gods.arch.AbstractEvent}
 * 
 * @author Ozair Kafray
 * @version $Id: StopApplicationEvent.java 292 2007-01-30 15:35:40Z ozair $
 */
public class StopApplicationEvent extends AbstractEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5153062762359567223L;

	/**
	 * The id of the virtual node process to be interrupted
	 */
	private int processIds[];

	/**
	 * Id of the slot on which this application is running
	 */
	private int slotIds[];

	/**
	 * @param priority
	 */
	public StopApplicationEvent(int priority) {
		super(priority);

	}

	/**
	 * @return the processId of the process to be interrupted
	 */
	public int[] getProcessIds() {
		return processIds;
	}

	/**
	 * @param processId
	 *            to set the processId of the process to be interrupted
	 */
	public void setProcessIds(int processIds[]) {
		this.processIds = processIds;
	}

	/**
	 * @return Id of the slot on which this application is running
	 */
	public int[] getSlotIds() {
		return slotIds;
	}

	/**
	 * @param Id
	 *            of the slot on which this application is running
	 */
	public void setSlotIds(int slotIds[]) {
		this.slotIds = slotIds;
	}
}
