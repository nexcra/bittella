/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.experiment.cc;

import static gods.Gods.streamToString;

import gods.GodsProperties;
import gods.arch.Event;
import gods.arch.EventHandler;
import gods.arch.Module;
import gods.experiment.Experiment;
import gods.experiment.Validator;
import gods.experiment.events.ExperimentFinished;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

/**
 * The <code>ExperimentFinishedHandler</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class ExperimentFinishedHandler extends EventHandler {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger.getLogger(EndExperimentHandler.class);

	private static Set<String> hostsResponded = null;

	/**
	 * 
	 */
	public ExperimentFinishedHandler(Event event, Module module) {
		super(event, module);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gods.arch.EventHandler#handle()
	 */
	@Override
	public Object handle() throws ClassCastException {

		try {
			log.debug("");

			ExperimentExecutorModule expModule = (ExperimentExecutorModule) module;
			ExperimentFinished expFinished = (ExperimentFinished) event;
			Experiment experiment = expModule.getExperiment();

			log.debug("ExperimentFinished received from agent on host: "
					+ expFinished.getHostName());

			if (agentExperimentFinished(expFinished.getHostName()) == GodsProperties
					.getNumberOfHosts()) {
				log.info("EXPERIMENT FINISHED...");
				log.debug("==================================================");
				log.info("COMBINING LOGS...");
				//combineLogs(experiment.getCentralizedlogFile(), experiment
				//		.getAppRemoteLog());
				log.info("VALIDATING EXPERIMENT...");
				//validateExperiment(experiment);
			}

		} catch (/*IO*/Exception ioe) {
			log.error(ioe.getMessage());

		}/* catch (InterruptedException ie) {
			log.error(ie.getMessage());
		}*/
		return null;
	}

	private void combineLogs(String logFile, String remoteLogFile)
			throws IOException, InterruptedException {
		String combineLogsScript = GodsProperties
				.getAbsoluteProperty("combinelogs.script");

		String hostsFileName = GodsProperties.getGodsHome() + "hosts";
		
		File hostsFile = new File(hostsFileName);
		hostsFile.createNewFile();
		BufferedWriter hostsWriter = new BufferedWriter(new FileWriter(
				hostsFile));
		
		for(String host:GodsProperties.getHosts()){
			hostsWriter.write(host + "\n");
		}
		hostsWriter.flush();
		hostsWriter.close();
		
		String command = combineLogsScript + " " + remoteLogFile + " "
				+ logFile + " " + hostsFileName;
		log.debug(command);

		Process combine = Runtime.getRuntime().exec(command);

		InputStream stdout = new BufferedInputStream(combine.getInputStream());
		InputStream stderr = new BufferedInputStream(combine.getErrorStream());

		if (combine.waitFor() != 0) {
			System.out.println("Output: " + streamToString(stdout));
			System.out.println("Error: " + streamToString(stderr));
		}
		hostsFile.delete();
	}

	private void validateExperiment(Experiment experiment) throws IOException {

		if (Validator.validate(experiment.getCentralizedlogFile(), experiment
				.getValidationFile(), experiment.getResultFile())) {

			log.info("Experiment Validated and results stored in "
					+ experiment.getResultFile());

		} else {
			log.debug("The Experiment Validation failed." + "Look in "
					+ experiment.getResultFile() + " for details");
		}

	}

	static int agentExperimentFinished(String hostName) {
		if (hostsResponded == null) {
			hostsResponded = new TreeSet<String>();
		}

		hostsResponded.add(hostName);
		int noOfHostsResponded = hostsResponded.size();
		if (noOfHostsResponded == GodsProperties.getNumberOfHosts()) {
			hostsResponded = null;
		}
		return noOfHostsResponded;
	}
}
