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
import gods.churn.events.ApplicationLaunchedEvent;
import gods.topology.common.SlotInformation;
import gods.topology.common.SlotStatus;
import gods.topology.common.UnknownSlotException;
import gods.topology.events.SlotInformationChanged;

import org.apache.log4j.Logger;

/**
 * The <code>ApplicationLaunchedChurnEventHandler</code> class handles the
 * ApplicationLaunchedEvent for Control Center's ChurnModule.
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class ApplicationLaunchedChurnEventHandler extends EventHandler {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger
			.getLogger(ApplicationLaunchedChurnEventHandler.class);

	/**
	 * @param event
	 * @param module
	 */
	public ApplicationLaunchedChurnEventHandler(Event event, Module module) {
		super(event, module);
	}

	/**
	 * {@link gods.arch.EventHandler#handle()}
	 */
	@Override
	public Object handle() throws ClassCastException {

		log.debug("");

		try {

			ApplicationLaunchedEvent applicationLaunched = (ApplicationLaunchedEvent) event;

			int[] slotIds = applicationLaunched.getSlotIds();
			int[] processIds = applicationLaunched.getProcessIds();

			SlotInformation[] slots = ControlCenter.getInstance().getSlots(
					applicationLaunched.getSlotIds());
			int count = 0;
			for (SlotInformation slot : slots) {

				if (slot.getSlotId() == slotIds[count]) {
					slot.setProcessId(processIds[count++]);
					slot.setCommand(applicationLaunched.getAppLaunchCommand());
					slot.setSlotStatus(SlotStatus.BUSY);
					
					log.debug("Application: "
							+ applicationLaunched.getAppLaunchCommand()
							+ " launched on Slot: " + slot.getSlotId());
				}
			}

			SlotInformationChanged changedSlotsInformation = new SlotInformationChanged(1);
			changedSlotsInformation.setChangedSlots(slots);
			ControlCenter.getInstance().enqueueEvent(changedSlotsInformation);
			
		} catch (UnknownSlotException use) {
			log.error(use.getMessage());
		}

		return null;
	}
}
