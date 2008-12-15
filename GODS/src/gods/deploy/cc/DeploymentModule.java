/**
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.deploy.cc;

import gods.GodsProperties;
import gods.arch.AbstractModule;
import gods.arch.Subscription;
import gods.cc.ControlCenter;
import gods.cc.GodsStatus;
import gods.deploy.events.BootEvent;
import gods.deploy.events.JoinedEvent;
import gods.deploy.events.PrepareEvent;
import gods.deploy.events.ReadyEvent;
import gods.topology.cc.AgentInformation;
import gods.topology.common.MachineStatus;
import gods.topology.common.SlotInformation;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

/**
 * The <code>DeploymentModule</code> class is the ControlCenter's module for
 * inititializing ControlCenter, deploying Agents and starting them. It
 * exchanges deployment events with Agents until they are in the Ready state.
 * 
 * @author Ozair Kafray
 * @version $Id$
 */
public class DeploymentModule extends AbstractModule {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger.getLogger(DeploymentModule.class);

	private Set<SlotInformation> slotsInformation = new TreeSet<SlotInformation>();

	/**
	 * Sets the thread name to module name
	 */
	public DeploymentModule() {
		super(DeploymentModule.class.getSimpleName());
	}

	/**
	 * {@link gods.arch.AbstractModule#initialize()}
	 */
	@Override
	public void initialize() {
		// Initialize Modules Local variables here

		// Subscribe to events
		subscribe();
	}

	private void subscribe() {
		log.debug(this.getClass() + " subscribing Events...");
		// Create List of subscriptions for events of interest
		List<Subscription> subscriptions = new LinkedList<Subscription>();

		// Fill in subscriptions including reference to this module, events and
		// corresponding EventHandlers

		// Add BootEvent to subscription list
		Subscription subscription = new Subscription(this, BootEvent.class,
				BootEventDeploymentHandler.class);
		subscriptions.add(subscription);

		// Add JoinedEvent to subscription list
		subscription = new Subscription(this, JoinedEvent.class,
				JoinedEventDeploymentHandler.class);
		subscriptions.add(subscription);

		// Add ReadyEvent to subscription list
		subscription = new Subscription(this, ReadyEvent.class,
				ReadyEventDeploymentHandler.class);
		subscriptions.add(subscription);

		// Subscribing Events
		ControlCenter.getInstance().subscribe(subscriptions);
	}

	/**
	 * Notifies ControlCenter that an Agent has joined. It looks up for Agents
	 * Remote reference in RMI Registry, and just returns until it receives same
	 * notification from all agents. If all agents have joined then it updates
	 * Gods Status to Preparing
	 * 
	 * @param hostName
	 *            of machine on which the agent is running
	 */
	public void agentJoined(String hostName) {

		AgentInformation agentInformation = ControlCenter.getAgentInformation(hostName);
		try {
			if (agentInformation != null) {

				// Lookup for the agent in RMI registry
				agentInformation.findAgent();
				agentInformation.setAgentStatus(MachineStatus.JOINED);

				int count = 0;
				for (String host : GodsProperties.getHosts()) {
					if (ControlCenter.getAgentInformation(host)
							.getAgentStatus() == MachineStatus.JOINED) {
						count++;
					} else {
						break;
					}
				}
				if (count == GodsProperties.getNumberOfHosts()) {
					prepareAgents();
				}

			} else {
				log.debug("Host " + hostName
						+ " not known to GODS Control Center."
						+ "Check model.machines file in gods.net.model.dir");
			}
		} catch (MalformedURLException mle) {
			log.error("Unable to find agent on host: "
					+ agentInformation.getAgentName() + " " + mle.getMessage());

		} catch (RemoteException re) {
			log.error("Unable to find agent: "
					+ agentInformation.getAgentName() + " " + re.getMessage());

		} catch (NotBoundException nbe) {
			log.error("Unable to find agent:" + agentInformation.getAgentName()
					+ " " + nbe.getMessage());
		}
	}

	/**
	 * Sends Prepare Event to all agents after ControlCenter has received a
	 * Joined Event from each of them.
	 */
	private void prepareAgents() {
		GodsProperties.setGodsStatus(GodsStatus.PREPARING);

		// Send prepare events to all agents
		for (String host : GodsProperties.getHosts()) {

			PrepareEvent prepareEvent = new PrepareEvent(1);

			int[] slotsRange = getSlotRangeFor(host);
			prepareEvent.setSlotsRange(slotsRange);

			ControlCenter.getInstance().notifyAgent(host, prepareEvent);
			log.info("Sent Prepare Event to:" + host + " with slots range: ["
					+ slotsRange[0] + " - " + slotsRange[1] + "]");
		}
	}

	/**
	 * Notifies ControlCenter that an Agent is Ready. If all are Ready then it
	 * updates Gods Status to Ready, from where the experiments can start.
	 * 
	 * @param hostName
	 */
	public void agentReady(String hostName, SlotInformation[] slots) {

		AgentInformation agentInformation = null;

		if ((agentInformation = ControlCenter.getAgentInformation(hostName)) != null) {

			agentInformation.setAgentStatus(MachineStatus.READY);

			for (SlotInformation slot : slots) {
				slotsInformation.add(slot);
			}

			int count = 0;
			for (String host : GodsProperties.getHosts()) {
				if (ControlCenter.getAgentInformation(host).getAgentStatus() == MachineStatus.READY) {
					count++;
				} else {
					break;
				}
			}
			if (count == GodsProperties.getNumberOfHosts()) {
				ControlCenter.getInstance().addSlotsInformation(
						slotsInformation);
				GodsProperties.setGodsStatus(GodsStatus.READY);
			}
		}
	}

	/**
	 * Returns the start and end indices of slots assigned to a host
	 * 
	 * @param hostName
	 *            of the machine for which slot range is required
	 * @return slotRange for the machine specified as parameter
	 */
	private int[] getSlotRangeFor(String hostName) {
		int[] slotsRange = new int[2];

		slotsRange[0] = ControlCenter.getAgentInformation(hostName)
				.getFirstSlotId();
		slotsRange[1] = ControlCenter.getAgentInformation(hostName)
				.getLastSlotId();

		return slotsRange;
	}
}
