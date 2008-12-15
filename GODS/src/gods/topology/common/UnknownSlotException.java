/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.topology.common;

/**
 * The <code>UnknownSlotException</code> is an exception to signify that the
 * slotId is not known to the callee (Agent or ControlCenter)
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class UnknownSlotException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7469788359546720458L;

	/**
	 * 
	 */
	public UnknownSlotException() {
	}

	/**
	 * @param message
	 */
	public UnknownSlotException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public UnknownSlotException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public UnknownSlotException(String message, Throwable cause) {
		super(message, cause);
	}

}
