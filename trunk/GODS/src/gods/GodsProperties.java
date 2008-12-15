/**
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods;

import gods.cc.GodsStatus;
import gods.churn.ArgumentGeneratorFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Properties;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The <code>GodsProperties</code> class encapsultes all the startup
 * properties required by different modules of Gods. This is a singleton class
 * to be explicitly initialized by calling its initialize function.
 * 
 * @author Ozair Kafray
 * @version $Id: GodsProperties.java 432 2008-02-02 14:17:12Z ozair $
 */
public final class GodsProperties {

	/**
	 * The time in milliseconds that InetAddress waits to reach a specified host. 
	 * See {@link gods.GodsProperties#areMachinesReachable()}
	 */
	private static int waitForHost = 1000;
	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger.getLogger(GodsProperties.class);

	/**
	 * Startup Properties for Gods
	 */
	private static Properties properties = null;

	/**
	 * Deployment Script Error messages specified as Java properties
	 */
	private static Properties deployErrorMessages = null;

	/**
	 * Experiment setup Script Error messages specified as Java properties
	 */
	private static Properties setupErrorMessages = null;

	/**
	 * Host Name of the Gods Control Center machine
	 */
	private static String localHost = null;

	/**
	 * Home folder of Gods. It is assumed that it contains scripts, config and
	 * all other required resources
	 */
	private static String godsHome = "";

	/**
	 * List of machines for running application nodes
	 */
	private static Vector<String> hosts = new Vector<String>();

	/**
	 * List of machines for emulating model net
	 */
	private static Vector<String> emulators = new Vector<String>();

	/**
	 * Total number of slots i.e., virtual nodes available for Gods experiment
	 */
	private static int numberOfSlots = 1;

	/**
	 * A value to GodsStatus type specifying the current state of Gods
	 * 
	 * {@link gods.cc.GodsStatus}
	 */
	private static GodsStatus godsStatus;

	/**
	 * This function currently checks if all the necessary startup parameters
	 * are available in the specified file to return true and returns false
	 * otherwise, on which the application should exit. Gives warnings on
	 * properties expected but not found.
	 * 
	 * @param startupFileName
	 * @return
	 * @throws IOException
	 */
	public static boolean initialize(String startupFileName) {

		setGodsStatus(GodsStatus.INITIALIZING);

		System.err.println("START " + startupFileName);
		
		try {

			localHost = InetAddress.getLocalHost().getHostName();
			log.debug("=====================================");
			log.debug("Hostname:" + localHost);
			log.debug("=====================================");
			InputStream startupFile = new FileInputStream(startupFileName);
			properties = new Properties();
			properties.loadFromXML(startupFile);
			startupFile.close();

			if ((godsHome = System.getProperty("gods.home")) != null) {
				if (!(godsHome.endsWith("/"))) {
					godsHome = godsHome + "/";
				}
			} else {
				log
						.warn("WARNING: 'gods.home' System property not specified while launching gods.Gods"
								+ "Make sure all paths specified in config file are absolute, "
								+ "NOT relative to gods.home folder");
			}

			// Get 'hostsFile' property and load host names
			String dirName = getAbsoluteProperty("gods.net.model.dir");
			if (dirName != null) {
				if (!(dirName.endsWith("/"))) {
					dirName = dirName + "/";
				}

				String machinesFile = dirName + getMachinesFile(dirName);

				if (machinesFile != null) {
					readHosts(machinesFile);
					log.debug("=====================================");
					log.debug("==         ModelNet Hosts          ==");
					log.debug("=====================================");
					display(hosts);

					log.debug("=====================================");
					log.debug("==       ModelNet Emulators        ==");
					log.debug("=====================================");
					readEmulators(machinesFile);
					display(emulators);

					if (!areMachinesReachable()) {
						return false;
					}

					/*
					 * String graphFile = dirName + getGraphFile(dirName);
					 * log.debug("Graph file is: " + graphFile); numberOfSlots =
					 * calculateSlots(graphFile); log.info("Number of Slots = " +
					 * numberOfSlots);
					 */

				} else {
					return false;
				}

			} else {

				log
						.fatal("Property-> 'gods.net.model' not specified in config file");
				return false;
			}

			String fileName = "";
			// Get 'deployScript' property
			if ((fileName = getAbsoluteProperty("gods.agent.deploy.script")) == null) {
				log
						.fatal("Property-> 'gods.agent.deploy.script' not specified in config file");

				return false;
			}

			// Get 'setupScript' property
			if ((fileName = getAbsoluteProperty("gods.agent.setup.script")) == null) {
				log
						.fatal("Property-> 'gods.agent.setup.script' not specified in config file");

				return false;
			}

			// Get 'launchScript' property
			if ((fileName = getAbsoluteProperty("gods.agent.launch.script")) == null) {
				log
						.fatal("Property-> 'gods.agent.launch.script' not specified in config file");

				return false;
			}

			if (getRelativeProperty("gods.slots") != null) {
				log.debug("Getting Number of Slots");
				numberOfSlots = Integer
						.parseInt(getRelativeProperty("gods.slots"));
				log.debug("Number of Slots are:" + numberOfSlots);
			} else {
				log
						.fatal("Property-> 'gods.slots' not specified in config file");
				return false;
			}

			/*
			 * The loading of the following error messages files can be delayed
			 * to when they are really required. However, for now it is being
			 * done here for the purpose of simplicity
			 */

			// Get 'deployErrorMessages' property and load script error messages
			if ((fileName = getAbsoluteProperty("gods.agent.deploy.errors")) == null) {
				log
						.warn("Property-> 'gods.agent.deploy.errors' not specified in config file");
			} else {
				log.debug("Deploy Error Messages File" + fileName);
				InputStream errorMsgsFile = new FileInputStream(fileName);
				deployErrorMessages = new Properties();
				deployErrorMessages.loadFromXML(errorMsgsFile);
				errorMsgsFile.close();
			}

			// Get 'setupErrorMessages' property and load script error messages
			if ((fileName = getAbsoluteProperty("gods.agent.setup.errors")) == null) {
				log
						.warn("Property-> 'gods.agent.setup.errors' not specified in config file");
			} else {
				log.debug("Setup Error Messages File" + fileName);
				InputStream errorMsgsFile = new FileInputStream(fileName);
				setupErrorMessages = new Properties();
				setupErrorMessages.loadFromXML(errorMsgsFile);
				errorMsgsFile.close();
			}

			// Get 'gods.churn.argumentgenerators.file' property and initialize
			// ArgumentGeneratorFactory
			if ((fileName = getAbsoluteProperty("gods.churn.argumentgenerators.file")) == null) {
				log
						.warn("Property-> 'gods.churn.argumentgenerators.file' not specified in config file");
			} else {
				log.debug("Gods Churn Argument Generator" + fileName);
				ArgumentGeneratorFactory.initialize(fileName);
			}

			return true;

		} catch (IOException ioe) {
			StackTraceElement[] trace = ioe.getStackTrace();
			log.error("IOEXCEPTION: " + ioe.getMessage());
			for (StackTraceElement e:trace) {
				log.error(e.toString());
			}
			return false;
		}
	}

	/**
	 * Reads the specified .machines file to get host names
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	private static void readHosts(String fileName) throws IOException {

		try {
			FileInputStream is = new FileInputStream(fileName);
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder parser = factory.newDocumentBuilder();

			Document d = parser.parse(is);

			Element e = d.getDocumentElement();

			NodeList n = e.getElementsByTagName("host");

			// For every class
			String hostName = null;
			for (int i = 0; i < n.getLength(); i++) {
				Element x = (Element) n.item(i);

				hostName = x.getAttribute("hostname");
				hosts.add(hostName);

			}

			is.close();

		} catch (ParserConfigurationException pce) {
			log.error(pce.getMessage());

		} catch (SAXException se) {
			log.error(se.getMessage());
		}

		/*
		 * log.debug("====================================="); log.info("Reading
		 * Hosts from hosts file:" + fileName);
		 * 
		 * try { BufferedReader hostsFile = new BufferedReader( new
		 * InputStreamReader(new FileInputStream(fileName)));
		 * 
		 * String host = null; int i = 1;
		 * log.debug("====================================="); while ((host =
		 * hostsFile.readLine()) != null) { hosts.add(host); log.debug("Host " +
		 * i + ":" + host); ++i; }
		 * log.debug("=====================================");
		 * hostsFile.close(); } catch (FileNotFoundException fnfe) {
		 * 
		 * log.fatal("Host file: " + fnfe.getMessage() + " not found"); }
		 */
	}

	/**
	 * Reads the specified .machines file to get host names
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	private static void readEmulators(String fileName) throws IOException {
		try {
			FileInputStream is = new FileInputStream(fileName);
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder parser = factory.newDocumentBuilder();

			Document d = parser.parse(is);

			Element e = d.getDocumentElement();

			NodeList n = e.getElementsByTagName("emul");

			// For every class
			String hostName = null;
			for (int i = 0; i < n.getLength(); i++) {
				Element x = (Element) n.item(i);

				hostName = x.getAttribute("hostname");
				emulators.add(hostName);

			}

			is.close();

		} catch (ParserConfigurationException pce) {
			log.error(pce.getMessage());

		} catch (SAXException se) {
			log.error(se.getMessage());
		}
	}

	/**
	 * Finds the number of vertices in specified .graph file whose role is
	 * virtual node
	 * 
	 * @param fileName
	 * @return
	 */
	private static int calculateSlots(String fileName) throws IOException {
		int noOfSlots = 0;

		try {
			FileInputStream is = new FileInputStream(fileName);
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder parser = factory.newDocumentBuilder();

			Document d = parser.parse(is);

			Element e = d.getDocumentElement();

			NodeList v = e.getElementsByTagName("vertices");
			log.debug("Elements named vertices are:" + v.getLength());
			for (int i = 0; i < v.getLength(); i++) {
				log.debug("here:" + i);
				Node n = v.item(i);
				NodeList vertices = n.getChildNodes();
				NamedNodeMap nodeAttributes = null;
				for (int j = 0; j < vertices.getLength(); j++) {
					Node x = vertices.item(j);
					if (x.getLocalName() == "vertex") {
						log.debug("here:" + j);
						if ((nodeAttributes = x.getAttributes()) != null) {
							if (nodeAttributes.getNamedItem("role")
									.getNodeValue() == "virtnode") {
								++noOfSlots;
								log.debug(noOfSlots);
							}
						}
					}

				}

			}

			is.close();

		} catch (ParserConfigurationException pce) {
			log.error(pce.getMessage());

		} catch (SAXException se) {
			log.error(se.getMessage());
		}

		return noOfSlots;
	}

	private static String getGraphFile(String dirName) {
		File modelDir = new File(dirName);
		String graphFile = null;

		// Files in the directory
		String[] fileNames = modelDir.list();

		// If there is no file in the directory
		if (fileNames == null) {
			log.error("The model specified as \"gods.net.model.dir\""
					+ " is not a directory");

		} else {
			// Check for .graph files in the directory
			Vector<String> graphFiles = new Vector<String>();
			for (String s : fileNames) {
				if (s.endsWith(".graph")) {
					graphFiles.add(s);
				}
			}
			// Warn if more than one because only first will be considered.
			if (graphFiles.size() > 1) {
				log.warn("There are more than one .graph file in " + dirName
						+ ". Considering " + graphFiles.get(0)
						+ " and ignoring others.");
			}

			graphFile = graphFiles.get(0);
		}
		return graphFile;
	}

	private static String getMachinesFile(String dirName) {

		File modelDir = new File(dirName);
		String machinesFile = null;

		// Files in the directory
		String[] fileNames = modelDir.list();

		// If there is no file in the directory
		if (fileNames == null) {
			log.error("The model specified as \"gods.net.model.dir\""
					+ " is not a directory");

		} else {
			// Check for .machines files in the directory
			Vector<String> machineFiles = new Vector<String>();
			for (String s : fileNames) {
				if (s.endsWith(".machines")) {
					machineFiles.add(s);
				}
			}
			// Warn if more than one because only first will be considered.
			if (machineFiles.size() > 1) {
				log.warn("There are more than one .machine file in " + dirName
						+ ". Considering " + machineFiles.get(0)
						+ " and ignoring others.");
			}

			machinesFile = machineFiles.get(0);
		}
		return machinesFile;
	}

	/**
	 * @return the list of machines which would run Gods Agents
	 */
	public static Vector<String> getHosts() {
		return hosts;
	}

	/**
	 * @return the number of hosts for this Gods experiment
	 */
	public static int getNumberOfHosts() {
		return hosts.size();
	}

	/**
	 * @return the total number of virtual nodes
	 */
	public static int getNumberOfSlots() {
		return numberOfSlots;
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
		return godsHome + property;
	}

	/**
	 * @param key -
	 *            String key for which value is desired from the startup file.
	 *            It returns the property value as it is in configuration file.
	 *            To get a value prefixed with gods home
	 *            {@link GodsProperties#getAbsoluteProperty(String)}
	 * @return value - String value of the property whose key was specified
	 *         {@link java.util.Properties}
	 */
	public static String getRelativeProperty(String key) {
		return properties.getProperty(key);
	}

	/**
	 * Checks whether the hosts specified in hosts file are reachable
	 * 
	 * @return
	 * @throws IOException
	 */
	private static boolean areMachinesReachable() throws IOException {

		log.info("Checking machines' reachability...");

		BufferedReader inputStream = null;
		Process p = null;
		Vector<String> unreachableHosts = new Vector<String>();

		for (String host : hosts) {
			if (!InetAddress.getByName(host).isReachable(waitForHost)) {
				unreachableHosts.add(host);
			}
		}

		if (unreachableHosts.size() > 0) {

			log.debug("Following hosts are unreachable.");

			for (int j = 0; j < unreachableHosts.size(); j++) {
				log.debug("    " + unreachableHosts.get(j));
			}
			return false;
		}

		for (String emulator : emulators) {
			if (!InetAddress.getByName(emulator).isReachable(waitForHost)) {
				unreachableHosts.add(emulator);
			}
		}

		if (unreachableHosts.size() > 0) {

			log.debug("Following emulators are unreachable.");

			for (int j = 0; j < unreachableHosts.size(); j++) {
				log.debug("    " + unreachableHosts.get(j));
			}
			return false;
		}

		return true;

		/*
		 * String command = null; String line = null; StringBuffer result = new
		 * StringBuffer(); // FIX Check if this is possible with
		 * InetAddress.isReachable() for (int i = 0; i < hosts.size(); i++) {
		 * command = "ping -c 1 " + hosts.get(i); log.debug(command); p =
		 * Runtime.getRuntime().exec(command); inputStream = new
		 * BufferedReader(new InputStreamReader(p .getInputStream()));
		 * 
		 * while ((line = inputStream.readLine()) != null) {
		 * result.append(line); }
		 * 
		 * if (result.indexOf("Unreachable") != -1) { log.debug(result);
		 * unreachableHosts.add(hosts.get(i)); }
		 * 
		 * result.delete(0, result.length() - 1); inputStream.close(); }
		 * 
		 * 
		 * if (unreachableHosts.size() > 0) {
		 * 
		 * log.debug("Following hosts are unreachable.");
		 * 
		 * for (int j = 0; j < unreachableHosts.size(); j++) { log.debug(" " +
		 * unreachableHosts.get(j)); }
		 */
		/*
		 * The following code has been removed since this change will affect the
		 * network model which is not being taken care here. To provide this the
		 * network model should also be regenerated here with less machines and
		 * the hosts file written in the same format as model.machines. The same
		 * was possible earlier because of separate hosts file for GODS and
		 * modelnet.
		 * 
		 * int input = 0; do {
		 * 
		 * System.out.println("Do you wish to continue(y/n)..."); input =
		 * System.in.read(); } while ((input != 'y') && (input != 'n'));
		 * 
		 * if (input == 'y') { hosts.removeAll(unreachableHosts);
		 * log.info("Modifying hosts file...");
		 * 
		 * FileOutputStream newHosts = new FileOutputStream(properties
		 * .getProperty("gods.hosts.file"));
		 * 
		 * for (int i = 0; i < hosts.size(); i++) { newHosts.write((hosts.get(i) +
		 * "\n").getBytes()); }
		 * 
		 * newHosts.flush(); newHosts.close(); } else {
		 * 
		 * return false; }
		 */
	}

	/**
	 * @param code -
	 *            Integer value of error code during Gods Agent deployment
	 * @return - String message corresponding to the Integer code specified
	 */
	public static String getDeployErrorMessage(Integer code) {
		return deployErrorMessages.getProperty(code.toString());
	}

	/**
	 * @param code -
	 *            Integer value of error code for during Gods setup
	 * @return - String message corresponding to the Integer code specified
	 */
	public static String getSetupErrorMessage(Integer code) {
		return setupErrorMessages.getProperty(code.toString());
	}

	private static void display(Vector<String> list) {

		for (int i = 0; i < list.size(); i++) {
			log.info("    " + list.get(i));
		}
	}

	/**
	 * @return the localHost
	 */
	public static String getLocalHost() {
		return GodsProperties.localHost;
	}

	/**
	 * @param godsStatus
	 *            the godsStatus to set
	 */
	public static void setGodsStatus(GodsStatus godsStatus) {
		GodsProperties.godsStatus = godsStatus;
		log.info("GODS STATUS UPDATE: " + godsStatus.toString());
	}

	/**
	 * @return the godsStatus
	 */
	public static GodsStatus getGodsStatus() {
		return GodsProperties.godsStatus;
	}

	/**
	 * @return the path godsHome
	 */
	public static String getGodsHome() {
		return godsHome;
	}

	/**
	 * @return the emulators
	 */
	public static Vector<String> getEmulators() {
		return emulators;
	}

}
