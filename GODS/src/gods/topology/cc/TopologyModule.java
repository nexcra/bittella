/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.topology.cc;

import gods.arch.AbstractModule;
import gods.arch.Subscription;
import gods.cc.ControlCenter;
import gods.topology.events.SlotsInformationRequest;

import java.util.LinkedList;
import java.util.List;

/**
 * The <code>TopologyModule</code> class
 *
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class TopologyModule extends AbstractModule {

	/**
	 * @param moduleName
	 */
	public TopologyModule() {
		super(TopologyModule.class.getSimpleName());
	}

	/**
	 * {@link gods.arch.AbstractModule#initialize()}
	 */
	@Override
	public void initialize() {
		
		subscribe();
	}

	private void subscribe(){
		List<Subscription> subscriptions = new LinkedList<Subscription>();

		// Fill in subscriptions including reference to this module, events and
		// corresponding EventHandlers

		// Add LaunchApplicationEvent to subscription list
		Subscription subscription = new Subscription(this, SlotsInformationRequest.class,
				SlotsInformationRequestHandler.class);
		subscriptions.add(subscription);
		
		// Subscribing Events
		ControlCenter.getInstance().subscribe(subscriptions);
	}
}
