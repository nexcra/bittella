/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.tests;

import gods.Gods;
import gods.experiment.tests.ExperimentTests;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * The <code>GodsTestSuite</code> class contains tests suites for all GODS
 * modules. The Gods main is also called from this class for tests so that the
 * Test Classes and Gods classes are loaded by same Class Loader.
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class GodsTestSuite implements Runnable {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger.getLogger(GodsTestSuite.class);

	private static String[] godsArgs = null;

	// private static String[] testSuites = null;

	public static Test suite() {
		TestSuite suite = new TestSuite("GODSTestSuite");

		//suite.addTest(new ChurnTestSuite());
		suite.addTest(new ExperimentTests("testStartExperiment"));

		return suite;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		PropertyConfigurator.configure(System
				.getProperty("org.apache.log4j.config.file"));

		if (args.length < 2) {
			System.out
					.println("usage: gods.GodsTestSuite -gods <gods-startup.xml>");
			System.exit(1);
		}

		try {
			int count = 0;
			for (String arg : args) {

				++count;

				if (arg.equals("-gods")) {
					break;
				}
			}

			godsArgs = new String[args.length - count];

			for (int i = 0; i < godsArgs.length; i++) {
				godsArgs[i] = args[count + i];
			}

			System.out.println("Gods Arguments are: ");
			for (String godsArg : godsArgs) {
				System.out.println(godsArg + " ");
			}

			new Thread(new GodsTestSuite()).start();

			InputStreamReader inputStreamReader = new InputStreamReader(
					System.in);
			BufferedReader stdin = new BufferedReader(inputStreamReader);

			System.out.println("Press any key when system is ready for tests");
			String input = stdin.readLine();
			System.out.println("Read input: " + input);
			if (input != null) {
				junit.textui.TestRunner.run(suite());
			}

		} catch (Exception ie) {
			System.out.println(ie.getMessage());
		}
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	public void run() {

		Gods.main(godsArgs);
	}
}
