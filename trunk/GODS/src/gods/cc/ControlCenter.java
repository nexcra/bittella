/**
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.cc;

import static gods.agent.Agent.getAgentNameOnHost;
import static gods.arch.EventHandler.handlerConstructorParameters;

import gods.GodsProperties;
import gods.arch.Event;
import gods.arch.EventHandler;
import gods.arch.Subscription;
import gods.arch.SubscriptionsRegistry;
import gods.arch.Task;
import gods.topology.cc.AgentInformation;
import gods.topology.common.MachineStatus;
import gods.topology.common.SlotInformation;
import gods.topology.common.UnknownSlotException;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.log4j.Logger;

/**
 * The <code>ControlCenter</code> is the Singleton ControlCenter class. All
 * its modules subscribe to and publish events through it.
 * 
 * @author Ozair Kafray
 * @version $Id: ControlCenter.java 407 2007-08-14 08:41:13Z ozair $
 */
public class ControlCenter implements ControlCenterInterface {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger.getLogger(ControlCenter.class);

	/**
	 * A reference to the ControlCenter Remote Object
	 */
	private static ControlCenterRemote ccRemote = null;

	/**
	 * Reference to the Control Center's single instance
	 */
	private static ControlCenter controlCenterInstance = null;

	/**
	 * A Registry of Subscriptions to events
	 */
	private SubscriptionsRegistry subscriptionsRegistry = new SubscriptionsRegistry();

	/**
	 * The priority based queue of events
	 */
	private PriorityBlockingQueue<Event> eventsQueue = new PriorityBlockingQueue<Event>();

	/**
	 * Map of Gods Agents associated with their host names. Agents in the
	 * ControlCenter are thus identified by the machines they are running on
	 */
	private static Map<String, AgentInformation> agentsInformation = new HashMap<String, AgentInformation>();

	/**
	 * A List of the slots available on all the host machines.
	 */
	private static Vector<SlotInformation> slotsInformation = new Vector<SlotInformation>();

	/**
	 * The ControlCenter is supposed to run until this value is set to true
	 */
	private boolean exit = false;

	

	/**
	 * Private Default Constructor to prevent creation of multiple instances for
	 * singleton ControlCenter class
	 */
	private ControlCenter() {
	}

	/**
	 * Returns the Single ControlCenter instance, createed when this method was
	 * called first time.
	 * 
	 * @return The single ControlCenter instance
	 */
	public static ControlCenter getInstance() {
		if (controlCenterInstance == null) {
			controlCenterInstance = new ControlCenter();
			log
					.debug("Single ControlCenterReference: "
							+ controlCenterInstance);
		}

		return controlCenterInstance;
	}

	/**
	 * Initializes Control Center Remote Object and initializes information for
	 * agents
	 * 
	 * @return - true for successfull initialization
	 */
	public boolean initialize() {
		log.info("Initializing Control Center...");
		String localHost = GodsProperties.getLocalHost();
		log.debug(localHost);

		try {

			if (System.getSecurityManager() == null) {
				log.info("Security Manager not found...");
				System.setSecurityManager(new RMISecurityManager());
				log.info("Created Security Manager...");
			}

			ccRemote = new ControlCenterRemote();
			log.info("Created ControlCenter Remote Object...");
			ccRemote.bind("//" + localHost + "/gods-cc");

			initializeAgentInformation();

		} catch (RemoteException re) {
			log.error(re.getMessage());
			return false;

		} catch (NullPointerException npe) {
			log.error(npe.getMessage());
			return false;
		} 

		return true;
	}

	/**
	 * 
	 * {@link gods.cc.ControlCenterInterface#enqueueEvent(gods.arch.Event)}
	 */
	public void enqueueEvent(Event e) {
		eventsQueue.offer(e);
	}

	/**
	 * Notifies about an Event to the agent running on the specified host
	 * machine
	 * 
	 * @param hostName
	 *            Hostname of the machine on which the agent is to be notified
	 * @param e
	 *            The Event of which the agent is to be notified
	 */
	public void notifyAgent(String hostName, Event e) {

		try {
			agentsInformation.get(hostName).getAgent().notifyEvent(e);

		} catch (RemoteException re) {

			log.error("Remote Exception while notifying event to Agent on "
					+ hostName + " " + re.getMessage());
		}
	}

	/**
	 * Notifies about an Event to all agents, with as minimum delay as possible
	 * in between sending of the event to different agents.
	 * 
	 * @param e
	 *            The Event of which the agents are to be notified
	 */
	public void notifyAllAgents(Event e) {

		try {
			Vector<String> hosts = GodsProperties.getHosts();
			NotifyAgentCallable[] calls = new NotifyAgentCallable[hosts.size()];

			int i = 0;
			for (String host : hosts) {
				calls[i] = new NotifyAgentCallable();
				calls[i].setAgent(agentsInformation.get(host).getAgent());
				calls[i].setEvent(e);
				++i;
			}

			ExecutorService executor = Executors.newFixedThreadPool(hosts
					.size());
			// Initialize Threads

			Collection<Callable<Void>> inits = new Vector<Callable<Void>>();
			for (int j = 0; j < hosts.size(); j++) {
				inits.add(new InitializingCallable());
			}
			executor.invokeAll(inits);

			for (NotifyAgentCallable call : calls) {
				executor.submit(call);
			}
		} catch (InterruptedException ie) {
			System.out.println(ie.getMessage());
		}

		/*
		 * Vector<String> hosts = GodsProperties.getHosts();
		 * NotifyAgentCallable[] calls = new NotifyAgentCallable[hosts .size()];
		 * 
		 * int i = 0; for (String host : hosts) { calls[i] = new
		 * NotifyAgentCallable();
		 * calls[i].setAgent(agentsInformation.get(host).getAgent());
		 * calls[i].setEvent(e); ++i; }
		 * 
		 * ExecutorService executor =
		 * Executors.newFixedThreadPool(hosts.size()); for (NotifyAgentCallable
		 * call : calls) { executor.submit(call); }
		 */

		/*
		 * try { Vector<String> hosts = GodsProperties.getHosts(); Collection<Callable<Void>>
		 * calls = new Vector<Callable<Void>>();
		 * 
		 * NotifyAgentCallable call = null; int i = 0; for (String host : hosts) {
		 * call = new NotifyAgentCallable(); call = new NotifyAgentCallable();
		 * call.setAgent(agentsInformation.get(host).getAgent());
		 * call.setEvent(e); calls.add(call); }
		 * 
		 * ExecutorService executor = Executors.newFixedThreadPool(hosts
		 * .size()); executor.invokeAll(calls); } catch (InterruptedException
		 * ie) { System.out.println(ie.getMessage()); }
		 */

		/*
		 * Vector<String> hosts = GodsProperties.getHosts();
		 * NotifyAgentRunnable calls[] = new NotifyAgentRunnable[hosts .size()];
		 * 
		 * int i = 0; for (String host : hosts) { calls[i] = new
		 * NotifyAgentRunnable();
		 * calls[i].setAgent(agentsInformation.get(host).getAgent());
		 * calls[i].setEvent(e); ++i; }
		 * 
		 * Executor executor = Executors.newFixedThreadPool(hosts.size()); for
		 * (NotifyAgentRunnable call : calls) { executor.execute(call); }
		 */

		/*
		 * try { Vector<String> hosts = GodsProperties.getHosts();
		 * AgentRemoteInterface agents[] = new AgentRemoteInterface[hosts
		 * .size()];
		 * 
		 * int i = 0; for (String host : hosts) { agents[i] =
		 * agentsInformation.get(host).getAgent(); ++i; }
		 * 
		 * for (AgentRemoteInterface agent : agents) { agent.notifyEvent(e); } }
		 * catch (RemoteException re) { log.error("Remote Exception while
		 * notifying event to Agent " + " " + re.getMessage()); }
		 */

		/*
		 * try { Vector<String> hosts = GodsProperties.getHosts(); for (String
		 * host : hosts) {
		 * agentsInformation.get(host).getAgent().notifyEvent(e); } } catch
		 * (RemoteException re) { log.error("Remote Exception while notifying
		 * event to Agent " + " " + re.getMessage()); }
		 */
	}

	/**
	 * @param task -
	 *            Task to be executed on each Agent
	 * 
	 */
	public void executeTaskOnAll(/* Task task */) {
		try {
			/*
			 * Vector<String> hosts = GodsProperties.getHosts(); Collection<Callable<Void>>
			 * calls = new Vector<Callable<Void>>(); Task syncClocksTask = new
			 * SyncClocksTask();
			 * 
			 * ExecuteTaskCallable call = null; int i = 0; for (String host :
			 * hosts) { call = new ExecuteTaskCallable();
			 * call.setAgent(agentsInformation.get(host).getAgent());
			 * call.setTask(syncClocksTask); calls.add(call); }
			 * 
			 * ExecutorService executor = Executors.newFixedThreadPool(hosts
			 * .size()); // Initialize Threads Collection<Callable<Void>>
			 * inits = new Vector<Callable<Void>>(); for (int j = 0; j <
			 * hosts.size(); j++) { inits.add(new InitializingCallable()); }
			 * 
			 * executor.invokeAll(inits);
			 * 
			 * executor.invokeAll(calls);
			 */
			/*
			 * Vector<String> hosts = GodsProperties.getHosts();
			 * ExecuteTaskRunnable[] calls = new
			 * ExecuteTaskRunnable[hosts.size()]; Task syncClocksTask = new
			 * SyncClocksTask();
			 * 
			 * int i = 0; for (String host : hosts) { calls[i] = new
			 * ExecuteTaskRunnable();
			 * calls[i].setAgent(agentsInformation.get(host).getAgent());
			 * calls[i].setTask(syncClocksTask); ++i; }
			 * 
			 * ExecutorService executor = Executors.newFixedThreadPool(hosts
			 * .size()); // Initialize Threads Collection<Callable<Void>>
			 * inits = new Vector<Callable<Void>>(); for (int j = 0; j <
			 * hosts.size(); j++) { inits.add(new InitializingCallable()); }
			 * executor.invokeAll(inits);
			 * 
			 * for (ExecuteTaskRunnable call : calls) { executor.submit(call); }
			 */

			Vector<String> hosts = GodsProperties.getHosts();
			ExecuteTaskCallable[] calls = new ExecuteTaskCallable[hosts.size()];
			Future[] results = new Future[calls.length];
			Task syncClocksTask = new SyncClocksTask();

			int i = 0;
			for (String host : hosts) {
				calls[i] = new ExecuteTaskCallable();
				calls[i].setAgent(agentsInformation.get(host).getAgent());
				calls[i].setTask(syncClocksTask);
				++i;
			}

			ExecutorService executor = Executors.newFixedThreadPool(hosts
					.size());

			// Initialize Threads
			/*
			 * Collection<Callable<Void>> inits = new Vector<Callable<Void>>();
			 * for (int j = 0; j < hosts.size(); j++) { inits.add(new
			 * InitializingCallable()); } executor.invokeAll(inits);
			 */
			int count = 0;
			for (ExecuteTaskCallable call : calls) {
				results[count++] = executor.submit(call);
			}
			
			//Wait for all threads to return
			for(Future result : results ){
				result.get();
			}
			
		} catch (/* Interrupted */Exception ie) {
			System.out.println(ie.getMessage());
		}
	}

	/**
	 * This method is the heartbeat of ControlCenter, runs infinitely until exit
	 * is true. Takes events from events queue and notifies modules interested
	 * in this event.
	 */
	public void run() {
		while (!exit) {
			try {
				notify(eventsQueue.take());
			} catch (InterruptedException ie) {

				log.error("EXCEPTION: ControlCenter Interrupted"
						+ ie.getMessage());
			}
		}
	}

	/**
	 * {@link gods.cc.ControlCenterInterface#subscribe(java.util.List)}
	 */
	public void subscribe(List<Subscription> subscriptions) {
		for (Subscription subscription : subscriptions) {
			subscriptionsRegistry.addSubscription(subscription);
		}
	}

	public void addSlotsInformation(Collection<SlotInformation> slotInformation) {

		ControlCenter.slotsInformation
				.ensureCapacity(ControlCenter.slotsInformation.size()
						+ slotInformation.size());

		ControlCenter.slotsInformation.addAll(slotInformation);

		log.info("Following Slots have been added...");
		for (SlotInformation slot : slotInformation) {
			log.info(slot.toString());
		}
	}

	/**
	 * @param hostName
	 *            the host name of the mahcine on which the agent is running
	 * @return {@link gods.topology.cc.AgentInformation} information required
	 *         about the agent
	 */
	public static AgentInformation getAgentInformation(String hostName) {
		return agentsInformation.get(hostName);
	}

	/**
	 * @param slotId
	 *            the slot index as assigned during Gods initialization
	 * @return {@link gods.topology.common.SlotInformation}
	 * @throws UnknownSlotException
	 *             if the slot is out of range
	 */
	public SlotInformation getSlot(int slotId) throws UnknownSlotException {
		if ((slotId < 1) || (slotId > slotsInformation.size())) {
			throw new UnknownSlotException("The slot " + slotId
					+ "is not known to GODS. Known slots range is:[1-"
					+ slotsInformation.size() + "]");
		}

		return slotsInformation.get(slotId - 1);
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
	 * @param slotIds
	 * @return {@link gods.topology.common.SlotInformation}[] An array of slot
	 *         information objects corresponding to the array of slotIds given
	 *         as parameter
	 * @throws UnknownSlotException
	 *             if the slot is out of range
	 */
	public SlotInformation[] getAllSlots() {

		SlotInformation[] slots = new SlotInformation[slotsInformation.size()];
		slotsInformation.toArray(slots);

		return slots;
	}

	/**
	 * Notifies of an event to the subscribing Module by enqueuing the related
	 * EventHandler
	 * 
	 * @param event
	 *            of which a module is to be notified.
	 */
	private void notify(Event event) {
		List<Subscription> subscriptions = subscriptionsRegistry
				.getSubscriptions(event.getClass());

		/*
		 * An event handler is being created each time an event of certain type
		 * is received and there is a subscription corresponding to it. The
		 * construction of EventHandlers should be delegated to an
		 * EventHandlerFactory which should take care that if an EventHandler
		 * was created earlier the same should be returned albeit encapsulating
		 * the new Event.
		 * 
		 * But the above procedure has to be well thought, since if an
		 * EventHandler is reused while it is in the queue of a module, it might
		 * create chaos.
		 */
		if (subscriptions == null) {
			log.warn("Notifications for " + event.getClass() + " not found...");
		} else {
			try {
				for (Subscription subscription : subscriptions) {
					EventHandler h = (EventHandler) subscription
							.getEventHandlerClass().getConstructor(
									handlerConstructorParameters).newInstance(
									event, subscription.getModule());

					log.debug("Handler: " + h.getClass().getSimpleName()
							+ " being enqueued in Module: " + subscription.getModule().getClass()
									.getSimpleName());

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
				log.debug("EXCEPTION: ClassCast" + cce.getMessage());

			}

		}
	}

	/**
	 * Populates the Host to AgentInformation map. Calculates slot ranges to be
	 * assigned to each host
	 */
	private void initializeAgentInformation() {

		log.info("Initializing Agent Information...");

		Vector<String> hosts = GodsProperties.getHosts();
		int slots = GodsProperties.getNumberOfSlots();

		int machines = hosts.size();
		
		int[] slotsRange = getSlotsRange(machines,slots);

		int count = 0;
		for (String host : hosts) {
			AgentInformation agentInformation = new AgentInformation(host,
					getAgentNameOnHost(host));

			agentInformation.setAgentStatus(MachineStatus.INITIALIZING);

			agentInformation.setFirstSlotId(slotsRange[count]);
			agentInformation.setLastSlotId(slotsRange[count + 1] - 1);

			agentsInformation.put(host, agentInformation);

			log.info("Slots For");
			log.info(host + ": [" + agentInformation.getFirstSlotId() + " - "
					+ agentInformation.getLastSlotId() + "]");

			count++;
		}
	}
	
	public static int[] getSlotsRange(int machines, int slots){
		
		int[] slotsForEachMachine = new int[machines];
		int[] slotsRange = new int[machines + 1];

		
		log.debug("Number of Machines : " + machines);

		int slotsPerMachine = slots / machines;
		log.debug("Slots Per Machines : " + slotsPerMachine);
		for (int i = 0; i < machines; i++) {
			slotsForEachMachine[i] = slotsPerMachine;
		}

		// Now assign remaining nodes
		int unassignedSlots = 0;
		if (slots > (slotsPerMachine * machines)) {
			unassignedSlots = slots - (slotsPerMachine * machines);
			log.debug("Unassigned Slots : " + unassignedSlots);
		}

		for (int j = 0; j < unassignedSlots; j++) {
			slotsForEachMachine[j]++;
		}

		slotsRange[0] = 1;
		for (int k = 1; k < machines + 1; k++) {
			slotsRange[k] = slotsRange[k - 1] + slotsForEachMachine[k - 1];
		}
		
		return slotsRange;
	}
}
