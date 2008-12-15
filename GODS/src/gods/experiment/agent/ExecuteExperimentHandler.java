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
import gods.agent.AgentProperties;
import gods.arch.Event;
import gods.arch.EventHandler;
import gods.arch.Module;
import gods.churn.agent.KillApplicationEventAgentChurnHandler;
import gods.churn.agent.LaunchApplicationEventAgentChurnHandler;
import gods.churn.agent.StopApplicationEventAgentChurnHandler;
import gods.churn.events.KillApplicationEvent;
import gods.churn.events.LaunchApplicationEvent;
import gods.churn.events.StopApplicationEvent;
import gods.experiment.ExperimentEvent;
import gods.experiment.TimeStamp;
import gods.experiment.events.ExecuteExperiment;
import gods.experiment.events.ExperimentFinished;
import gods.experiment.events.FailExperimentEvent;
import gods.experiment.events.JoinExperimentEvent;
import gods.experiment.events.LeaveExperimentEvent;
import gods.topology.common.UnknownSlotException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * The <code>StartExperimentHandler</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class ExecuteExperimentHandler extends EventHandler {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger
			.getLogger(ExecuteExperimentHandler.class);

	/**
	 * Script for synchronizing machine clocks
	 */
	private String syncScriptRelative = "scripts/syncclock.sh";

	/**
	 * Script for launching the application under test
	 */
	private String appLaunchScript = null;

	/**
	 * @param event
	 * @param module
	 */
	public ExecuteExperimentHandler(Event event, Module module) {
		super(event, module);
	}

	/**
	 * {@link gods.arch.EventHandler#handle()}
	 */
	@Override
	public Object handle() throws ClassCastException {

		log.info("EXECUTING EXPERIMENT...");

		try {
			
			ExecuteExperiment execExp = (ExecuteExperiment) event;
			AgentExperimentExecutor expModule = (AgentExperimentExecutor) module;

			appLaunchScript = execExp.getAppLaunchScript();

			log.debug("==================================================");
			log.debug("START EXPERIMENT TIME:" + System.currentTimeMillis());
			// syncClock();
			log.info("EXECUTING EXPERIMENT...");
			execute(expModule.getExperimentEvents());

			ExperimentFinished expFinished = new ExperimentFinished(1);
			expFinished
					.setHostName(Agent.getMachineInformation().getHostName());

			Agent.getInstance().notifyControlCenter(expFinished);
			log.debug("ExperimentFinished sent to ControlCenter");

		} catch (UnknownSlotException use) {
			log.error("UnknownSlotException: " + use.getMessage());

		} /*
			 * catch (IOException ioe) { log.error("InterruptedException: " +
			 * ioe.getMessage()); }
			 */catch (InterruptedException ie) {
			log.error("InterruptedException: " + ie.getMessage());
		}

		return null;
	}

	private void syncClock() throws IOException, InterruptedException {

		log.debug("SYNC============SYNC==========SYNC===========SYNC");

		String syncScript = AgentProperties.getAgentHome() + syncScriptRelative;
		String command = syncScript;
		log.debug(command);

		Process sync = Runtime.getRuntime().exec(command);

		InputStream stdout = new BufferedInputStream(sync.getInputStream());
		InputStream stderr = new BufferedInputStream(sync.getErrorStream());

		if (sync.waitFor() != 0) {
			log.debug("Output: " + streamToString(stdout));
			log.debug("Error: " + streamToString(stderr));
		}

	}

	private void execute(Vector<ExperimentEvent> experimentEvents)
			throws InterruptedException, UnknownSlotException {

		long startTime = System.currentTimeMillis();

		TimeStamp currentTime = new TimeStamp();
		TimeStamp timeForNextEvent;
		TimeStamp timeToSleep;
		long ms = 0;
		int ns = 0;
		int i = 0;
		long diff = 0, diffavg = 0, diffcnt = 0;

		log.debug("Initialized variables");

		for (ExperimentEvent expEvent : experimentEvents) {

			log.debug("Executing Event...");

			timeForNextEvent = expEvent.getTimeToHappen();
			long currentMillis = System.currentTimeMillis();
			currentTime.setSeconds(currentMillis / 1000);
			currentTime.setMicroseconds(((int) (currentMillis % 1000)) * 1000);

			timeToSleep = timeForNextEvent.difference(currentTime);

			if (timeToSleep.isPositive()
					&& (timeToSleep.getSeconds() * 1000
							+ timeToSleep.getMilliseconds() - diffavg > 0))
				Thread.sleep(timeToSleep.getSeconds() * 1000
						+ timeToSleep.getMilliseconds() - diffavg, timeToSleep
						.getNanoseconds());

			diff = processNow(expEvent);
			diffavg = (diffavg * diffcnt + diff) / (diffcnt + 1);
			diffcnt++;

			/*
			 * timeForNextEvent = expEvent.getTimeToHappen(); timeToSleep =
			 * timeForNextEvent.difference(currentTime);
			 * 
			 * ms = timeToSleep.getSeconds() * 1000 +
			 * timeToSleep.getMilliseconds(); log.debug("ms: " + ms); ns =
			 * timeToSleep.getNanoseconds(); Date stepTime = new Date(); long
			 * timefix = stepTime.getTime() - startTime; log.debug("Timefix is " +
			 * timefix); ms -= timefix; log.debug("ms: " + ms);
			 * log.debug("Sleeping For: " + ms + " milliseconds and " + ns + "
			 * nanoseconds"); Thread.sleep(ms, ns); startTime = new Date();
			 * currentTime = timeForNextEvent;
			 * 
			 * process(expEvent);
			 */
		}
	}

	private long processNow(ExperimentEvent expEvent) {

		Agent agent = Agent.getInstance();

		long diff = 0;

		if (expEvent instanceof JoinExperimentEvent) {
			JoinExperimentEvent joinExpEvent = (JoinExperimentEvent) expEvent;
			LaunchApplicationEvent launchApplication = new LaunchApplicationEvent(
					1);
			int appInstances = 1;
			// Set command line arguments for app that are not specified
			// in app launch script
			if (appLaunchScript == null) {
				log.debug("App Launch Script not specified");
				return 0;
			}
			launchApplication.setAppLaunchCommand(appLaunchScript);

			String[] args = new String[appInstances];
			args[0] = joinExpEvent.getAppArgs();
			launchApplication.setArguments(args);

			// Set slotId on which to run dummy app
			int[] slotids = new int[appInstances];
			slotids[0] = joinExpEvent.getVnid();
			launchApplication.setSlotIds(slotids);

			LaunchApplicationEventAgentChurnHandler handler = new LaunchApplicationEventAgentChurnHandler(
					launchApplication, null);

			long ts1 = System.currentTimeMillis();
			handler.handle();
			long ts2 = System.currentTimeMillis();
			log.debug("JOINEVENTHANDLINGTIME:" + (ts2 - ts1));

			diff = ts2 - ts1;

			// Enqueue this event in Agent
			// agent.enqueueEvent(launchApplication);
		}

		else if (expEvent instanceof LeaveExperimentEvent) {
			LeaveExperimentEvent leaveExpEvent = (LeaveExperimentEvent) expEvent;
			StopApplicationEvent stopApplication = new StopApplicationEvent(1);
			int count = 1;

			int[] slotids = new int[count];
			slotids[0] = leaveExpEvent.getVnid();
			stopApplication.setSlotIds(slotids);

			StopApplicationEventAgentChurnHandler handler = new StopApplicationEventAgentChurnHandler(
					stopApplication, null);

			long ts1 = System.currentTimeMillis();
			handler.handle();
			long ts2 = System.currentTimeMillis();
			log.debug("LEAVEEVENTHANDLINGTIME:" + (ts2 - ts1));

			diff = ts2 - ts1;

			// Enqueue this event in Agent
			// agent.enqueueEvent(stopApplication);
		}

		else if (expEvent.getClass() == FailExperimentEvent.class) {
			FailExperimentEvent failExpEvent = (FailExperimentEvent) expEvent;
			KillApplicationEvent killApplication = new KillApplicationEvent(1);
			int count = 1;

			int[] slotids = new int[count];
			slotids[0] = failExpEvent.getVnid();
			killApplication.setSlotIds(slotids);

			KillApplicationEventAgentChurnHandler handler = new KillApplicationEventAgentChurnHandler(
					killApplication, null);

			long ts1 = System.currentTimeMillis();
			handler.handle();
			long ts2 = System.currentTimeMillis();
			log.debug("FAILEVENTHANDLINGTIME:" + (ts2 - ts1));

			diff = ts2 - ts1;

			// Enqueue this event in Agent
			// agent.enqueueEvent(killApplication);
		}
		return diff;
	}

	private void process(ExperimentEvent expEvent) {

		Agent agent = Agent.getInstance();

		if (expEvent.getClass() == JoinExperimentEvent.class) {
			JoinExperimentEvent joinExpEvent = (JoinExperimentEvent) expEvent;
			LaunchApplicationEvent launchApplication = new LaunchApplicationEvent(
					1);
			int appInstances = 1;
			// Set command line arguments for app that are not specified
			// in app launch script
			if (appLaunchScript == null) {
				log.debug("App Launch Script not specified");
				return;
			}
			launchApplication.setAppLaunchCommand(appLaunchScript);

			String[] args = new String[appInstances];
			args[0] = joinExpEvent.getAppArgs();
			launchApplication.setArguments(args);

			// Set slotId on which to run dummy app
			int[] slotids = new int[appInstances];
			slotids[0] = joinExpEvent.getVnid();
			launchApplication.setSlotIds(slotids);
			// Enqueue this event in Agent
			agent.enqueueEvent(launchApplication);
		}

		else if (expEvent.getClass() == LeaveExperimentEvent.class) {
			LeaveExperimentEvent leaveExpEvent = (LeaveExperimentEvent) expEvent;
			StopApplicationEvent stopApplication = new StopApplicationEvent(1);
			int count = 1;

			int[] slotids = new int[count];
			slotids[0] = leaveExpEvent.getVnid();
			stopApplication.setSlotIds(slotids);
			// Enqueue this event in Agent
			agent.enqueueEvent(stopApplication);
		}

		else if (expEvent.getClass() == FailExperimentEvent.class) {
			FailExperimentEvent failExpEvent = (FailExperimentEvent) expEvent;
			KillApplicationEvent killApplication = new KillApplicationEvent(1);
			int count = 1;

			int[] slotids = new int[count];
			slotids[0] = failExpEvent.getVnid();
			killApplication.setSlotIds(slotids);
			// Enqueue this event in Agent
			agent.enqueueEvent(killApplication);
		}
	}
}
