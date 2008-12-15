/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.churn.agent;

import gods.agent.Agent;
import gods.agent.AgentProperties;
import gods.arch.Event;
import gods.arch.EventHandler;
import gods.arch.Module;
import gods.churn.events.ApplicationLaunchedEvent;
import gods.churn.events.LaunchApplicationEvent;
import gods.topology.common.SlotInformation;
import gods.topology.common.SlotStatus;
import gods.topology.common.UnknownSlotException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import static gods.Gods.streamToString;

/**
 * The <code>LaunchApplicationEventAgentChurnHandler</code> class handles the
 * LaunchApplicationEvent for AgentChurnModule.
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class LaunchApplicationEventAgentChurnHandler extends EventHandler {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger
			.getLogger(LaunchApplicationEventAgentChurnHandler.class);

	/**
	 * @param event
	 * @param module
	 */
	public LaunchApplicationEventAgentChurnHandler(Event event, Module module) {
		super(event, module);
	}

	/**
	 * {@link gods.arch.EventHandler#handle()}
	 */
	@Override
	public Object handle() throws ClassCastException {

		log.debug("");

		try {
			LaunchApplicationEvent launchEvent = (LaunchApplicationEvent) event;
			String launchScript = AgentProperties.getLaunchScript();
			SlotInformation[] slots = Agent.getInstance().getSlots(
					launchEvent.getSlotIds());

			String args[] = launchEvent.getArguments();

			Process[] apps = new Process[slots.length];
			InputStream[] stdout = new InputStream[slots.length];
			InputStream[] stderr = new InputStream[slots.length];
			int count = 0;

			// Map of slotId to processId of processes just being launched
			Map<Integer, Integer> processOnSlot = new HashMap<Integer, Integer>();
			int argsCounter = 0;
			for (SlotInformation slot : slots) {

				String arg = "";
				if ((args != null) && (args.length > argsCounter)) {
					arg = args[argsCounter++];
				}
				String command = launchScript + " "
						+ launchEvent.getAppLaunchCommand() + " " + arg + " "
						+ slot.getVirtNodeAddress();

				log.debug(command);

				apps[count] = Runtime.getRuntime().exec(command);
				stdout[count] = new BufferedInputStream(apps[count]
						.getInputStream());
				stderr[count] = new BufferedInputStream(apps[count]
						.getErrorStream());

				int exitValue = apps[count].waitFor();

				String processOutput = streamToString(stdout[count]);

				if (exitValue == 0) {

					log.debug("The exact output is: \"" + processOutput + "\"");

					int processId = Integer.parseInt(processOutput);

					log.info("Application launched on slot " + slot.getSlotId()
							+ "with ProcessId " + processId);

					processOnSlot.put(slot.getSlotId(), processId);

					slot.setCommand(launchEvent.getAppLaunchCommand());
					slot.setProcessId(processId);
				} else {
					log
							.error("Encountered while launching application on Slot: "
									+ slot.getSlotId());

					log.error(processOutput);
				}

				count++;
				//Thread.sleep(1000);
			}

			//Update slots information on Agent
			int[] slotIds = new int[processOnSlot.size()];
			int[] processIds = new int[processOnSlot.size()];
			SlotInformation slot = null;
			count = 0;

			for (Integer slotId : processOnSlot.keySet()) {
				slotIds[count] = slotId;
				processIds[count] = processOnSlot.get(slotId);

				slot = Agent.getInstance().getSlot(slotId);
				slot.setProcessId(processIds[count++]);
				slot.setCommand(launchEvent.getAppLaunchCommand());
				slot.setSlotStatus(SlotStatus.BUSY);

				++count;
			}

			//Send Application Launched Event to ControlCenter for update
			ApplicationLaunchedEvent applicationLaunched = new ApplicationLaunchedEvent(
					1);
			applicationLaunched.setSlotIds(slotIds);
			applicationLaunched.setProcessIds(processIds);
			applicationLaunched.setAppLaunchCommand(launchEvent
					.getAppLaunchCommand());

			Agent.getInstance().notifyControlCenter(applicationLaunched);

		} catch (UnknownSlotException use) {
			log.error(use.getMessage());
		} catch (InterruptedException ie) {

			log.fatal("Interrupted while deploying Agents " + ie.getMessage());
			return 0;
		} catch (IOException ioe) {
			log.error("IOException while launching application"
					+ ioe.getMessage());
		} catch (NumberFormatException nfe) {
			log.error(nfe.getMessage());
		}
		return null;
	}
}
