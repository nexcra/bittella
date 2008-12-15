/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.experiment.tests;

import gods.GodsProperties;
import gods.cc.ControlCenter;
import gods.experiment.Experiment;
import gods.experiment.events.RunExperiment;

import junit.framework.TestCase;

/**
 * The <code>ExperimentTests</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class ExperimentTests extends TestCase {

	private String experimentFile = "/experiments/dks.joins.100/dks.joins.100.exp";

	/**
	 * Default constructor, this test case would not behave as desired if it is
	 * initialized with this constructor. It is therefore private.
	 */
	private ExperimentTests() {
	}

	/**
	 * @param arg0
	 */
	public ExperimentTests(String arg0) {
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
	}

	public void testStartExperiment() {

		String experimentFileFull = GodsProperties.getGodsHome()
				+ experimentFile;
		RunExperiment runExp = new RunExperiment(1);

		runExp.setExperiment(Experiment.load(experimentFileFull));

		ControlCenter.getInstance().enqueueEvent(runExp);
	}

	/**
	 * To release any resources taken for this test
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {

		super.tearDown();
	}

}
