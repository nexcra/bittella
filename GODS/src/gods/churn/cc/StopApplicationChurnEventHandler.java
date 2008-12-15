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
import gods.churn.events.StopApplicationEvent;
import gods.topology.common.SlotInformation;
import gods.topology.common.UnknownSlotException;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * The <code>StopApplicationEventChurnHandler</code> class handles the
 * StopApplicationEvent for Control Center's ChurnModule.
 *
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class StopApplicationChurnEventHandler extends EventHandler {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger
			.getLogger(StopApplicationChurnEventHandler.class);
	
	/**
	 * @param event
	 * @param module
	 */
	public StopApplicationChurnEventHandler(Event event, Module module) {
		super(event, module);
	}

	/**
	 * {@link gods.arch.EventHandler#handle()}
	 */
	@Override
	public Object handle() throws ClassCastException {
		
		log.debug("");
		
		try {
			StopApplicationEvent stopEvent = (StopApplicationEvent) event;

			int[] slotIds = stopEvent.getSlotIds();
			
			if (slotIds == null) {
				log.error("Received null in slotIds of StopApplicationEvent");
			}

			// Otherwise, if there is only one slotId, just forward it to the
			// agent responsible for the slot
			else if (slotIds.length == 1) {
				SlotInformation slot = ControlCenter.getInstance().getSlot(
						slotIds[0]);

				log.debug("Stop Application on Slot: " + slotIds[0]);
				ControlCenter.getInstance().notifyAgent(slot.getHostName(),
						stopEvent);
			}
			
			// Or else create a map of host machines to the LaunchEvent that
			// needs to be sent to them
			else {
				SlotInformation[] slots = ControlCenter.getInstance().getSlots(
						slotIds);

				// Map of host to slotIds
				Map<String, Vector<Integer>> stopEvents = new HashMap<String, Vector<Integer>>();
				for (SlotInformation slot : slots) {
					if (stopEvents.containsKey(slot.getHostName())) {
						stopEvents.get(slot.getHostName()).add(
								slot.getSlotId());

					} else {
						Vector<Integer> hostSlots = new Vector<Integer>();
						hostSlots.ensureCapacity(slots.length);
						hostSlots.add(slot.getSlotId());
						stopEvents.put(slot.getHostName(), hostSlots);

					}
				}

				// If all slots belong to one machine then send the LaunchEvent
				// as it is to the corresponding agent
				if (stopEvents.size() == 1) {
					ControlCenter.getInstance().notifyAgent(
							slots[0].getHostName(), stopEvent);
				}

				// Otherwise, create StopAppEvent for each host in the map
				else {
					for (String host : stopEvents.keySet()) {
						StopApplicationEvent stopAppEvent = new StopApplicationEvent(
								1);

						Integer[] hostSlotIds = new Integer[stopEvents.get(
								host).size()];
						stopEvents.get(host).toArray(hostSlotIds);

						int[] intHostSlotIds = new int[hostSlotIds.length];
						int count = 0;
						for (Integer hostSlotId : hostSlotIds) {
							intHostSlotIds[count++] = hostSlotId;
						}
						stopAppEvent.setSlotIds(intHostSlotIds);

						ControlCenter.getInstance().notifyAgent(host,
								stopAppEvent);
					}
				}
			}

		} catch (UnknownSlotException use) {
		
			log.error(use.getMessage());
		}
		
		return null;
	}

}
