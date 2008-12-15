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
import gods.cc.ControlCenter;
import gods.experiment.Experiment;
import gods.experiment.ExperimentEvent;
import gods.experiment.events.AbstractChurnExperimentEvent;
import gods.experiment.events.RunExperiment;
import gods.experiment.events.PrepareForExperiment;
import gods.topology.common.SlotInformation;
import gods.topology.common.UnknownSlotException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * The <code>PrepareExperimentHandler</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class RunExperimentHandler extends EventHandler {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger.getLogger(RunExperimentHandler.class);

	/**
	 * @param event
	 * @param module
	 */
	public RunExperimentHandler(Event event, Module module) {
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
			RunExperiment runExp = (RunExperiment) event;
			ExperimentExecutorModule expModule = (ExperimentExecutorModule) module;

			//log.info("LOADING EXPERIMENT...");
			Experiment experiment = runExp.getExperiment();
			
			experiment.dump();

			expModule.setExperiment(experiment);

			//log.info("DEPLOYING MODEL...");
			//deployModel(experiment.getNetModelPath());

			log.info("DEPLOYING APPLICATION...");
			deployApplication(experiment.getDeployScript());

			log.info("DISTRIBUTING EXPERIMENT to HOSTS...");
			Map<String, Vector<ExperimentEvent>> churnExpEvents = distribute(experiment);

			log.debug("CHURN EXPERIMENT EVENTS ARE DISTRIBUTED OVER "
					+ churnExpEvents.size() + " HOSTS");
			if (churnExpEvents.size() == 1) {
				/*
				 * String host =
				 * cc.getSlot(((AbstractChurnExperimentEvent)experiment
				 * .get(0)).getVnid()).getHostName();
				 */
				String host = churnExpEvents.keySet().iterator().next();
				log.debug("All experiment events belong to the HOST: " + host);
				PrepareForExperiment prepareExp = new PrepareForExperiment(1,
						churnExpEvents.get(host));
				prepareExp.setInitScript(experiment.getInitScript());
				ControlCenter.getInstance().notifyAgent(host, prepareExp);
			}

			// Otherwise, create PreparExperiment for each host in the map
			else {
				PrepareForExperiment prepareExp = null;
				int argsCounter = 0;
				Set<String> hosts = churnExpEvents.keySet();
				for (String host : hosts) {
					log.debug("Sending Experiment to " + host);
					prepareExp = new PrepareForExperiment(1, churnExpEvents
							.get(host));

					// if (log.getLevel() == Level.DEBUG) {
					Vector<ExperimentEvent> expEvents = prepareExp
							.getExperimentEvents();
					for (ExperimentEvent expEvent : expEvents) {
						log.debug(expEvent.toString());
					}
					// }

					ControlCenter.getInstance().notifyAgent(host, prepareExp);
				}
				log.debug("Experiment events sent to respective Agents...");
			}
			
			Vector<String> resAgents = new Vector<String>();
			resAgents.addAll(churnExpEvents.keySet());
			
			expModule.setResponsibleAgents(resAgents);

		} catch (UnknownSlotException use) {
			log.error("UnknownSlotException: " + use.getMessage());

		} catch (IOException ioe) {
			log.error("IOException: " + ioe.getMessage());

		} catch (InterruptedException ie) {
			log.error("InterruptedException: " + ie.getMessage());
		}

		return null;
	}

	private void deployModel(String netModelPath) throws IOException {

		String deployScript = GodsProperties
				.getAbsoluteProperty("net.deploy.script");

		Vector<String> machines = new Vector<String>();
		machines.addAll(GodsProperties.getHosts());
		machines.addAll(GodsProperties.getEmulators());

		Process deploy[] = new Process[machines.size()];

		int count = 0;
		for (String machine : machines) {
			String command = deployScript + " -h " + machine + " " + netModelPath;

			log.debug(command);

			deploy[count++] = Runtime.getRuntime().exec(command);
		}
	}

	private void deployApplication(String deployScript) throws IOException,
			InterruptedException {

		Process[] deploys = new Process[GodsProperties.getNumberOfHosts()];
		String command = "";
		int count = 0;

		for (String host : GodsProperties.getHosts()) {
			command = deployScript + " " + host;
			log.debug(command);
			deploys[count] = Runtime.getRuntime().exec(command);

			InputStream stdout = new BufferedInputStream(deploys[count]
					.getInputStream());
			InputStream stderr = new BufferedInputStream(deploys[count]
					.getErrorStream());

			if (deploys[count].waitFor() != 0) {
				System.out.println("Output: " + streamToString(stdout));
				System.out.println("Error: " + streamToString(stderr));
			}

		}
	}

	private Map<String, Vector<ExperimentEvent>> distribute(
			Experiment experiment) throws InterruptedException,
			UnknownSlotException {

		ControlCenter cc = ControlCenter.getInstance();

		// Map of hostnames to their experiment events
		Map<String, Vector<ExperimentEvent>> churnExpEvents = new HashMap<String, Vector<ExperimentEvent>>();

		ExperimentEvent expEvent = null;
		AbstractChurnExperimentEvent churnExpEvent = null;
		SlotInformation slot = null;

		int i = 0;
		while ((expEvent = experiment.get(i)) != null) {
			log.debug("Experiment Event " + i);
			churnExpEvent = (AbstractChurnExperimentEvent) expEvent;

			slot = cc.getSlot(churnExpEvent.getVnid());
			log.debug("is for host" + slot.getHostName());

			if (churnExpEvents.containsKey(slot.getHostName())) {
				churnExpEvents.get(slot.getHostName()).add(churnExpEvent);
				log.debug("Event " + i
						+ " added to vector of events for the host");

			} else {
				Vector<ExperimentEvent> hostExpEvents = new Vector<ExperimentEvent>();
				hostExpEvents.add(churnExpEvent);
				churnExpEvents.put(slot.getHostName(), hostExpEvents);
				log.debug("Event " + i + " is first event for host "
						+ slot.getHostName()
						+ ". Added host to events distribution map and "
						+ "event to vector of events for the host");
			}
			++i;
		}

		return churnExpEvents;

	}
}
