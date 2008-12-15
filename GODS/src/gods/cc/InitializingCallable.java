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

import java.util.concurrent.Callable;

/**
 * The <code>InitializingCallable</code> class
 *
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class InitializingCallable implements Callable<Void> {

	/**
	 * 
	 */
	public InitializingCallable() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	public Void call() throws Exception {
		
		int i=0;
		int j=1;
		int k = j+i;
		
		return null;
	}

}
