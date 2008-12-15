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
 * The <code>ApplicationLaunchedEvent</code> is an event scheduled when an
 * application is launched on virtual node(s). A single instance of this class
 * represents the launch of a single application on 1-* virtual nodes of a
 * single machine.
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class ApplicationLaunchedEvent extends AbstractEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3872954974662992362L;

	/**
	 * Ids of the slots on which the application is launched
	 */
	private int[] slotIds = null;

	/**
	 * The name of the script that launched the application
	 */
	private String appLaunchCommand = null;

	/**
	 * The processIds of the applications launched
	 */
	private int[] processIds = null;

	/**
	 * @param priority
	 */
	public ApplicationLaunchedEvent(int priority) {
		super(priority);
	}

	/**
	 * @return name of the script that launched the application
	 */
	public String getAppLaunchCommand() {
		return appLaunchCommand;
	}

	/**
	 * @param set
	 *            name of the script that launched the application
	 */
	public void setAppLaunchCommand(String javaArguments) {
		this.appLaunchCommand = javaArguments;
	}

	/**
	 * @return the processId of the application launched. The processIds
	 *         correspond with slotIds of same indices in slotIds array.
	 */
	public int[] getProcessIds() {
		return processIds;
	}

	/**
	 * @param to
	 *            set the processId of the application launched. The processIds
	 *            correspond with slotIds of same indices in slotIds array.
	 */
	public void setProcessIds(int processIds[]) {
		this.processIds = processIds;
	}

	/**
	 * @return the Ids of the slots on which the application is launched. The
	 *         slotIds correspond with processIds of same indices in processIds
	 *         array.
	 */
	public int[] getSlotIds() {
		return slotIds;
	}

	/**
	 * @param slotId
	 *            to set the slotIds of slots on which an application is
	 *            launched. The slotIds correspond with processIds of same
	 *            indices in processIds array.
	 */
	public void setSlotIds(int[] slotIds) {
		this.slotIds = slotIds;
	}

}
