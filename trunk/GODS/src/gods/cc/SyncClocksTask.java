/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.cc;

import static gods.Gods.streamToString;

import gods.agent.AgentProperties;
import gods.arch.Task;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

/**
 * The <code>SyncClocksTask</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class SyncClocksTask implements Task {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3050963355811321566L;

	/**
	 * An instance of Logger for this class
	 */
	private static Logger log = Logger.getLogger(SyncClocksTask.class);

	/**
	 * TODO
	 */
	private String syncScriptRelative = "scripts/syncclock.sh";

	/**
	 * 
	 */
	public SyncClocksTask() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gods.arch.Task#execute()
	 */
	public Object execute() {
		try {
			log.debug("SYNC============SYNC==========SYNC===========SYNC");

			String syncScript = AgentProperties.getAgentHome()
					+ syncScriptRelative;
			String command = syncScript;
			log.debug(command);

			Process sync = Runtime.getRuntime().exec(command);

			InputStream stdout = new BufferedInputStream(sync.getInputStream());
			InputStream stderr = new BufferedInputStream(sync.getErrorStream());

			if (sync.waitFor() != 0) {
				log.debug("Output: " + streamToString(stdout));
				log.debug("Error: " + streamToString(stderr));
			}
		} catch (InterruptedException ie) {

		} catch (IOException ioe) {

		}
		return null;
	}

}
