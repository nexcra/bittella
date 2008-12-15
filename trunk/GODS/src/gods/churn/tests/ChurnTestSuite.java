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

import gods.GodsProperties;

import java.io.FileNotFoundException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;

/**
 * The <code>ChurnTestSuite</code> class. This class is of no use currently
 *
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class ChurnTestSuite extends TestSuite{
	
	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger.getLogger(ChurnTestSuite.class);
	
	public ChurnTestSuite(){
		super("ChurnTestSuite");
		initialize();
	}
	
	public void initialize(){
		
		try {
			ChurnTestsProperties.initialize(GodsProperties.getGodsHome()
					+ "/config/tests.config/churn-test.xml");
			
			//Argument Generator Tests
			//addTest(new ArgumentGeneratorTests());
			log.debug("initializing");
			//Launch Application Tests
			//addTest(new LaunchApplicationTests("testLaunchApplicationSN"));
			//addTest(new LaunchApplicationTests("testLaunchApplicationMNSM"));
			//addTest(new LaunchApplicationTests("testLaunchApplicationMNMM"));
			addTest(new LaunchApplicationTests("testDKSNodesOnSingleRing"));
			
			//Kill Application Tests
			//addTest(new KillApplicationTests("testKillApplicationSN"));
			
			
		} catch (FileNotFoundException fnfe) {
			log.error(fnfe.getMessage());
		}
	}
	/**
	 * @return Test Suite for Churn Tests
	 */
	public static Test suite(){
		TestSuite suite = new TestSuite("Gods Churn Module Tests");
		try{
			ChurnTestsProperties.initialize("./churn-test.xml");
			
			//Add Launch Application Tests
			//suite.addTest(new LaunchApplicationTests("testLaunchApplicationSN"));
			//suite.addTest(new LaunchApplicationTests("testLaunchApplicationMNSM"));
			//suite.addTest(new LaunchApplicationTests("testLaunchApplicationMNMM"));
			
			//Add Kill Application Tests
			//suite.addTest(new KillApplicationTests("testKillApplication"));
			
			//Add StopApplicationTests
			
		}
		catch(FileNotFoundException fnfe){
			log.error(fnfe.getMessage());
		}
		

		
		return suite;
	}
}
