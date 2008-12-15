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

import gods.experiment.events.JoinExperimentEvent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Vector;

/**
 * The <code>Experiment</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class Experiment implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -203773602199189551L;

	/**
	 * Path to the directory containing 'model' and 'route' files
	 */
	private String netModelPath = null;

	/**
	 * The size of the overlay network.
	 */
	// private BigInteger netSize;
	/**
	 * A vector of generated experiment events
	 */
	private Vector<ExperimentEvent> events = null;

	/**
	 * An instance of argument generator for generating command line arguments
	 * to the system under test
	 */
	private String argGenDisplayName = null;

	/**
	 * Path of the file specifying parameters for ArgumentGenerator of the
	 * application under test
	 */
	private String argGenParamsFile = null;

	/**
	 * Script for deploying application
	 */
	private String deployScript;

	/**
	 * Script for cleaning up before launching the application
	 */
	private String initScript;

	/**
	 * Script for launching nodes of the application under test
	 */
	private String launchScript;

	/**
	 * File name of the centralized log
	 */
	private String centralizedlogFile;

	/**
	 * File with which the log is to be compared for validating the experiment
	 */
	private String validationFile;

	/**
	 * File where results from comparison of experiment log and validation are
	 * stored
	 */
	private String resultFile;

	/**
	 * Full path of the log file on remote machines in which the application
	 * logs
	 */
	private String appRemoteLog;

	/**
	 * @param netModelPath
	 * @param netSize
	 */
	public Experiment(String netModelPath/* , BigInteger netSize */) {
		this.netModelPath = netModelPath;
		// this.netSize = netSize;
	}

	/**
	 * Deafult Constructor
	 */
	public Experiment() {
	}

	/**
	 * Generates a list of events to be executed in the experiment.
	 * 
	 * @param churnEventGen
	 * @param totalTime
	 */
	public void create(AbstractChurnEventGenerator churnEventGen,
			TimeStamp totalTime) {

		events = new Vector<ExperimentEvent>();

		AbstractExperimentEvent event = churnEventGen.getNextChurnEvent();
		while (event.getTimeToHappen().compareTo(totalTime) < 0) {

			events.add(event);
			event = churnEventGen.getNextChurnEvent();
		}

	}

	/**
	 * @param index
	 * @return
	 */
	public ExperimentEvent get(int index) {
		ExperimentEvent event = null;

		if ((events != null) && (events.size() > index)) {
			event = events.get(index);
		}

		return event;
	}

	/**
	 * @param filename
	 */
	public static void store(String filename, Experiment experiment) {

		try {
			File file = new File(filename);
			FileOutputStream outStream = new FileOutputStream(file);
			ObjectOutputStream objStream = new ObjectOutputStream(outStream);

			objStream.writeObject(experiment);
			objStream.flush();
			objStream.close();

		} catch (IOException e) {
			// log.error("IOEXCEPTION: while creating file for saving
			// experiment");
			System.out
					.println("IOEXCEPTION: while creating file for saving experiment");
		}
	}

	/**
	 * @param filename
	 */
	public static Experiment load(String filename) {

		Experiment experiment = null;

		try {
			File file = new File(filename);
			FileInputStream inStream = new FileInputStream(file);
			ObjectInputStream objStream = new ObjectInputStream(inStream);

			experiment = (Experiment) objStream.readObject();
			objStream.close();

		} catch (IOException ioe) {
			// log.error("IOEXCEPTION: " + ioe.getMessage());
			System.out.println("IOEXCEPTION: " + ioe.getMessage());
		} catch (ClassNotFoundException cnfe) {
			// log.error("CLASSNOTFOUNDEXCEPTION: " + cnfe.getMessage());
			System.out.println("CLASSNOTFOUNDEXCEPTION: " + cnfe.getMessage());
		}

		return experiment;
	}

	public void dump() {

		System.out.println("Model: " + netModelPath);
		System.out.println("Deploy Script: " + deployScript);
		System.out.println("Initialization Script: " + initScript);
		System.out.println("Launch Script: " + launchScript);
		System.out.println("Centralized Log: " + centralizedlogFile);
		System.out.println("Validation File: " + validationFile);
		System.out.println("Result File: " + resultFile);
		
		System.out.println("ArgumentGenerator Name: " + argGenDisplayName);
		System.out.println("ArgumentGenerator Params File: " + argGenParamsFile);

		System.out.println("Events are :");
		System.out.printf("\t  TTH\t\t\tVnid EventType\n");
		for (ExperimentEvent ee : events) {
			System.out.println(ee.toString());
			if(ee instanceof JoinExperimentEvent && argGenDisplayName != null){
				JoinExperimentEvent joinEvent = (JoinExperimentEvent) ee;
				System.out.println(joinEvent.getAppArgs());
			}
		}
	}

	/**
	 * @return the argGenDisplayName
	 */
	public String getArgGenDisplayName() {
		return argGenDisplayName;
	}

	/**
	 * @param argGenDisplayName
	 *            the argGenDisplayName to set
	 */
	public void setArgGenDisplayName(String argGen) {
		this.argGenDisplayName = argGen;
	}

	/**
	 * @return the netModelPath
	 */
	public String getNetModelPath() {
		return netModelPath;
	}

	/**
	 * @param netModelPath
	 *            the netModelPath to set
	 */
	public void setNetModelPath(String netModelPath) {
		this.netModelPath = netModelPath;
	}

	/**
	 * @return the deployScript
	 */
	public String getDeployScript() {
		return deployScript;
	}

	/**
	 * @param deployScript
	 *            the deployScript to set
	 */
	public void setDeployScript(String deployScript) {
		this.deployScript = deployScript;
	}

	/**
	 * @return the initScript
	 */
	public String getInitScript() {
		return initScript;
	}

	/**
	 * @param initScript
	 *            the initScript to set
	 */
	public void setInitScript(String initScript) {
		this.initScript = initScript;
	}

	/**
	 * @return the centralizedlogFile
	 */
	public String getCentralizedlogFile() {
		return centralizedlogFile;
	}

	/**
	 * @param centralizedlogFile
	 *            the centralizedlogFile to set
	 */
	public void setCentralizedlogFile(String centralizedlogFile) {
		this.centralizedlogFile = centralizedlogFile;
	}

	/**
	 * @return the validationFile
	 */
	public String getValidationFile() {
		return validationFile;
	}

	/**
	 * @param validationFile
	 *            the validationFile to set
	 */
	public void setValidationFile(String validationFile) {
		this.validationFile = validationFile;
	}

	/**
	 * @return the resultFile
	 */
	public String getResultFile() {
		return resultFile;
	}

	/**
	 * @param resultFile
	 *            the resultFile to set
	 */
	public void setResultFile(String resultFile) {
		this.resultFile = resultFile;
	}

	/**
	 * @return the appRemoteLog
	 */
	public String getAppRemoteLog() {
		return appRemoteLog;
	}

	/**
	 * @param appRemoteLog
	 *            the appRemoteLog to set
	 */
	public void setAppRemoteLog(String appRemoteLog) {
		this.appRemoteLog = appRemoteLog;
	}

	/**
	 * @return the launchScript
	 */
	public String getLaunchScript() {
		return launchScript;
	}

	/**
	 * @param launchScript
	 *            the launchScript to set
	 */
	public void setLaunchScript(String launchScript) {
		this.launchScript = launchScript;
	}

	/**
	 * @return the argGenParamsFile
	 */
	public String getArgGenParamsFile() {
		return argGenParamsFile;
	}

	/**
	 * @param argGenParamsFile the argGenParamsFile to set
	 */
	public void setArgGenParamsFile(String argGenParamsFile) {
		this.argGenParamsFile = argGenParamsFile;
	}

	/**
	 * @return total number of events in this experiment
	 */
	public int getNumberOfEvents(){
		return events.size();
	}
}
