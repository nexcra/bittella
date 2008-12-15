/**
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.deploy.agent;

import gods.agent.Agent;
import gods.arch.Event;
import gods.arch.EventHandler;
import gods.arch.Module;
import gods.deploy.events.PrepareEvent;
import gods.deploy.events.ReadyEvent;
import gods.topology.common.SlotInformation;
import gods.topology.common.SlotStatus;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * The <code>PrepareEventAgentDeploymentHandler</code> class is responsible
 * for handling the PrepareEvent sent to an Agent by ControlCenter for
 * AgentDeploymentModule
 * 
 * @author Ozair Kafray
 * @version $Id$
 */
public class PrepareEventAgentDeploymentHandler extends EventHandler {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger
			.getLogger(PrepareEventAgentDeploymentHandler.class);

	/**
	 * An int value for comparing first byte of InetAddresses, which shows that
	 * the InetAddress belongs to virtNetAddress.*.*.*
	 */
	private static short virtNetAddress = 10; // meaning 10.*.*.*

	/**
	 * @param event
	 * @param module
	 */
	public PrepareEventAgentDeploymentHandler(Event event, Module module) {
		super(event, module);
	}

	/**
	 * Handles the PrepareEvent. The Network Addresses of the Virtual Network
	 * are enumerated between the ids specified in slotsRange
	 * 
	 * {@link gods.arch.EventHandler#handle()}
	 */
	@Override
	public Object handle() throws ClassCastException {

		log.debug("");

		PrepareEvent prepareEvent = (PrepareEvent) event;
		int[] slotsRange = prepareEvent.getSlotsRange();

		log.info("Received Slot Range: [" + slotsRange[0] + " - "
				+ slotsRange[1] + "]");

		SlotInformation[] slotsInformation = initializeSlotsInformation(slotsRange);

		Agent.getInstance().setSlotsInformation(slotsRange, slotsInformation);

		ReadyEvent readyEvent = new ReadyEvent(1, slotsInformation);
		readyEvent.setHostName(Agent.getMachineInformation().getHostName());

		Agent.getInstance().notifyControlCenter(readyEvent);

		return null;
	}

	/**
	 * Initializes Slots Information Array.
	 * 
	 * @param slotsRange
	 *            range of slots on this machine
	 */
	private SlotInformation[] initializeSlotsInformation(int[] slotsRange) {

		SlotInformation[] slotsInformation = null;
		int numberOfSlots = (slotsRange[1] - slotsRange[0]) + 1;
		log.debug("Slots Required by CC: " + numberOfSlots);
		
		Vector<String> virtualNetAddresses = getVirtualNetworkAddresses();
		if (numberOfSlots <= virtualNetAddresses.size()) {
			slotsInformation = new SlotInformation[numberOfSlots];
		} else {
			slotsInformation = new SlotInformation[virtualNetAddresses.size()];
		}

		log.debug("Number Of Slots: " + slotsInformation.length);

		int slotId = slotsRange[0];
		// for (SlotInformation slot : slotsInformation)
		for (int i = 0, j = 0; (i < slotsInformation.length)
				&& (j < virtualNetAddresses.size()); i++, j++) {

			slotsInformation[i] = new SlotInformation(slotId, Agent
					.getMachineInformation().getHostName(), virtualNetAddresses
					.get(j));

			log.debug(slotsInformation[i].toString());

			slotsInformation[i].setSlotStatus(SlotStatus.READY);

			slotId++;
		}

		return slotsInformation;
	}

	/**
	 * @return A vector of all addresses belonging to the virtual network for
	 *         Gods experiment
	 */
	private Vector<String> getVirtualNetworkAddresses() {

		Vector<String> virtNetAddrs = new Vector<String>();

		try {
			Enumeration<NetworkInterface> nifs = NetworkInterface
					.getNetworkInterfaces();

			while (nifs.hasMoreElements()) {

				NetworkInterface nif = nifs.nextElement();
				Enumeration<InetAddress> inaddrs = nif.getInetAddresses();
				log.info("Network Interface Display Name: "
						+ nif.getDisplayName());

				while (inaddrs.hasMoreElements()) {
					InetAddress inaddr = inaddrs.nextElement();

					if (((Byte) inaddr.getAddress()[0]).shortValue() == virtNetAddress) {
						virtNetAddrs.add(inaddr.getHostAddress());
					}
				}
			}
		} catch (SocketException se) {

			log.error(se.getMessage());
		}

		return virtNetAddrs;
	}
}
