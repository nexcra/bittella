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
import gods.churn.events.KillApplicationEvent;
import gods.topology.common.SlotInformation;
import gods.topology.common.UnknownSlotException;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * The <code>KillApplicationEventChurnHandler</code> class handles the
 * KillApplicationEvent for Control Center's ChurnModule.
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class KillApplicationChurnEventHandler extends EventHandler {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger
			.getLogger(KillApplicationChurnEventHandler.class);

	/**
	 * @param event
	 * @param module
	 */
	public KillApplicationChurnEventHandler(Event event, Module module) {
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

			int[] slotIds = killEvent.getSlotIds();
			
			if ((slotIds == null) || (slotIds.length == 0)) {
				log.error("Received no slotIds in KillApplicationEvent");
			}

			// Otherwise, if there is only one slotId, just forward it to the
			// agent responsible for the slot
			else if (slotIds.length == 1) {
				SlotInformation slot = ControlCenter.getInstance().getSlot(
						slotIds[0]);

				log.debug("Kill Application on Slot: " + slotIds[0]);
				ControlCenter.getInstance().notifyAgent(slot.getHostName(),
						killEvent);
			}
			
			// Or else create a map of host machines to the LaunchEvent that
			// needs to be sent to them
			else {
				SlotInformation[] slots = ControlCenter.getInstance().getSlots(
						slotIds);

				// Map of host to slotIds
				Map<String, Vector<Integer>> killEvents = new HashMap<String, Vector<Integer>>();
				for (SlotInformation slot : slots) {
					if (killEvents.containsKey(slot.getHostName())) {
						killEvents.get(slot.getHostName()).add(
								slot.getSlotId());

					} else {
						Vector<Integer> hostSlots = new Vector<Integer>();
						hostSlots.ensureCapacity(slots.length);
						hostSlots.add(slot.getSlotId());
						killEvents.put(slot.getHostName(), hostSlots);

					}
				}

				// If all slots belong to one machine then send the LaunchEvent
				// as it is to the corresponding agent
				if (killEvents.size() == 1) {
					ControlCenter.getInstance().notifyAgent(
							slots[0].getHostName(), killEvent);
				}

				// Otherwise, create KillAppEvent for each host in the map
				else {
					for (String host : killEvents.keySet()) {
						KillApplicationEvent killAppEvent = new KillApplicationEvent(
								1);

						Integer[] hostSlotIds = new Integer[killEvents.get(
								host).size()];
						killEvents.get(host).toArray(hostSlotIds);

						int[] intHostSlotIds = new int[hostSlotIds.length];
						int count = 0;
						for (Integer hostSlotId : hostSlotIds) {
							intHostSlotIds[count++] = hostSlotId;
						}
						killAppEvent.setSlotIds(intHostSlotIds);

						ControlCenter.getInstance().notifyAgent(host,
								killAppEvent);
					}
				}
			}

		} catch (UnknownSlotException use) {
		
			log.error(use.getMessage());
		}
		
		return null;
	}

}
