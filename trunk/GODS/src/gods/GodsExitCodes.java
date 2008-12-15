/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods;

/**
 * The <code>GodsErrorCodes</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public interface GodsExitCodes {

	/**
	 * If Gods or any one of its components exits due to errors related to
	 * registration of remote objects
	 */
	public static final int REMOTE_OBJECT_REGISTRATION_ERROR = 10;

}
