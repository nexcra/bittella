/**
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.agent;

import gods.churn.agent.AgentChurnModule;
import gods.deploy.agent.AgentDeploymentModule;
import gods.deploy.events.AgentBootEvent;
import gods.experiment.agent.AgentExperimentExecutor;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * The <code>GodsAgent</code> class is responsible for bootstrapping an Agent.
 * It has the main function to start Agents.
 * 
 * @author Ozair Kafray
 * @version $Id: GodsAgent.java 386 2007-07-24 16:03:10Z ozair $
 */
public class GodsAgent {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger.getLogger(GodsAgent.class);

	/**
	 * This function bootstraps a Gods Agent 1. Initializes the information on
	 * which this Agent is running 2. Create, initialize and start Modules 3.
	 * Create AgentBootEvent and enqueue it in Agent.
	 * 
	 * @param args
	 *            requires the host name of the machine on which ControlCenter
	 *            is running.
	 */
	public static void main(String[] args) {

		System.out.println(System.getProperty("org.apache.log4j.config.file"));

		PropertyConfigurator.configure(System
				.getProperty("org.apache.log4j.config.file"));

		// Initialize AgentProperties
		if ((args.length == 0) || (args.length > 2)) {
			System.out
					.println("usage: gods.agent.GodsAgent <ccHostName> <agent-config-file>");
			System.exit(1);
		}

		try {
			// Initialize Agent with this Machine's information
			String localHost = InetAddress.getLocalHost().getHostName();
			log.debug("=====================================");
			log.debug("Hostname:" + localHost);
			log.debug("=====================================");
			Agent.getInstance().initializeMachineInformation(localHost);
			AgentProperties.initialize(args[1]);

			addShutdownHook();
			// Initializing Modules
			/*
			 * For now these modules are just being created and
			 * initialized/started. Later even the list of modules can be
			 * specified in a GodsAgent config file
			 */
			new AgentDeploymentModule().start();
			new AgentChurnModule().start();
			new AgentExperimentExecutor().start();

			// Creating AgentBootEvent
			/*
			 * Creation of AgentBootEvent is the first event needed to be raised
			 * and then the agent starts to work, handling a series of events
			 */
			AgentBootEvent agentBoot = new AgentBootEvent(1);

			// Parameter specifying hostname where cc is running
			if (args.length != 2) {
				log.error("[hostname-cc] not provided..");
				agentBoot.setCcHostName("//" + localHost + "/gods-cc");

			} else {
				agentBoot.setCcHostName("//" + args[0] + "/gods-cc");
			}

			Agent.getInstance().enqueueEvent(agentBoot);
			Agent.getInstance().run();

		} catch (java.net.UnknownHostException uhe) {

			log.error(uhe.getMessage());

		}

	}

	private static void addShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					Naming.unbind(Agent.getAgentNameOnHost(InetAddress
							.getLocalHost().getHostName())); 

				} catch (RemoteException re) {
					log.debug(re.getMessage());

				} catch (MalformedURLException mfue) {
					log.debug(mfue.getMessage());

				} catch (NotBoundException nbe) {
					log.debug(nbe.getMessage());

				} catch (UnknownHostException uhe) {
					log.debug(uhe.getMessage());

				}
			}
		});
	}
}
