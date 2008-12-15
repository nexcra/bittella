/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.arch.remote;

import static gods.Gods.bindRemoteObject;
import static gods.arch.EventHandler.handlerConstructorParameters;

import gods.GodsExitCodes;
import gods.arch.Event;
import gods.arch.EventHandler;
import gods.arch.Module;
import gods.arch.Subscription;
import gods.arch.SubscriptionsRegistry;
import gods.cc.ControlCenterRemoteInterface;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.log4j.Logger;

/**
 * The <code>AbstractRemoteModule</code> class represents a RemoteModule of
 * ControlCenter or an Agent. A class extending it only has to override the
 * initialize method for subscribing to ControlCenter events and write
 * corresponding event handlers
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public abstract class AbstractRemoteModule extends UnicastRemoteObject
		implements RemoteModule, Module, Runnable {

	/**
	 * An instance of logger for this class
	 */
	private static Logger log = Logger.getLogger(AbstractRemoteModule.class);

	/**
	 * A reference to the proxy of this RemoteModule in ControlCenter
	 */
	private RemoteModuleProxy proxy;

	/**
	 * Reference to the ControlCenter Remote object
	 */
	protected ControlCenterRemoteInterface ccRemote;

	/**
	 * Name with which this {@link gods.arch.remote.RemoteModule} is bound to
	 * RMI registry
	 */
	private String rmiName = "";

	/**
	 * A Registry of Subscriptions to events
	 */
	private SubscriptionsRegistry subscriptionsRegistry = new SubscriptionsRegistry();

	/**
	 * The priority based queue of events
	 */
	private PriorityBlockingQueue<Event> eventsQueue = new PriorityBlockingQueue<Event>();

	/**
	 * The RemoteModule is supposed to run until this value is set to true
	 */
	private boolean exit = false;

	/**
	 * @throws RemoteException
	 */
	public AbstractRemoteModule(String rmiName,
			ControlCenterRemoteInterface ccRemote) throws RemoteException {
		super();
		this.rmiName = rmiName;
		this.ccRemote = ccRemote;
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

				log.error("EXCEPTION: RemoteModule Interrupted"
						+ ie.getMessage());
			}
		}
	}

	/**
	 * {@link gods.arch.remote.RemoteModule#notifyEvent(gods.arch.Event)}
	 */
	public void notifyEvent(Event e) {
		eventsQueue.offer(e);
		log.debug("Received Event " + e.getClass().getSimpleName());
	}

	/**
	 * An abstract method which must be overridden by extending classes. The
	 * overriding method must bind to RMI registry in this method and subscribe
	 * to events, besides initializations specific to a remote module. Extending
	 * classes can call the {@link gods.arch.remote.AbstractRemoteModule#bind()}
	 * method already implemented in this class to bind to the RMI registry
	 * which binds the remote module with the name provided in constructor.
	 * 
	 */
	public abstract void initialize() throws RemoteException,
			MalformedURLException;

	/**
	 * Binds the remote module with its rmi name and remote reference in the RMI
	 * registry
	 */
	protected void bind() {

		try {
			// Binding Remote Object with its name
			log.debug("=====================================");
			log.debug("Binding Remote Module with name: " + rmiName);
			bindRemoteObject(rmiName, this);
			log.info("Remote Module bound with name:" + rmiName);
			log.debug("=====================================");
			
		} catch (MalformedURLException mfue) {
			log.debug(mfue.getMessage());
			System.exit(GodsExitCodes.REMOTE_OBJECT_REGISTRATION_ERROR);
		}
	}

	/**
	 * Starts the RemoteModule processing
	 */
	public void start() throws RemoteException, MalformedURLException {
		initialize();
		run();
	}

	/**
	 * @param e
	 */
	private void notify(Event event) {
		List<Subscription> subscriptions = subscriptionsRegistry
				.getSubscriptions(event.getClass());

		if (subscriptions == null) {
			log.warn(event.getClass() + " is not subscribed by any module...");
		} else {
			try {
				for (Subscription subscription : subscriptions) {
					EventHandler h = (EventHandler) subscription
							.getEventHandlerClass().getConstructor(
									handlerConstructorParameters).newInstance(
									event, subscription.getModule());

					log.debug(h.getClass().getSimpleName()
							+ subscription.getModule().getClass()
									.getSimpleName());

					h.handle();
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
	 * The name of this function is just to fulfill the contract of
	 * implementation with Module interface, however it executes the handler in
	 * this function.
	 * 
	 * @see gods.arch.Module#enqueue(gods.arch.EventHandler)
	 */
	public void enqueue(EventHandler h) {
		h.handle();
	}

	/**
	 * {@link gods.arch.remote.RemoteModule#subscribe(java.util.List)}
	 */
	public void subscribe(List<Subscription> subscriptions)
			throws RemoteException {

		List<Class> events = new LinkedList<Class>();

		for (Subscription subscription : subscriptions) {

			subscriptionsRegistry.addSubscription(subscription);

			events.add(subscription.getEventClass());
		}

		proxy.subscribe(events);
	}

	/**
	 * {@link gods.arch.remote.RemoteModule#getRemoteModuleProxy()}
	 */
	public RemoteModuleProxy getRemoteModuleProxy() {
		return proxy;
	}

	/**
	 * {@link gods.arch.remote.RemoteModule#setRemoteModuleProxy(gods.arch.remote.RemoteModuleProxy)}
	 */
	public void setRemoteModuleProxy(RemoteModuleProxy proxy)
			throws RemoteException {
		this.proxy = proxy;
		proxy.setRemoteModule(this);
	}

	/**
	 * @return the exit value
	 */
	public synchronized boolean isExit() {
		return exit;
	}

	/**
	 * @param exit -
	 *            the exit value to set
	 */
	public synchronized void setExit(boolean exit) {
		this.exit = exit;
	}

	/**
	 * @return the rmiName
	 */
	public String getRmiName() {
		return rmiName;
	}

	/**
	 * @return the ccRemote
	 */
	public ControlCenterRemoteInterface getCcRemote() {
		return ccRemote;
	}
}
