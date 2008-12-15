/**
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.deploy.cc;

import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

/**
 * The <code>StartAgentCall</code> class implements the Callable interface and
 * overrides the call function to start agents on host specified in constructor
 * 
 * @author Ozair Kafray
 * @version $Id$
 */
public class StartAgentCall implements Callable {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger.getLogger(StartAgentCall.class);

	/**
	 * Host name of the machine on which agent is to be statred
	 */
	private String host = null;

	/**
	 * The script that executes the agent
	 */
	private static String setupScript = null;

	/**
	 * @param host
	 */
	public StartAgentCall(String host) {
		super();
		this.host = host;
	}

	/**
	 * {@link java.util.concurrent.Callable#call()}
	 */
	public Object call() throws Exception {
		String command = StartAgentCall.setupScript + " " + host;

		log.debug(command);

		Process startup = Runtime.getRuntime().exec(command);

		return new Integer(startup.waitFor());
	}

	/**
	 * @param setupScript
	 *            the setupScript to set
	 */
	public static void setSetupScript(String setupScript) {
		StartAgentCall.setupScript = setupScript;
	}

	/**
	 * @return the setupScript
	 */
	public static String getSetupScript() {
		return setupScript;
	}

}
