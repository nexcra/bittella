/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.experiment.cc;

import gods.arch.Event;
import gods.arch.EventHandler;
import gods.arch.Module;
import gods.experiment.Experiment;
import gods.experiment.events.EndExperiment;

import org.apache.log4j.Logger;

/**
 * The <code>EndExperimentHandler</code> class - Not in use
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class EndExperimentHandler extends EventHandler {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger.getLogger(EndExperimentHandler.class);

	/**
	 * @param event
	 * @param module
	 */
	public EndExperimentHandler(Event event, Module module) {
		super(event, module);
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gods.arch.EventHandler#handle()
	 */
	@Override
	public Object handle() throws ClassCastException {

		EndExperiment endExp = (EndExperiment) event;
		Experiment experiment = endExp.getExperiment();

		return null;
	}

}
