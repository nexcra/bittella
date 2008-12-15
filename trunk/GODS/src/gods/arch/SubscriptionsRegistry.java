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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * The <code>SubscriptionsRegistry</code> class keeps a map of Events to all
 * its subscriptions. This class is not an Event Broker itself but is a utility
 * class for such broker classes.
 * 
 * @author Ozair Kafray
 * @version $Id: SubscriptionsRegistry.java 258 2006-11-28 13:05:40Z cosmin $
 */
public class SubscriptionsRegistry {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger.getLogger(SubscriptionsRegistry.class);

	/**
	 * Map of the events associated with their subscriptions
	 */
	private Map<Class, List<Subscription>> subscriptions = new HashMap<Class, List<Subscription>>();

	public SubscriptionsRegistry() {
	}

	/**
	 * Adds a subscription to the corresponding event type
	 */

	public void addSubscription(Subscription subscription) {
		if (subscriptions.containsKey(subscription.getEventClass())) {

			subscriptions.get(subscription.getEventClass()).add(subscription);

		} else {

			log.debug("Subscription for " + subscription.getEventClass()
					+ " not found...");

			List<Subscription> eventSubscriptions = new LinkedList<Subscription>();
			eventSubscriptions.add(subscription);
			subscriptions.put(subscription.getEventClass(), eventSubscriptions);
		}

		log.debug("Subscription for " + subscription.getEventClass()
				+ " mapped to " + subscription.getEventHandlerClass()
				+ " by Module " + subscription.getModule().getClass()
				+ " added");

	}

	/**
	 * @return List of the subscriptions associated with an event type in the
	 *         registry
	 */
	public List<Subscription> getSubscriptions(Class eventClass) {

		return subscriptions.get(eventClass);
	}
}
