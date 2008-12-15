/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.churn.cc;

import gods.arch.Event;
import gods.arch.EventHandler;
import gods.arch.Module;
import gods.cc.ControlCenter;
import gods.churn.events.ApplicationKilledEvent;
import gods.topology.common.SlotInformation;
import gods.topology.common.SlotStatus;
import gods.topology.common.UnknownSlotException;
import gods.topology.events.SlotInformationChanged;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * The <code>ApplicationKilledEventChurnHandler</code> class handles the
 * ApplicationKilledEvent for Control Center's ChurnModule.
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class ApplicationKilledChurnEventHandler extends EventHandler {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger
			.getLogger(ApplicationKilledChurnEventHandler.class);

	/**
	 * @param event
	 * @param module
	 */
	public ApplicationKilledChurnEventHandler(Event event, Module module) {
		super(event, module);
	}

	/**
	 * {@link gods.arch.EventHandler#handle()}
	 */
	@Override
	public Object handle() throws ClassCastException {

		log.debug("");

		try {
			ApplicationKilledEvent applicationKilled = (ApplicationKilledEvent) event;

			SlotInformation[] slots = ControlCenter.getInstance().getSlots(
					applicationKilled.getSlotIds());

			for (SlotInformation slot : slots) {
				slot.setCommand("");
				slot.setProcessId(0);
				slot.setSlotStatus(SlotStatus.READY);
			}

			if (log.getLevel() == Level.DEBUG) {
				int[] pids = applicationKilled.getProcessIds();
				int[] slotids = applicationKilled.getSlotIds();

				for (int i = 0; (i < pids.length) && (i < slotids.length); i++) {
					log.debug("Application with Process Id: " + pids[i]
							+ " killed on Slot: " + slotids[i]);
				}
			}

			SlotInformationChanged changedSlotsInformation = new SlotInformationChanged(
					1);

			changedSlotsInformation.setChangedSlots(slots);
			ControlCenter.getInstance().enqueueEvent(changedSlotsInformation);

		} catch (UnknownSlotException use) {
			log.error(use.getMessage());
		}

		return null;
	}

}
