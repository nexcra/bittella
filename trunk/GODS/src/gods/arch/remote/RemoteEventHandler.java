/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.arch.remote;

import gods.arch.Event;
import gods.arch.EventHandler;
import gods.arch.Module;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

/**
 * The <code>RemoteEventHandler</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class RemoteEventHandler extends EventHandler {

	/**
	 * An instance of Logger for this class
	 */
	private static Logger log = Logger.getLogger(RemoteEventHandler.class);

	/**
	 * @param event
	 * @param module
	 */
	public RemoteEventHandler(Event event, Module module) {
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
			log.debug("");
			System.out.println("========================================");
			RemoteModuleProxy moduleProxy = (RemoteModuleProxy) module;
			System.out.println("RemoteModule Proxy is: " + moduleProxy);
			RemoteModule rmodule = moduleProxy.getRemoteModule();
			System.out.println("Remote Module is: " + rmodule);

			if (rmodule != null) {
				rmodule.notifyEvent(event);
				System.out.println("Notified " + rmodule.toString() + " of event "
						+ event.getClass().getSimpleName());
			}
			else{
				System.out.println("Remote module not initialized...");
			}
			System.out.println("========================================");

		} catch (RemoteException re) {
			System.out.println("EXCEPTION: " + re.getMessage());
		}

		return null;
	}

}
