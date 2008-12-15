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
import gods.churn.events.ApplicationKilledEvent;
import gods.churn.events.KillApplicationEvent;
import gods.topology.common.SlotInformation;
import gods.topology.common.SlotStatus;
import gods.topology.common.UnknownSlotException;

import java.io.IOException;

import org.apache.log4j.Logger;

/**
 * The <code>KillApplicationEventAgentChurnHandler</code> class handles the
 * KillApplicationEvent for AgentChurnModule.
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class KillApplicationEventAgentChurnHandler extends EventHandler {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger
			.getLogger(KillApplicationEventAgentChurnHandler.class);

	/**
	 * @param event
	 * @param module
	 */
	public KillApplicationEventAgentChurnHandler(Event event, Module module) {
		super(event, module);
	}

	/**
	 * {@link gods.arch.EventHandler#handle()}
	 */
	@Override
	public Object handle() throws ClassCastException {

		log.debug("");

		try {
			KillApplicationEvent killEvent = (KillApplicationEvent) event;

			String killScript = AgentProperties.getKillScript();
			String command = "";

			int[] slotids = killEvent.getSlotIds();
			SlotInformation[] slots = Agent.getInstance().getSlots(slotids);
			int pid = 0;

			for(SlotInformation slot : slots){
				
				pid = slot.getProcessId();
				command = killScript + " " + pid;
				Process killApp = Runtime.getRuntime().exec(command);

				int exitValue = killApp.waitFor();

				if (exitValue != 0) {
					log.error("Could not Kill Application with Process id: "
							+ pid + " on Slot: " + slot.getSlotId());
					continue;
				}

				//Update slot data
				slot.setCommand("");
				slot.setProcessId(0);
				slot.setSlotStatus(SlotStatus.READY);

			}

			//Send App Killed Event to ControlCenter for update 
			ApplicationKilledEvent killedEvent = new ApplicationKilledEvent(1);

			killedEvent.setSlotIds(killEvent.getSlotIds());
			killedEvent.setProcessIds(killEvent.getProcessIds());

			Agent.getInstance().notifyControlCenter(killedEvent);

		} catch (UnknownSlotException use) {
			log.error(use.getMessage());

		} catch (IOException ioe) {
			log.error(ioe.getMessage());

		} catch (InterruptedException ie) {
			log.error(ie.getMessage());
		}

		return null;
	}
}
