/**
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods;

import gods.cc.ControlCenter;
import gods.churn.cc.ChurnModule;
import gods.deploy.cc.DeploymentModule;
import gods.deploy.events.BootEvent;
import gods.experiment.cc.ExperimentExecutorModule;
import gods.topology.cc.TopologyModule;
import gods.visualizer.VisualizerModuleProxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * The <code>Gods</code> class is responsible for boot strapping Gods. It has
 * the main function.
 * 
 * @author Ozair Kafray
 * @version $Id: Gods.java 431 2008-02-02 14:15:38Z ozair $
 */
public class Gods {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger.getLogger(Gods.class);

	private static final int registryPort = 1099;

	/**
	 * This function performs the bootstraps Gods 1. Initialize GodsProperties
	 * 2. Create, initialize and start Modules 3. Create BootEvent and enqueue
	 * it in ControlCenter
	 * 
	 * @param args
	 *            requires a gods startup file and optionally a true/false. In
	 *            case of true the agents are automatically started, in case of
	 *            false the Gods runs in debug mode. In the debug mode although
	 *            the agents are deployed automatically, but they need to be
	 *            started manually.
	 */
	public static void main(String[] args) {

		PropertyConfigurator.configure(System
				.getProperty("org.apache.log4j.config.file"));

		// Initialize GodsProperties
		if ((args.length == 0) || (args.length > 2)) {
			System.out.println("usage: gods.Gods <gods.config.xml>");
			System.exit(1);
		} else {
			log.info("INITIALIZING PROPERTIES...");
			if (!GodsProperties.initialize(args[0]))
				System.exit(1);
			log.info("\n----------------" + " GODS PROPERTIES INITIALIZED"
					+ "----------------");
		}

		addShutdownHook();

		// Initializing Modules
		/*
		 * For now these modules are just being created and initialized/started
		 * Later even the list of modules can be specified in a Gods config file
		 */
		new DeploymentModule().start();
		new ChurnModule().start();
		new TopologyModule().start();
		new ExperimentExecutorModule().start();

		// Initializing Remote Modules
		try {

			new VisualizerModuleProxy().start();

		} catch (UnknownHostException uhe) {
			log.error(uhe.getMessage());

		} catch (RemoteException re) {
			log.error(re.getMessage());

		}

		log.info("\n----------------"
				+ " SUBSCRIPTIONS COMPLETE & MODULES STARTED"
				+ "----------------");

		// Creating BootEvent
		/*
		 * Creation of BootEvent is the first event needed to be raised and then
		 * the whole system starts to work handling a seires of events
		 */
		BootEvent boot = new BootEvent(1);

		// Parameter specifying whether deployment module should autostart
		// agents or not
		if (args.length == 1) {
			boot.setAutoStartAgents(true);
		} else if (args.length == 2) {
			boot.setAutoStartAgents(Boolean.parseBoolean(args[1]));
		}

		ControlCenter.getInstance().enqueueEvent(boot);

		log.info("\n----------------" + " BOOT EVENT CREATED AND ENQUEUED"
				+ "----------------");

		ControlCenter.getInstance().run();

	}

	private static void addShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					Naming.unbind("//"
							+ InetAddress.getLocalHost().getHostName()
							+ "/gods-cc");
					Naming.unbind("//"
							+ InetAddress.getLocalHost().getHostName()
							+ "/gods-visualizer-proxy");

				} catch (RemoteException re) {
					log.debug(re.getMessage());

				} catch (MalformedURLException mfue) {
					log.debug(mfue.getMessage());

				} catch (NotBoundException nbe) {
					log.debug(nbe.getMessage());

				} catch (UnknownHostException uhe) {
					log.debug(uhe.getMessage());

				}
			}
		});
	}

	public static String streamToString(InputStream outputStream)
			throws IOException {

		InputStreamReader outputReader = new InputStreamReader(outputStream);

		final int maxRead = 20;
		char[] cbuf = new char[maxRead];
		int noRead = outputReader.read(cbuf, 0, maxRead);
		StringBuffer stringBuffer = new StringBuffer();

		while (noRead != -1) {
			stringBuffer.append(cbuf, 0, noRead);
			noRead = outputReader.read(cbuf, 0, maxRead);
		}

		outputReader.close();

		return stringBuffer.toString();

	}

	private static void startRmiRegistry() {
		try {

			log.debug("Strating rmiregistry on "
					+ InetAddress.getLocalHost().getHostName() + " on port "
					+ registryPort);
			LocateRegistry.createRegistry(registryPort);

		} catch (UnknownHostException uhe) {
			log.debug(uhe.getMessage());
		} catch (RemoteException re) {
			log.debug("Could not create rmiregistry" + re.getMessage());
		}
	}

	public static void bindRemoteObject(String objectName,
			Remote objectReference) throws MalformedURLException{

		boolean retry = true;
		int count = 0;
		while (retry) {
			try {
				Naming.rebind(objectName, objectReference);
				retry = false;
			} catch (RemoteException re) {
				++count;
				log.debug("Could not connect to rmiregistry in attempt "
						+ count + " " + re.getMessage()
						+ " Attempting to start...");
				startRmiRegistry();
				if (count == 3) {
					log.debug("Terminating execution...");
					System.exit(GodsExitCodes.REMOTE_OBJECT_REGISTRATION_ERROR);
				}
			}
		}
	}
	
}
