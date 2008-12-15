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
import gods.churn.ArgumentGenerator;
import gods.churn.ArgumentGeneratorFactory;
import gods.churn.events.LaunchApplicationEvent;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

/**
 * The <code>LaunchApplicationTests</code> class is to test different cases of
 * launching application instance(s) on virtual node(s)
 * 
 * @author Ozair Kafray
 * 
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class LaunchApplicationTests extends TestCase {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger.getLogger(LaunchApplicationTests.class);

	/**
	 * SN - Single Node
	 */
	private LaunchApplicationEvent launchApplicationSN;

	/**
	 * MNMM - Multiple Nodes on Single Machine
	 */
	private LaunchApplicationEvent launchApplicationMNSM;

	/**
	 * MNMM - Multiple Nodes on Multiple Machines
	 */
	private LaunchApplicationEvent launchApplicationMNMM;

	/**
	 * MNSR - Multiple Nodes on Single DKS ring
	 */
	private LaunchApplicationEvent launchApplicationMNSR;

	private static String APP_HOME = "/home/gods/dks/";

	/**
	 * Default constructor, this test case would not behave as desired if it is
	 * initialized with this constructor. It is therefore private.
	 */
	private LaunchApplicationTests() {

	}

	/**
	 * @param arg0
	 *            specifying the test function for this instance
	 */
	public LaunchApplicationTests(String arg0) {
		super(arg0);
	}

	/**
	 * Creates and initializes objects required for the test.
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		log.debug("Setting up...");

		String appLaunchScript = APP_HOME + "launch-dks.sh";

		launchApplicationSN = new LaunchApplicationEvent(1);
		launchApplicationSN.setAppLaunchCommand(appLaunchScript);

		launchApplicationMNSM = new LaunchApplicationEvent(1);
		launchApplicationMNSM.setAppLaunchCommand(appLaunchScript);

		launchApplicationMNMM = new LaunchApplicationEvent(1);
		launchApplicationMNMM.setAppLaunchCommand(appLaunchScript);

		launchApplicationMNSR = new LaunchApplicationEvent(1);
		launchApplicationMNSR.setAppLaunchCommand(appLaunchScript);

		int[] slotIdSN = new int[1];
		slotIdSN[0] = 45;
		launchApplicationSN.setSlotIds(slotIdSN);

		int[] slotIdsMNSM = new int[2];
		slotIdsMNSM[0] = 13;
		slotIdsMNSM[1] = 14;
		launchApplicationMNSM.setSlotIds(slotIdsMNSM);

		int[] slotIdsMNMM = new int[2];
		slotIdsMNMM[0] = 16;
		slotIdsMNMM[1] = 45;
		launchApplicationMNMM.setSlotIds(slotIdsMNMM);

		int noOfNodes = ChurnTestsProperties.getNodes();
		int[] slotIdsMNSR = new int[noOfNodes];
		for (int i = 0; i < noOfNodes; i++) {
			slotIdsMNSR[i] = i + 1;
		}
		launchApplicationMNSR.setSlotIds(slotIdsMNSR);

		ArgumentGenerator argGen = ArgumentGeneratorFactory
				.getArgumentGenerator(ChurnTestsProperties
						.getArgumentGeneratorName(), ChurnTestsProperties
						.getParametersFileName());
		argGen.setNumberOfInstances(noOfNodes);

		launchApplicationMNSR.setArguments(argGen.generateArguments());
	}

	/**
	 * Tests launching of an application on a Single virtual Node(SN)
	 */
	public void testLaunchApplicationSN() {
		System.out.println(ControlCenter.getInstance());
		ControlCenter.getInstance().enqueueEvent(launchApplicationSN);
	}

	/**
	 * Tests launching of an application on Multiple Nodes on a Single
	 * Machine(MNSM)
	 */
	public void testLaunchApplicationMNSM() {
		System.out.println(ControlCenter.getInstance());
		ControlCenter.getInstance().enqueueEvent(launchApplicationMNSM);
	}

	/**
	 * Tets launching of an application on Multiple Nodes on Multiple
	 * Machines(MNMM)
	 */
	public void testLaunchApplicationMNMM() {
		System.out.println(ControlCenter.getInstance());
		ControlCenter.getInstance().enqueueEvent(launchApplicationMNMM);
	}

	/**
	 * Tets launching of number of nodes in a single DKS ring
	 */
	public void testDKSNodesOnSingleRing() {
		System.out.println(ControlCenter.getInstance());
		ControlCenter.getInstance().enqueueEvent(launchApplicationMNSR);
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
}
