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

import gods.arch.Event;
import gods.arch.EventHandler;
import gods.arch.Module;
import gods.cc.ControlCenter;
import gods.topology.events.SlotInformationChanged;

/**
 * The <code>SlotsInformationRequestHandler</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class SlotsInformationRequestHandler extends EventHandler {

	/**
	 * @param event
	 * @param module
	 */
	public SlotsInformationRequestHandler(Event event, Module module) {
		super(event, module);

	}

	/**
	 * {@link gods.arch.EventHandler#handle()}
	 */
	@Override
	public Object handle() throws ClassCastException {

		SlotInformationChanged changedSlotsInformation = new SlotInformationChanged(
				1);
		changedSlotsInformation.setChangedSlots(ControlCenter.getInstance()
				.getAllSlots());
		ControlCenter.getInstance().enqueueEvent(changedSlotsInformation);

		return null;
	}

}
