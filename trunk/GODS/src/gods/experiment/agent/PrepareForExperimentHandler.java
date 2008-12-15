/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.experiment.agent;

import static gods.Gods.streamToString;

import gods.agent.Agent;
import gods.arch.Event;
import gods.arch.EventHandler;
import gods.arch.Module;
import gods.experiment.ExperimentEvent;
import gods.experiment.events.PrepareForExperiment;
import gods.experiment.events.ReadyForExperiment;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * The <code>PrepareForExperimentHandler</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class PrepareForExperimentHandler extends EventHandler {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger
			.getLogger(PrepareForExperimentHandler.class);

	/**
	 * @param event
	 * @param module
	 */
	public PrepareForExperimentHandler(Event event, Module module) {
		super(event, module);
	}

	/**
	 * {@link gods.arch.EventHandler#handle()}
	 */
	@Override
	public Object handle() throws ClassCastException {

		log.debug("");

		PrepareForExperiment prepareExp = (PrepareForExperiment) event;
		AgentExperimentExecutor expModule = (AgentExperimentExecutor) module;

		expModule.setExperimentEvents(prepareExp.getExperimentEvents());

		log.debug("Experiment Events for "
				+ Agent.getMachineInformation().getHostName() + " are: ");
		// if (log.getLevel() == Level.DEBUG) {
		Vector<ExperimentEvent> expEvents = prepareExp.getExperimentEvents();
		for (ExperimentEvent expEvent : expEvents) {
			log.debug(expEvent.toString());
		}
		// }
		
		//initApplication();
		
		ReadyForExperiment readyExp = new ReadyForExperiment(1);
		readyExp.setHostName(Agent.getMachineInformation().getHostName());
		Agent.getInstance().notifyControlCenter(readyExp);
		log.debug("ReadyForExperiment sent to ControlCenter");

		return null;
	}

	private void initApplication(String initScript) throws IOException,
			InterruptedException {

		log.debug(initScript);
		Process cleanUp = Runtime.getRuntime().exec(initScript);

		InputStream stdout = new BufferedInputStream(cleanUp.getInputStream());
		InputStream stderr = new BufferedInputStream(cleanUp.getErrorStream());

		if (cleanUp.waitFor() != 0) {
			System.out.println("Output: " + streamToString(stdout));
			System.out.println("Error: " + streamToString(stderr));
		}

	}

}
