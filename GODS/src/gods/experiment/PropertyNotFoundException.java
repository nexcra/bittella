/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.experiment;

/**
 * The <code>PropertyNotFoundException</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class PropertyNotFoundException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5257440306591056764L;

	/**
	 * 
	 */
	public PropertyNotFoundException() {

	}

	/**
	 * @param message
	 */
	public PropertyNotFoundException(String key, String fileName) {
		super("Property: " + key + " not found in properties file: " + fileName);
	}

	/**
	 * @param message
	 */
	public PropertyNotFoundException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public PropertyNotFoundException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public PropertyNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

}
