/**
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
// file: Visualizer ---
// cre: 2006-09/FH  
// rev: 2007-02-02
//----------------------
package gods.visualizer;

import gods.GodsProperties;
import gods.arch.remote.RemoteModuleProxy;
import gods.cc.ControlCenterRemoteInterface;
import gods.topology.events.SlotInformationChanged;
import gods.visualizer.graphics.MainDisplay;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * The <code>Visualizer</code> class
 * 
 * @author Fredrik Holmgren and Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 * 
 */
public class Visualizer {

	/**
	 * An instance of Logger for this class
	 */
	private static Logger log = Logger.getLogger(Visualizer.class);

	/**
	 * Reference to the main display window of visualizer
	 */
	private MainDisplay main_display;

	/**
	 * Reference to the single instance of the visualizer
	 */
	private static Visualizer visualizerInstance = null;

	/**
	 * 
	 */
	private static String ccLaunchScript = "/home/ozair/workspace/gods-www/scripts/run-gods.sh";

	/**
	 * 
	 */
	private static String ccSetupScript = "/home/ozair/workspace/gods-www/scripts/gods-setup-snusmumrik.sh";;
	
	/**
	 * Reference to the remote Visualizer object
	 */
	private static VisualizerRemoteModule visualizerModule = null;

	/**
	 * 
	 */
	private static ControlCenterRemoteInterface ccRemote = null;

	public static void main(String[] args) {

		if (args.length != 2) {
			System.out
					.println("usage: gods.visualizer.Visualizer <ccHostName> <gods.config.xml>");
			System.exit(1);
		}

		PropertyConfigurator.configure(System
				.getProperty("org.apache.log4j.config.file"));

		String proxyName = "", localHost = "", hostName = "", ccName = "";
		hostName = args[0];
		
		log.info("INITIALIZING PROPERTIES...");
		if (!GodsProperties.initialize(args[1]))
			System.exit(1);
		log.info("\n----------------" + " GODS PROPERTIES INITIALIZED"
				+ "----------------");

		RemoteModuleProxy moduleProxy = null;
		boolean joinedLate = false;

		try {

			ccName = "//" + hostName + "/gods-cc";
			log.info("Checking for ControlCenter: " + ccName);

			ccRemote = findControlCenter(ccName);

			if (ccRemote == null) {

				joinedLate = true;
				if (launchControlCenter(hostName)) {

					ccRemote = findControlCenter(ccName);
					log.info(ccName + "...found");

					proxyName = "//" + hostName + "/gods-visualizer-proxy";
					log.info("Checking for visualizer-proxy: " + proxyName);

					if ((moduleProxy = findProxy(proxyName)) == null) {
						log.fatal("ControlCenter launched but " + proxyName
								+ " not found...");
						System.exit(1);
					}
				} else {
					System.exit(1);
				}

			} else {
				log.info(ccName + "...found");

				proxyName = "//" + hostName + "/gods-visualizer-proxy";
				log.info("Checking for visualizer-proxy: " + proxyName);

				if ((moduleProxy = findProxy(proxyName)) == null) {
					log.fatal("ControlCenter launched but " + proxyName
							+ " not found...");
					System.exit(1);
				}
			}

			addShutdownHook();

			// Initialize Visualizer
			Visualizer visualizer = Visualizer.getInstance();

			localHost = InetAddress.getLocalHost().getHostName();
			log.debug("Hostname:" + localHost);

			// Initialize Visualizer Remote Module
			visualizerModule = new VisualizerRemoteModule(ccRemote);
			visualizerModule.setRemoteModuleProxy(moduleProxy);
			visualizerModule.setJoinedLate(joinedLate);
			// VisualizerRemoteModule.start() runs in this thread, i.e.,
			// main thread
			visualizerModule.start();

		} catch (RemoteException re) {
			log.error(re.getMessage());

		} catch (MalformedURLException mfue) {
			log.error(mfue.getMessage());

		} catch (UnknownHostException uhe) {
			log.error(uhe.getMessage());
		}
	}

	private static ControlCenterRemoteInterface findControlCenter(String ccName) {

		ControlCenterRemoteInterface cc = null;

		try {
			cc = (ControlCenterRemoteInterface) Naming.lookup(ccName);

		} catch (RemoteException re) {
			log.error(re.getMessage());
			re.printStackTrace();

		} catch (NotBoundException nbe) {
			log.info(nbe.getMessage());

		} catch (MalformedURLException mfue) {
			log.error(mfue.getMessage());
			mfue.printStackTrace();

		}
		return cc;
	}

	private static RemoteModuleProxy findProxy(String proxyName) {

		RemoteModuleProxy proxy = null;

		try {
			proxy = (RemoteModuleProxy) Naming.lookup(proxyName);

		} catch (RemoteException re) {
			log.error(re.getMessage());

		} catch (NotBoundException nbe) {
			log.error(nbe.getMessage());

		} catch (MalformedURLException mfue) {
			log.error(mfue.getMessage());
		}
		return proxy;
	}

	private static boolean launchControlCenter(String hostName) {

		boolean result = false;

		try {
			String command = /* "ssh ozair@" + hostName + " " + */ccLaunchScript
					+ " " + ccSetupScript;

			Process cc = Runtime.getRuntime().exec(command);

			if (cc.waitFor() != 0) {
				log.fatal("Could not launch ControlCenter on " + hostName);
			} else {
				result = true;
			}

		} catch (InterruptedException ie) {
			log.fatal(ie.getMessage());

		} catch (IOException ioe) {
			log.fatal(ioe.getMessage());

		}
		return result;
	}

	public static Visualizer getInstance() {
		if (visualizerInstance == null) {
			visualizerInstance = new Visualizer();
			log.debug("Single VisualizerReference: " + visualizerInstance);
		}

		return visualizerInstance;
	}

	/**
	 * @param control_center
	 */
	private Visualizer() {
		// super("Visualizer");
		main_display = new MainDisplay(this, ccRemote);
	}

	/**
	 * @param event
	 */
	public void updateSlotInfo(SlotInformationChanged event) {
		main_display.SlotInfoChange(event);
	}

	private static void addShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					Naming.unbind("//"
							+ InetAddress.getLocalHost().getHostName()
							+ "/gods-visualizer");

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
}
