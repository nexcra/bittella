/**
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.agent;

import gods.arch.Event;
import gods.arch.EventHandler;
import gods.arch.Module;
import gods.arch.Subscription;
import gods.arch.SubscriptionsRegistry;
import gods.cc.ControlCenterRemoteInterface;
import gods.topology.common.MachineInformation;
import gods.topology.common.SlotInformation;
import gods.topology.common.UnknownSlotException;

import java.lang.reflect.InvocationTargetException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.log4j.Logger;

/**
 * The <code>Agent</code> is the Singleton Agent class. All its modules
 * subscribe to and publish events through it.
 * 
 * @author Ozair Kafray
 * @version $Id: Agent.java 396 2007-08-01 14:45:00Z ozair $
 */
public class Agent implements AgentInterface {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger.getLogger(Agent.class);

	/**
	 * The structure that keeps the information about the machine on which this
	 * agent is running.
	 */
	private static MachineInformation machineInformation = null;

	/**
	 * A reference to this Agent's remote object
	 */
	private static AgentRemote agentRemote = null;

	/**
	 * Registered RMI name of the Agent
	 */
	private String agentName = null;

	/**
	 * A reference to the ControlCenter Remote Object
	 */
	private ControlCenterRemoteInterface ccRemote = null;

	/**
	 * Reference to the Agent's single instance
	 */
	private static Agent agentInstance = null;

	/**
	 * A Registry of Subscriptions to events
	 */
	private SubscriptionsRegistry subscriptionsRegistry = new SubscriptionsRegistry();

	/**
	 * The priority based queue of events
	 */
	private PriorityBlockingQueue<Event> eventsQueue = new PriorityBlockingQueue<Event>();

	private SlotInformation[] slotsInformation = null;

	/**
	 * The Agent is supposed to run until this value is set to false
	 */
	private boolean exit = false;

	/**
	 * The parameters required to construct an EventHandler. These are not
	 * required as class variable, but this would save time from constructing
	 * them each time an event is scheduled.
	 */
	private static Class[] handlerConstructorParameters = { Event.class,
			Module.class };

	/**
	 * Private Default Constructor to prevent creation of multiple instances for
	 * singleton Agent class
	 */
	private Agent() {
	}

	/**
	 * Returns the Single Agent instance, created when this method was called
	 * first time
	 * 
	 * @return The single Agent instance
	 */
	public static Agent getInstance() {
		if (agentInstance == null) {
			agentInstance = new Agent();
		}

		return agentInstance;
	}

	/**
	 * Initializes Agent Remote Object and looks for specified
	 * ControlCenterRemote
	 * 
	 * @param Name
	 *            of ControlCenter registered in RMI registry
	 * @return - true for successfull initialization
	 */
	public boolean initialize(String ccName) {

		try {
			if (System.getSecurityManager() == null) {
				System.setSecurityManager(new RMISecurityManager());
			}

			log.info("Checking for cc: " + ccName);

			ccRemote = (ControlCenterRemoteInterface) Naming
					.lookup(ccName); 
			
			log.info("... found");

			agentRemote = new AgentRemote();
			log.info("Created Agent Remote Object");

			agentRemote.bind();
			log.debug("Agent: " + agentName + " now linked to ControlCenter:"
					+ ccName);

		} catch (RemoteException re) {
			log.debug("RemoteException: " + re.getMessage());
			return false;

		} catch (java.net.MalformedURLException urle) {
			log.debug("MalformedURLException: " + urle.getMessage());
			return false;

		} catch (NotBoundException nbe) {
			log.fatal("Control Center:" + ccName + "not found.");
			log.debug("NotBoundException: " + nbe.getMessage());
			System.exit(1);
		}

		return true;
	}

	/**
	 * {@link gods.agent.AgentInterface#enqueueEvent(gods.arch.Event)}
	 */
	public void enqueueEvent(Event e) {
		eventsQueue.offer(e);
	}

	/**
	 * Notifies the ControlCenter of an event
	 * 
	 * @param event
	 *            of which the ControlCenter is to be notified
	 */
	public void notifyControlCenter(Event event) {

		try {
			ccRemote.notifyEvent(event);

		} catch (RemoteException re) {
			log.error("Remote Exception while notifying event to ControlCenter"
					+ re.getMessage());
		}
	}

	/**
	 * This method is the heartbeat of Agent, runs infinitely until exit is
	 * true. Takes events from events queue and notifies modules interetsed in
	 * this internal event.
	 */
	public void run() {
		while (!exit) {
			try {
				notify(eventsQueue.take());
			} catch (InterruptedException ie) {

				log.error("EXCEPTION: Agent interrupted" + ie.getMessage());
			}
		}
	}

	/**
	 * {@link gods.agent.AgentInterface#subscribe(java.util.List)}
	 */
	public void subscribe(List<Subscription> subscriptions) {
		for (Subscription subscription : subscriptions) {
			subscriptionsRegistry.addSubscription(subscription);
		}
	}

	/**
	 * Initializes the MachineInformation structure with the host name of the
	 * machine on which this agent is running
	 * 
	 * @param localHost
	 *            Host name of the machine on which this agent is running
	 */
	void initializeMachineInformation(String localHost) {
		// Initializing MachineInformation on which this agent is running
		machineInformation = new MachineInformation(localHost);
		agentName = getAgentNameOnHost(machineInformation.getHostName());
	}

	public void setSlotsInformation(int[] slotsRange,
			SlotInformation[] slotsInformation) {
		this.slotsInformation = slotsInformation;
		machineInformation.setFirstSlotId(slotsRange[0]);
		machineInformation.setLastSlotId(slotsRange[1]);
	}

	/**
	 * Returns the name of the agent running on a machine
	 * 
	 * @param hostName
	 *            name of the machine whose Agent's name is required
	 * @return Name of the agent running on the machine specified in parameter
	 */
	public static String getAgentNameOnHost(String hostName) {
		return "//" + hostName + "/gods-agent";
	}

	/**
	 * Returns the name of this Agent
	 * 
	 * @return The name of this Agent as registered in RMI registry
	 */
	public String getAgentName() {
		return agentName;
	}

	/**
	 * @return Information of the machine on which this Agent is running
	 */
	public static MachineInformation getMachineInformation() {
		return machineInformation;
	}

	/**
	 * @param slotId
	 *            the slot index as assigned during Gods initialization
	 * @return {@link gods.topology.common.SlotInformation}
	 * @throws UnknownSlotException
	 *             if the slot is out of range
	 */
	public SlotInformation getSlot(int slotId) throws UnknownSlotException {
		int index = slotId - machineInformation.getFirstSlotId();

		if ((index < 0) || (index >= slotsInformation.length)) {
			throw new UnknownSlotException("The slot " + slotId
					+ "is not known to GODS. Known slots range is:["
					+ machineInformation.getFirstSlotId() + "-"
					+ machineInformation.getLastSlotId() + "]");
		}

		return slotsInformation[index];
	}

	/**
	 * @param slotIds
	 * @return {@link gods.topology.common.SlotInformation}[] An array of slot
	 *         information objects corresponding to the array of slotIds given
	 *         as parameter
	 * @throws UnknownSlotException
	 *             if the slot is out of range
	 */
	public SlotInformation[] getSlots(int[] slotIds)
			throws UnknownSlotException {
		SlotInformation[] slots = null;

		int count = 0;
		if (slotIds != null) {
			slots = new SlotInformation[slotIds.length];
			for (int slotId : slotIds) {
				slots[count++] = getSlot(slotId);
			}
		}

		return slots;
	}

	/**
	 * Notifies of an event to the subscribing Module by enqueuing the related
	 * EventHandler.
	 * 
	 * @param event
	 *            of which a module is to be notified.
	 */
	private void notify(Event event) {
		List<Subscription> subscriptions = subscriptionsRegistry
				.getSubscriptions(event.getClass());

		if (subscriptions == null) {
			log.warn("Notifications for " + event.getClass() + "not found...");
		} else {
			try {
				for (Subscription subscription : subscriptions) {
					EventHandler h = (EventHandler) subscription
							.getEventHandlerClass().getConstructor(
									handlerConstructorParameters).newInstance(
									event, subscription.getModule());

					log.debug(h.getClass().getSimpleName());

					subscription.getModule().enqueue(h);
				}

			} catch (IllegalAccessException iae) {
				log.error("EXCEPTION: IllegalAccess" + iae.getMessage());

			} catch (InstantiationException ie) {
				log.error("EXCEPTION: Instantiation" + ie.getMessage());

			} catch (NoSuchMethodException nsme) {
				log.error("EXCEPTION: NoSuchMethod" + nsme.getMessage());

			} catch (InvocationTargetException ite) {
				log.error("EXCEPTION: InvocationTarget" + ite.getMessage());

			} catch (ClassCastException cce) {
				log.error("EXCEPTION: ClassCast" + cce.getMessage());

			}
		}
	}

}
