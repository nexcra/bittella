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

import gods.GodsProperties;
import gods.arch.Event;
import gods.arch.EventHandler;
import gods.arch.Module;
import gods.cc.ControlCenter;
import gods.experiment.events.ExecuteExperiment;
import gods.experiment.events.ReadyForExperiment;

import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * The <code>ReadyForExperimentHandler</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class ReadyForExperimentHandler extends EventHandler {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger
			.getLogger(ReadyForExperimentHandler.class);

	private static Set<String> hostsResponded = null;

	/**
	 * @param event
	 * @param module
	 */
	public ReadyForExperimentHandler(Event event, Module module) {
		super(event, module);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gods.arch.EventHandler#handle()
	 */
	@Override
	public Object handle() throws ClassCastException {

		log.debug("");

		ExperimentExecutorModule expModule = (ExperimentExecutorModule) module;
		ReadyForExperiment readyExp = (ReadyForExperiment) event;

		log.debug("ReadyForExperiment received from agent on host: "
				+ readyExp.getHostName());

		if (agentReadyForExperiment(readyExp.getHostName()) == expModule
				.getResponsibleAgents().size()) {
			ExecuteExperiment execExp = new ExecuteExperiment(1);
			execExp.setAppLaunchScript(expModule.getExperiment()
					.getLaunchScript());

			Vector<String> hosts = GodsProperties.getHosts();
			ControlCenter.getInstance().executeTaskOnAll();
			ControlCenter.getInstance().notifyAllAgents(execExp);

		}

		return null;
	}

	private static int agentReadyForExperiment(String hostName) {
		if (hostsResponded == null) {
			hostsResponded = new TreeSet<String>();
		}
		log.debug("");
		hostsResponded.add(hostName);
		int noOfHostsResponded = hostsResponded.size();
		if (noOfHostsResponded == GodsProperties.getNumberOfHosts()) {
			hostsResponded = null;
		}
		return noOfHostsResponded;
	}
}
