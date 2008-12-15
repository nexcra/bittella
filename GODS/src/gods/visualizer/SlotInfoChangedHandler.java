/**
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */

package gods.visualizer;

import gods.arch.Event;
import gods.arch.Module;
import gods.arch.EventHandler;
import gods.topology.events.SlotInformationChanged;

/**
 * The <code>SlotInfoChangedHandler</code> class
 * 
 * @author Fredrik Holmgren
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class SlotInfoChangedHandler extends EventHandler {

	/**
	 * @param event
	 * @param module
	 */
	public SlotInfoChangedHandler(Event event, Module module) {
		super(event, module);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gods.arch.EventHandler#handle()
	 */
	@Override
	public Object handle() {
		Visualizer.getInstance().updateSlotInfo((SlotInformationChanged) event);
		return null;
	}

}
