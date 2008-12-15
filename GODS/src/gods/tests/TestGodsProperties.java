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

import gods.GodsProperties;

import org.apache.log4j.PropertyConfigurator;

/**
 * The <code>TestGodsProperties</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class TestGodsProperties {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (System.getProperty("org.apache.log4j.config.file") != null) {
			PropertyConfigurator.configure(System
					.getProperty("org.apache.log4j.config.file"));
		} else {
			usage();
			System.exit(1);
		}

		if (args.length == 0) {
			usage();
			System.exit(1);
		}

		GodsProperties.initialize(args[0]);

	}

	private static void usage() {
		System.out.println("\nusage: java -Dgods.home=<gods.home> "
				+ "-Dorg.apache.log4j.config.file=<log4j.config> "
				+ "gods.tests.TestGodsProperties <gods.config.xml>\n");
	}

}
