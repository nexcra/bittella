/**
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.arch;

import java.util.concurrent.PriorityBlockingQueue;

import org.apache.log4j.Logger;

/**
 * The <code>AbstractModule</code> class is an abstract implementation of the
 * {@link gods.arch.Module} interface. It also extends the Thread class and
 * overrides the {@link java.lang.Thread#run} method to take event handlers from
 * queue and execute them.
 * 
 * An AbstractModule subscribes to Events in its initialize method, which is
 * called before the thread starts.
 * 
 * As EventHandlers are enqueued in a priority based queue the Events are hence
 * handled by modules according to their priority.
 * 
 * @author Ozair Kafray
 * @version $Id: AbstractModule.java 378 2007-07-20 11:34:04Z ozair $
 */
public abstract class AbstractModule extends Thread implements Module {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger.getLogger(AbstractModule.class);

	/**
	 * The priority based queue of event handlers
	 */
	private PriorityBlockingQueue<EventHandler> handlersQueue = new PriorityBlockingQueue<EventHandler>();

	/**
	 * The module is supposed to run until this value is set to false
	 */
	private boolean exit = false;

	/**
	 * This function is called for each child class implicitly in
	 * AbstractModule.start(). All initialization code including subscription
	 * for events by a module should be written in this function.
	 */
	protected abstract void initialize();

	public AbstractModule(String moduleName) {
		super(moduleName);
	}

	/**
	 * {@link java.lang.Thread#run()}
	 */

	@Override
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

	public void enqueue(EventHandler h) {
		handlersQueue.offer(h);
	}

	/**
	 * @param exit -
	 *            the exit value to set
	 */
	public synchronized void setExit(boolean exit) {
		this.exit = exit;
	}

	/**
	 * The method Thread.start has been overriden to call
	 * AbstractModule.initialize before AbstractModule.run
	 * 
	 * {@link java.lang.Thread#start()}
	 */
	@Override
	public synchronized void start() {
		this.initialize();
		super.start();
	}

	/**
	 * @return the exit value
	 */
	public synchronized boolean isExit() {
		return exit;
	}

	/**
	 * This function will be called in each scheduling of the thread for a
	 * module. Tasks that are to be executed by the module continuously should
	 * be in this function. This would not work at all currently, as it is under
	 * thought as this would violate the motive of current Gods architecture. To
	 * acheive the same effect the module should push event handler in its queue
	 * by itself e.g., a TickEventHandler
	 */
	// public abstract void tick();
}
