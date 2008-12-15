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

import gods.arch.remote.AbstractRemoteModuleProxy;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;

/**
 * The <code>VisualizerModuleProxy</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class VisualizerModuleProxy extends AbstractRemoteModuleProxy {

	/**
	 * An instance of Logger for this class
	 */
	private static Logger log = Logger.getLogger(VisualizerModuleProxy.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = -2056035516075953668L;

	/**
	 * @throws RemoteException
	 */
	public VisualizerModuleProxy() throws RemoteException, UnknownHostException {
		super("//" + InetAddress.getLocalHost().getHostName()
				+ "/gods-visualizer-proxy");
	}

	/**
	 * {@link gods.arch.remote.AbstractRemoteModuleProxy#start()}
	 */
	public void start() {

		bind();
		Thread visualizer = new Thread(this);
		visualizer.setName("VisualizerModuleProxy");
		visualizer.start();

	}

}
