/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.visualizer;

import gods.arch.Subscription;
import gods.arch.remote.AbstractRemoteModule;
import gods.cc.ControlCenterRemoteInterface;
import gods.topology.events.SlotInformationChanged;
import gods.topology.events.SlotsInformationRequest;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * The <code>VisualizerRemote</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class VisualizerRemoteModule extends AbstractRemoteModule {

	/**
	 * 
	 */
	private static final long serialVersionUID = -378682231089089268L;

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger.getLogger(VisualizerRemoteModule.class);
	
	private boolean joinedLate = false;

	/**
	 * @param rminame
	 *            name with which this remote module is bound with rmiregistry
	 * @throws RemoteException
	 */
	public VisualizerRemoteModule(ControlCenterRemoteInterface ccRemote)
			throws RemoteException, UnknownHostException {
		super("//" + InetAddress.getLocalHost().getHostName()
				+ "/gods-visualizer", ccRemote);
	}

	/**
	 * Initializes the module
	 * {@link gods.arch.remote.AbstractRemoteModule#run()}
	 */
	@Override
	public void start() throws RemoteException,
			MalformedURLException {
	
		initialize();

		//if (joinedLate) {
			SlotsInformationRequest slotsInfoRequest = new SlotsInformationRequest(
					1);
			ccRemote.notifyEvent(slotsInfoRequest);
		//}

		run();
	}

	/**
	 * Binds the remote module to rmi registry, and subscribes to events of
	 * interest {@link gods.arch.AbstractRemoteModule#initialize()}
	 */
	public void initialize() throws RemoteException, MalformedURLException {

		// Bind the remote module to rmi registry
		bind();

		// Subscribe to events
		List<Subscription> subscriptions = new ArrayList<Subscription>();

		subscriptions.add(new Subscription(this, SlotInformationChanged.class,
				SlotInfoChangedHandler.class));

		subscribe(subscriptions);
	}

	/**
	 * @return the joinedLate
	 */
	public boolean didJoinLate() {
		return joinedLate;
	}

	/**
	 * @param joinedLate the joinedLate to set
	 */
	public void setJoinedLate(boolean joinedLate) {
		this.joinedLate = joinedLate;
	}
}
