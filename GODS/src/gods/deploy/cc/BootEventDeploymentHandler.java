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

import gods.GodsProperties;
import gods.arch.Event;
import gods.arch.EventHandler;
import gods.arch.Module;
import gods.cc.ControlCenter;
import gods.cc.GodsStatus;
import gods.deploy.events.BootEvent;

import java.io.IOException;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * The <code>BootEventDeploymentHandler</code> class is responsible for
 * handling the BootEvent for DeploymentModule of ContrlCenter
 * 
 * @author Ozair Kafray
 * @version $Id$
 */
public class BootEventDeploymentHandler extends EventHandler {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger
			.getLogger(BootEventDeploymentHandler.class);

	/**
	 * @param event
	 * @param module
	 */
	public BootEventDeploymentHandler(Event event, Module module) {
		super(event, module);
		// TODO Auto-generated constructor stub
	}

	/**
	 * {@link gods.arch.EventHandler#handle()}
	 */
	@Override
	public Object handle() throws ClassCastException {

		log.debug("");

		BootEvent boot = (BootEvent) event;
		int result = 0;
		try {

			if (deployAgents() == false) {
				log.fatal("Aborting due to unsuccessful deployment.");
			}

			if (ControlCenter.getInstance().initialize() == false) {
				log
						.fatal("Aborting. Unsuccessfull attempt to start ControlCenter.");
			}

			GodsProperties.setGodsStatus(GodsStatus.JOINING);

			if (boot.isAutoStartAgents()) {

				if (startAgents() == false) {
					log
							.fatal("Aborting. Unsuccessfull attempt to start Agent.");
				}

			}

		} catch (IOException ioe) {
			log
					.fatal("EXCEPTION IOException while deploying or executing Agents "
							+ ioe.getMessage());
		}

		return new Integer(result);
	}

	/**
	 * TODO: document
	 */
	private boolean deployAgents() throws IOException {

		log.info("Deploying Agents...");

		String deployScript = GodsProperties
				.getAbsoluteProperty("gods.agent.deploy.script");
		Vector<String> hosts = GodsProperties.getHosts();
		Process deploy[] = new Process[hosts.size()];

		int count = 0;
		for (String host : hosts) {
			String command = deployScript + " " + host;

			log.debug(command);

			deploy[count++] = Runtime.getRuntime().exec(command);
		}
		count = 0;
		for (String host : hosts) {
			try {

				int exitValue = deploy[count++].waitFor();

				if (exitValue != 0) {
					String message = null;
					if ((message = GodsProperties
							.getDeployErrorMessage(exitValue)) != null) {
						log.fatal(message + " on " + host);

					} else {

						log
								.fatal("Encountered while deploying agent on "
										+ host
										+ ". Exact reason could not be determined because the deployErrorMessages file has not been specified in gods-startup.xml");
					}

					return false;

				}

				log.info("Successfully deployed agent to:" + host);

			} catch (InterruptedException ie) {

				log.fatal("EXCEPTION: Interrupted while deploying Agents "
						+ ie.getMessage());
				return false;
			}
		}
		return true;
	}

	/**
	 * TODO:document
	 */
	private boolean startAgents() throws IOException {

		log.info("Starting Agents...");

		Vector<String> hosts = GodsProperties.getHosts();
		String launchScript = GodsProperties
				.getAbsoluteProperty("gods.agent.launch.script");

		String setupScript = GodsProperties
				.getAbsoluteProperty("gods.agent.setup.script");

		for (int i = 0; i < hosts.size(); i++) {
			String command = launchScript + " " + setupScript + " "
					+ hosts.get(i);
			log.debug(command);

			Process deploy = Runtime.getRuntime().exec(command);

			try {

				int exitValue = deploy.waitFor();

				if (exitValue != 0) {
					String message = null;
					if ((message = GodsProperties
							.getSetupErrorMessage(exitValue)) != null) {
						log.fatal(message + " on " + hosts.get(i));

					} else {

						log
								.fatal("Encountered while deploying agent on "
										+ hosts.get(i)
										+ ". Exact reason could not be determined because the deployErrorMessages file has not been specified in gods-startup.xml");
					}

					return false;

				}
				log.info("Successfully started gods-agent on: " + hosts.get(i));

			} catch (InterruptedException ie) {

				log.fatal(ie.getMessage());
				return false;
			}
		}
		return true;
	}

}
