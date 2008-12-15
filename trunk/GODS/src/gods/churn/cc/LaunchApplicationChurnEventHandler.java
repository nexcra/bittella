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
import gods.churn.events.LaunchApplicationEvent;
import gods.topology.common.SlotInformation;
import gods.topology.common.UnknownSlotException;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * The <code>LaunchApplicationEventChurnHandler</code> class handles the
 * LaunchApplicationEvent for ControlCenter's Churn Module.
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class LaunchApplicationChurnEventHandler extends EventHandler {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger
			.getLogger(LaunchApplicationChurnEventHandler.class);

	/**
	 * @param event
	 * @param module
	 */
	public LaunchApplicationChurnEventHandler(Event event, Module module) {
		super(event, module);
	}

	/**
	 * {@link gods.arch.EventHandler#handle()} Gets slot information for slot
	 * specified in LaunchApplicationEvent, gets the machine on which this slot
	 * is hosted and sends a LaunchApplicationEvent to the agent on that
	 * machine.
	 */
	@Override
	public Object handle() throws ClassCastException {

		log.debug("");

		try {
			LaunchApplicationEvent launchEvent = (LaunchApplicationEvent) event;

			int[] slotIds = launchEvent.getSlotIds();
			// Just log and return if there are no solt ids in the LaunchEvent
			if (slotIds == null) {
				log.error("Received null in slotIds of LaunchApplicationEvent");
			}

			// Otherwise, if there is only one slotId, just forward it to the
			// agent responsible for the slot
			else if (slotIds.length == 1) {
				SlotInformation slot = ControlCenter.getInstance().getSlot(
						slotIds[0]);

				log.debug("Launch Application on Slot: " + slotIds[0]);
				ControlCenter.getInstance().notifyAgent(slot.getHostName(),
						launchEvent);
			}

			// Or else create a map of host machines to the LaunchEvent that
			// needs to be sent to them
			else {
				SlotInformation[] slots = ControlCenter.getInstance().getSlots(
						slotIds);

				// Map of host to slotIds
				Map<String, Vector<Integer>> launchEvents = slotToHostDistribution(slots);

				// If all slots belong to one machine then send the LaunchEvent
				// as it is to the corresponding agent
				if (launchEvents.size() == 1) {
					ControlCenter.getInstance().notifyAgent(
							slots[0].getHostName(), launchEvent);
				}

				// Otherwise, create LaunchEvent for each host in the map
				else {
					int argsCounter = 0;
					for (String host : launchEvents.keySet()) {
						LaunchApplicationEvent launchAppEvent = new LaunchApplicationEvent(
								1);

						launchAppEvent.setAppLaunchCommand(launchEvent
								.getAppLaunchCommand());

						// Set slotIds array in LaunchEvent
						Integer[] hostSlotIds = new Integer[launchEvents.get(
								host).size()];
						launchEvents.get(host).toArray(hostSlotIds);

						int[] intHostSlotIds = new int[hostSlotIds.length];
						int count = 0;
						for (Integer hostSlotId : hostSlotIds) {
							intHostSlotIds[count++] = hostSlotId;
						}
						launchAppEvent.setSlotIds(intHostSlotIds);

						String[] args = null;
						if (launchEvent.getArguments() != null) {
							// Set args array in Launch Event
							args = new String[launchEvents.get(host)
									.size()];
							for (int i = 0; i < args.length; i++) {
								args[i] = launchEvent.getArguments()[argsCounter++];
							}
							
						}
						launchAppEvent.setArguments(args);

						ControlCenter.getInstance().notifyAgent(host,
								launchAppEvent);
					}
				}
			}

		} catch (UnknownSlotException use) {
			log.error(use.getMessage());
		}

		return null;
	}

	private Map<String, Vector<Integer>> slotToHostDistribution(
			SlotInformation[] slots) {
		// Map of host to slotIds
		Map<String, Vector<Integer>> launchEvents = new HashMap<String, Vector<Integer>>();
		for (SlotInformation slot : slots) {
			if (launchEvents.containsKey(slot.getHostName())) {
				launchEvents.get(slot.getHostName()).add(slot.getSlotId());

			} else {
				Vector<Integer> hostSlots = new Vector<Integer>();
				hostSlots.ensureCapacity(slots.length);
				hostSlots.add(slot.getSlotId());
				launchEvents.put(slot.getHostName(), hostSlots);

			}
		}
		return launchEvents;
	}
}
