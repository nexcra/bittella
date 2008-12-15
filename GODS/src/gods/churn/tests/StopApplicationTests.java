/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.churn.tests;

import gods.cc.ControlCenter;
import gods.churn.events.StopApplicationEvent;

import java.io.IOException;

import junit.framework.TestCase;

/**
 * The <code>StopApplicationTest</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class StopApplicationTests extends TestCase {

	private StopApplicationEvent stopApplication;

	/**
	 * Default constructor, this test case would not behave as desired if it is
	 * initialized with this constructor. It is therefore private.
	 */
	private StopApplicationTests() {
	}

	/**
	 * @param arg0
	 *            specifying the test function for this instance
	 */
	public StopApplicationTests(String arg0) {
		super(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		stopApplication = new StopApplicationEvent(1);
	}

	public void testStopApplication() {
		try {
			System.out
					.println("Please enter process id of application node to be stopped");
			
			int slotid = System.in.read();
			
			int[] slotids = new int[1];
			slotids[0] = slotid;
			
			stopApplication.setProcessIds(slotids);
			System.out.println(ControlCenter.getInstance());
			ControlCenter.getInstance().enqueueEvent(stopApplication);

		} catch (IOException e) {
			System.out
					.println("IOException while reading process id from console");
		}
	}
}
