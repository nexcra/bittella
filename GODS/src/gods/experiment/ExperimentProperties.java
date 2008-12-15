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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The <code>ExperimentProperties</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class ExperimentProperties {

	/**
	 * 
	 */
	private static String experimentHome = "";

	/**
	 * 
	 */
	private static String experimentName = "";

	/**
	 * 
	 */
	private static String experimentGeneratorClass = "";

	/**
	 * 
	 */
	private static String experimentFile = "";

	/**
	 * 
	 */
	private static String validationFile = "";

	/**
	 * 
	 */
	private static String resultFile = "";

	/**
	 * 
	 */
	private static String experimentLog = "";

	/**
	 * 
	 */
	private static String netModelPath = "";

	/**
	 * 
	 */
	private static String appHome = "";

	/**
	 * 
	 */
	private static String appDeployScript = "";

	/**
	 * 
	 */
	private static String appInitScript = "";

	/**
	 * 
	 */
	private static String appLaunchScript = "";

	/**
	 * 
	 */
	private static String appRemoteLog = "";

	/**
	 * 
	 */
	private static String appArgGenDisplayName = null;

	/**
	 * 
	 */
	private static String argGenParamsFile = null;

	/**
	 * 
	 */
	private static int appKillSignal = 0;

	/**
	 * 
	 */
	private static int appStopSignal = 0;

	/**
	 * Time period of the experiment in seconds
	 */
	private static int experimentTime = 0;

	/**
	 * 
	 */
	private static int seed = 1;

	/**
	 * 
	 */
	private static int numberOfSlots = 0;

	/**
	 * Configuration parameters for Experiment Generator
	 */
	private static Properties properties = null;

	/**
	 * 
	 */
	private static String appRemoteHome;

	public static boolean initialize(String experimentParametersFile)
			throws PropertyNotFoundException {

		try {
			InputStream startupFile = new FileInputStream(
					experimentParametersFile);
			properties = new Properties();
			properties.loadFromXML(startupFile);
			startupFile.close();

			String experimentDir = null;
			String temp = null;

			if ((experimentDir = getProperty("experiment.dir.path")) == null) {
				throw new PropertyNotFoundException("experiment.dir.path",
						experimentParametersFile);
			}

			if ((experimentName = getProperty("experiment.name")) == null) {
				throw new PropertyNotFoundException("experiment.name",
						experimentParametersFile);
			}

			if ((experimentGeneratorClass = getProperty("experiment.gen.class")) == null) {
				throw new PropertyNotFoundException("experiment.gen.class",
						experimentParametersFile);
			}

			if ((netModelPath = getProperty("network.model.path")) == null) {
				throw new PropertyNotFoundException("network.model.path",
						experimentParametersFile);
			}

			if ((appHome = getProperty("app.home.path")) != null) {
				if (!appHome.endsWith("/")) {
					appHome = appHome + "/";
				}
			} else {
				throw new PropertyNotFoundException("app.home.path",
						experimentParametersFile);
			}

			if ((appRemoteHome = getProperty("app.remote.home")) != null) {
				if (!appRemoteHome.endsWith("/")) {
					appRemoteHome = appRemoteHome + "/";
				}
			} else {
				throw new PropertyNotFoundException("app.remote.home",
						experimentParametersFile);
			}

			if ((temp = getProperty("app.remote.log")) != null) {
				appRemoteLog = appRemoteHome + temp;
			} else {
				throw new PropertyNotFoundException("app.remote.log",
						experimentParametersFile);
			}

			if ((temp = getProperty("app.deploy.script")) != null) {
				appDeployScript = appHome + temp;
			} else {
				throw new PropertyNotFoundException("app.deploy.script",
						experimentParametersFile);
			}

			if ((temp = getProperty("app.init.script")) != null) {
				appInitScript = appRemoteHome + temp;
			} else {
				throw new PropertyNotFoundException("app.init.script",
						experimentParametersFile);
			}

			if ((temp = getProperty("app.launch.script")) != null) {
				appLaunchScript = appRemoteHome + temp;
			} else {
				throw new PropertyNotFoundException("app.launch.script",
						experimentParametersFile);
			}

			if ((temp = getProperty("totaltime.int")) != null) {
				experimentTime = Integer.parseInt(temp);
			} else {
				throw new PropertyNotFoundException("totaltime.int",
						experimentParametersFile);
			}

			if ((temp = getProperty("seed.int")) != null) {
				seed = Integer.parseInt(temp);
			} else {
				throw new PropertyNotFoundException("seed.int",
						experimentParametersFile);
			}

			if ((temp = getProperty("slots.int")) != null) {
				numberOfSlots = Integer.parseInt(temp);
			} else {
				throw new PropertyNotFoundException("slots.int",
						experimentParametersFile);
			}

			if ((temp = getProperty("app.kill.signal.int")) != null) {
				appKillSignal = Integer.parseInt(temp);
			} else {
				throw new PropertyNotFoundException("app.kill.signal.int",
						experimentParametersFile);
			}

			if ((temp = getProperty("app.stop.signal.int")) != null) {
				appStopSignal = Integer.parseInt(temp);
			} else {
				throw new PropertyNotFoundException("app.stop.signal.int",
						experimentParametersFile);
			}

			if ((temp = getProperty("app.arggen.displayname")) != null) {

				if (!temp.equals("void")) {

					appArgGenDisplayName = temp;

					if ((temp = getProperty("app.arggen.config.file")) != null) {

						argGenParamsFile = temp;

					} else {
						throw new PropertyNotFoundException(
								"app.arggen.config.file",
								experimentParametersFile);
					}

				}

			} else {
				throw new PropertyNotFoundException("app.arggen.displayname",
						experimentParametersFile);
			}

			experimentHome = experimentDir + experimentName + "/";
			experimentFile = experimentHome + experimentName + ".exp";

			validationFile = experimentHome + experimentName + ".val";

			resultFile = experimentHome + experimentName + ".res";

			experimentLog = experimentHome + experimentName + ".log";

		} catch (FileNotFoundException fnfe) {
			System.out
					.println("Could not find experiment generation parameters file:"
							+ fnfe.getMessage());

		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());

		}
		return true;
	}

	public static void dump() {
		System.out.println("Experiment Home: " + getExperimentHome());
		System.out.println("Experiment Name: " + getExperimentName());
		System.out.println("Experiment Generator Class: "
				+ getExperimentGeneratorClass());
		System.out.println("Net Model Path: " + getNetModelPath());
		System.out.println("App Home: " + getAppHome());
		System.out.println("Experiment Time: " + getExperimentTime());
		System.out.println("Slots: " + getNumberOfSlots());
		System.out.println("ArgGen Name:" + getAppArgGenDisplayName());
		System.out.println("ArgGen Paramaters File:" + getArgGenParamsFile());
	}

	private static String getProperty(String key) {
		return properties.getProperty(key);
	}

	/**
	 * @return the appDeployScript
	 */
	public static String getAppDeployScript() {
		return appDeployScript;
	}

	/**
	 * @return the appHome
	 */
	public static String getAppHome() {
		return appHome;
	}

	/**
	 * @return the appInitScript
	 */
	public static String getAppInitScript() {
		return appInitScript;
	}

	/**
	 * @return the appKillSignal
	 */
	public static int getAppKillSignal() {
		return appKillSignal;
	}

	/**
	 * @return the appLaunchScript
	 */
	public static String getAppLaunchScript() {
		return appLaunchScript;
	}

	/**
	 * @return the appStopSignal
	 */
	public static int getAppStopSignal() {
		return appStopSignal;
	}

	/**
	 * @return the experimentFile
	 */
	public static String getExperimentFile() {
		return experimentFile;
	}

	/**
	 * @return the experimentHome
	 */
	public static String getExperimentHome() {
		return experimentHome;
	}

	/**
	 * @return the experimentLog
	 */
	public static String getExperimentLog() {
		return experimentLog;
	}

	/**
	 * @return the experimentName
	 */
	public static String getExperimentName() {
		return experimentName;
	}

	/**
	 * @return the experimentTime
	 */
	public static int getExperimentTime() {
		return experimentTime;
	}

	/**
	 * @return the netModelPath
	 */
	public static String getNetModelPath() {
		return netModelPath;
	}

	/**
	 * @return the noOfVnodes
	 */
	public static int getNumberOfSlots() {
		return numberOfSlots;
	}

	/**
	 * @return the resultFile
	 */
	public static String getResultFile() {
		return resultFile;
	}

	/**
	 * @return the seed
	 */
	public static int getSeed() {
		return seed;
	}

	/**
	 * @return the validationFile
	 */
	public static String getValidationFile() {
		return validationFile;
	}

	/**
	 * @return the appRemoteLog
	 */
	public static String getAppRemoteLog() {
		return appRemoteLog;
	}

	/**
	 * @return the appArgGenDisplayName
	 */
	public static String getAppArgGenDisplayName() {
		return appArgGenDisplayName;
	}

	/**
	 * @return the argGenParamsFile
	 */
	public static String getArgGenParamsFile() {
		return argGenParamsFile;
	}

	/**
	 * @return the appRemoteHome
	 */
	public static String getAppRemoteHome() {
		return appRemoteHome;
	}

	/**
	 * @return the experimentGeneratorClass
	 */
	public static String getExperimentGeneratorClass() {
		return experimentGeneratorClass;
	}
}
