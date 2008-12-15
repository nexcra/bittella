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
 * The <code>LaunchApplicationEvent</code> class extends the AbstractEvent
 * class for launching a Distributed System Under Test(Application).
 * {@link gods.arch.AbstractEvent}
 * 
 * @author Ozair Kafray
 * @version $Id: LaunchApplicationEvent.java 307 2007-02-20 15:29:10Z ozair $
 */
public class LaunchApplicationEvent extends AbstractEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2685338178194770111L;

	/**
	 * The script to launch the application and its arguments
	 */
	private String appLaunchCommand;

	/**
	 * Ids of the slots on which the application is to be launched.
	 */
	private int[] slotIds;

	/**
	 * Initially null. Args stores the arguments generated for launching
	 * instances of application equal to the numberOfInstances. The arguments
	 * need not be generated manually. To know more see
	 * {@link ArgumentGenerator}
	 */
	protected String[] arguments = null;

	/**
	 * Reference to the Argument Generator for this launch event
	 */
	// private ArgumentGenerator argumentGenerator = null;
	/**
	 * @param priority
	 */
	public LaunchApplicationEvent(int priority) {
		super(priority);
	}

	/**
	 * @return name of script for launching the application
	 */
	public String getAppLaunchCommand() {
		return appLaunchCommand;
	}

	/**
	 * @param appLaunchCommand
	 *            name of script for launching the application
	 */
	public void setAppLaunchCommand(String appLaunchCommand) {
		this.appLaunchCommand = appLaunchCommand;
	}

	/**
	 * @return Ids of the slots on which the application is to be launched.
	 */
	public int[] getSlotIds() {
		return slotIds;
	}

	/**
	 * @param slotIds
	 *            of the slots on which the application is to be launched.
	 */
	public void setSlotIds(int[] slotIds) {
		this.slotIds = slotIds;
	}

	/**
	 * @return the argGenerator This launch events
	 *         {@link gods.churn.ArgumentGenerator}
	 * 
	 * public ArgumentGenerator getArgumentGenerator() { return
	 * argumentGenerator; }
	 * 
	 * /**
	 * @param argGenerator
	 *            to set this launch events {@link ArgumentGenerator}
	 * 
	 * public void setArgumentGenerator(ArgumentGenerator argGenerator) {
	 * this.argumentGenerator = argGenerator; }
	 */

	/**
	 * @return Array of strings, each of which can be supplied as arguments for
	 *         the application to be launched by this event.
	 */
	public String[] getArguments() {
		return arguments;
	}

	/**
	 * @param arguments
	 *            to be passed to instances of application that are to be
	 *            launched
	 */
	public void setArguments(String[] arguments) {
		this.arguments = arguments;
	}
}
