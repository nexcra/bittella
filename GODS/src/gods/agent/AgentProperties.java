/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.agent;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * The <code>AgentProperties</code> class encapsultes all the startup
 * properties required by different modules of a Gods Agent. This is a singleton
 * class to be explicitly initialized by calling its initialize function.
 * 
 * @author Ozair Kafray
 * @version $Id: AgentProperties.java 363 2007-07-16 09:39:28Z ozair $
 */
public final class AgentProperties {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger.getLogger(AgentProperties.class);

	/**
	 * Home folder of the Gods Agent. It is assumed that it contains scripts,
	 * config and all resources required for a GODS agent to run
	 */
	private static String agentHome = "";

	/**
	 * Script for launching an application on virtual node
	 */
	private static String launchScript = null;

	/**
	 * Script for stopping an application on virtual node
	 */
	private static String stopScript = null;

	/**
	 * To kill an application running on a virtual node
	 */
	private static String killScript = null;

	/**
	 * Startup Properties for Gods Agent
	 */
	private static Properties properties = null;

	public static boolean initialize(String agentStartupFileName) {

		try {
			InputStream startupFile = new FileInputStream(agentStartupFileName);
			properties = new Properties();
			properties.loadFromXML(startupFile);
			startupFile.close();

			if ((agentHome = System.getProperty("gods.agent.home")) != null) {
				if (!(agentHome.endsWith("/"))) {
					agentHome = agentHome + "/";
				}
			} else {
				log
						.warn("WARNING: 'gods.agent.home' property not specified in agent.config file."
								+ "Make sure all paths specified are absolute,"
								+ "NOT relative to gods.agent.home folder");
			}

			if ((launchScript = getAbsoluteProperty("gods.agent.launch.application.script")) == null) {
				log
						.error("Property-> 'gods.agent.launch.application.script' not specified in agent.config file");

			}

			if ((stopScript = getAbsoluteProperty("gods.agent.stop.application.script")) == null) {
				log
						.error("Property-> 'gods.agent.stop.application.script' not specified in agent.config file");

			}

			if ((killScript = getAbsoluteProperty("gods.agent.kill.application.script")) == null) {
				log
						.error("Property-> 'gods.agent.kill.application.script' not specified in agent.config file");

			}

			log.info("appLaunchScript = " + launchScript);
			log.info("appStopScript = " + stopScript);
			log.info("appKillScript = " + killScript);

		} catch (FileNotFoundException fnfe) {
			log.error("Could not find startup file:" + fnfe.getMessage());

		} catch (IOException ioe) {
			log.error(ioe.getMessage());
		}
		return true;
	}

	/**
	 * @param key -
	 *            String key for which value is desired from the startup file.
	 *            It returns the property value as it is in configuration file.
	 *            To get a value prefixed with gods home
	 *            {@link AgentProperties#getAbsoluteProperty(String)}
	 * @return value - String value of the property whose key was specified
	 *         {@link java.util.Properties}
	 */
	public static String getRelativeProperty(String key) {
		return properties.getProperty(key);
	}

	/**
	 * @param key
	 * @return A property value prefixed with gods.home
	 */
	public static String getAbsoluteProperty(String key) {
		String property = getRelativeProperty(key);
		if (property == null) {
			return null;
		}
		return agentHome + property;
	}

	/**
	 * @return the killScript to kill an application running on a virtual node
	 */
	public static String getKillScript() {
		return killScript;
	}

	/**
	 * @return the launchScript for launching an application on virtual node
	 */
	public static String getLaunchScript() {
		return launchScript;
	}

	/**
	 * @return the stopScript for stopping an application on virtual node
	 */
	public static String getStopScript() {
		return stopScript;
	}

	/**
	 * @return the agentHome
	 */
	public static String getAgentHome() {
		return agentHome;
	}

	/**
	 * @param agentHome the agentHome to set
	 */
	public static void setAgentHome(String agentHome) {
		AgentProperties.agentHome = agentHome;
	}
}
