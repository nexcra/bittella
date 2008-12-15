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

import gods.GodsExitCodes;
import gods.arch.EventHandler;
import gods.arch.Module;
import gods.arch.Subscription;
import gods.cc.ControlCenter;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.log4j.Logger;

/**
 * The <code>AbstractRemoteModuleProxy</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public abstract class AbstractRemoteModuleProxy extends UnicastRemoteObject
		implements RemoteModuleProxy, Module, Runnable {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger
			.getLogger(AbstractRemoteModuleProxy.class);

	/**
	 * A reference to the remote module to which this proxy belongs
	 */
	private RemoteModule rModule;

	/**
	 * Name with which this {@link gods.arch.remote.RemoteModule} is bound to
	 * RMI registry
	 */
	private String rmiName = "";

	/**
	 * The priority based queue of event handlers
	 */
	private PriorityBlockingQueue<EventHandler> handlersQueue = new PriorityBlockingQueue<EventHandler>();

	/**
	 * The module is supposed to run until this value is set to false
	 */
	private boolean exit = false;

	/**
	 * @throws RemoteException
	 */
	public AbstractRemoteModuleProxy(String rmiName) throws RemoteException {
		super();
		this.rmiName = rmiName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while (!(isExit())) {

			try {
				log.debug("trying to take from queue...");
				EventHandler h = handlersQueue.take();
				log.debug("took from the queue. executing "
						+ h.getClass().getSimpleName());

				h.handle();

			} catch (InterruptedException ie) {
				log.fatal("EXCEPTION: Module Interrupted " + ie.getMessage());

			} catch (ClassCastException cce) {
				log.fatal("EXCEPTION: " + cce.getMessage());
				cce.printStackTrace();
			}
		}
	}

	/**
	 * Should call bind(), and call run in overidden method, if no separate
	 * thread is required, otherwise create a thread and call its start() method
	 */
	public abstract void start();

	/**
	 * {@link gods.arch.remote.RemoteModuleProxy#getRemoteModule()}
	 */
	public RemoteModule getRemoteModule() throws RemoteException {

		return rModule;
	}

	/**
	 * {@link gods.arch.remote.RemoteModuleProxy#setRemoteModule(gods.arch.remote.RemoteModule)}
	 */
	public void setRemoteModule(RemoteModule rModule) {
		this.rModule = rModule;
		log.debug("Remote Module has been set to: " + rModule);
	}

	/**
	 * {@link gods.arch.remote.RemoteModuleProxy#subscribe(java.util.List)}
	 */
	public void subscribe(List<Class> events) throws RemoteException {

		List<Subscription> subscriptions = new LinkedList<Subscription>();
		Subscription subscription = null;

		for (Class ec : events) {
			subscription = new Subscription(this, ec, RemoteEventHandler.class);

			subscriptions.add(subscription);
		}

		ControlCenter.getInstance().subscribe(subscriptions);
	}

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
	 * {@link gods.arch.Module#enqueue(gods.arch.EventHandler)}
	 */
	public void enqueue(EventHandler h) {

		handlersQueue.offer(h);
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
	 * @param rmiName
	 *            the rmiName to set
	 */
	public void setRmiName(String rmiName) {
		this.rmiName = rmiName;
	}

}
