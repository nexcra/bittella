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
 * The <code>KillApplicationEvent</code> class extends the AbstractEvent class
 * for killing a Distributed System Under Test(Application).
 * {@link gods.arch.AbstractEvent}
 * 
 * @author Ozair Kafray
 * @version $Id: KillApplicationEvent.java 292 2007-01-30 15:35:40Z ozair $
 */
public class KillApplicationEvent extends AbstractEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -997810432067197900L;

	/**
	 * The id of the process to be killed
	 */
	private int processIds[];

	/**
	 * Id of the slot on which this application is running
	 */
	private int slotIds[];

	/**
	 * @param priority
	 */
	public KillApplicationEvent(int priority) {
		super(priority);
	}

	/**
	 * @return the processId of the process to be killed
	 */
	public int[] getProcessIds() {
		return processIds;
	}

	/**
	 * @param processId
	 *            to set the processId of the process to be killed
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
