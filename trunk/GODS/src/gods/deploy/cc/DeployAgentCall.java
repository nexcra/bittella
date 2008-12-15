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
 * The <code>DeployAgentCall</code> class implements the Callable interface
 * and overrides the call function to deploy agent code on host specified in
 * constructor
 * 
 * @author Ozair Kafray
 * @version $Id$
 */
public class DeployAgentCall implements Callable<Integer> {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger.getLogger(DeployAgentCall.class);

	/**
	 * Host name of the machine on which agent is to be deployed
	 */
	private String host = null;

	/**
	 * The script that deploys the agent
	 */
	private static String deployScript = null;

	/**
	 * @param host
	 */
	public DeployAgentCall(String host) {
		this.host = host;
	}

	/**
	 * {@link java.util.concurrent.Callable#call()}
	 */
	public Integer call() throws Exception {
		String command = DeployAgentCall.deployScript + " " + host;

		log.debug(command);

		Process deploy = Runtime.getRuntime().exec(command);

		return new Integer(deploy.waitFor());
	}

	/**
	 * @param deployScript
	 *            the deployScript to set
	 */
	public static void setDeployScript(String deployScript) {
		DeployAgentCall.deployScript = deployScript;
	}

	/**
	 * @return the deployScript
	 */
	public static String getDeployScript() {
		return deployScript;
	}

}
