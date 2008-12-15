/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.experiment.agent;

import static gods.Gods.streamToString;

import gods.agent.AgentProperties;
import gods.arch.Event;
import gods.arch.EventHandler;
import gods.arch.Module;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

/**
 * The <code>SyncClocksHandler</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class SyncClocksHandler extends EventHandler {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger.getLogger(SyncClocksHandler.class);

	/**
	 * TODO
	 */
	private String syncScriptRelative = "scripts/syncclock.sh";

	/**
	 * @param event
	 * @param module
	 */
	public SyncClocksHandler(Event event, Module module) {
		super(event, module);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gods.arch.EventHandler#handle()
	 */
	@Override
	public Object handle() throws ClassCastException {

		try {
			log.debug("SYNC============SYNC==========SYNC===========SYNC");
			syncClock();
			
			//TODO Send ClockSynced event back to ControlCenter
			
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		} catch (InterruptedException ie) {
			System.out.println(ie.getMessage());
		}

		return null;
	}

	private void syncClock() throws IOException, InterruptedException {

		String syncScript = AgentProperties.getAgentHome() + syncScriptRelative;
		String command = syncScript;
		log.debug(command);

		Process sync = Runtime.getRuntime().exec(command);

		InputStream stdout = new BufferedInputStream(sync.getInputStream());
		InputStream stderr = new BufferedInputStream(sync.getErrorStream());

		//if (sync.waitFor() != 0) {
			System.out.println("Output: " + streamToString(stdout));
			System.out.println("Error: " + streamToString(stderr));
		//}

		/*
		 * String syncScript = GodsProperties.getGodsHome() +
		 * syncScriptRelative; String hostsFile = GodsProperties.getGodsHome() +
		 * hostsFileRelative;
		 * 
		 * String command = syncScript + " " + hostsFile; log.debug(command);
		 */
	}

}
