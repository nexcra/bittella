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

import gods.churn.ArgumentGenerator;
import gods.churn.ArgumentGeneratorFactory;

import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * The <code>ArgumentGeneratorFactoryTests</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class ArgumentGeneratorTests extends TestCase {

	private String[] displayNames;

	private String parametersFileName;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		displayNames = ArgumentGeneratorFactory.getAllDisplayNames();
		parametersFileName = "/home/ozair/workspace/gods-www/config/arggens.config/dks.uniformlydist.idparams.xml";

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * 
	 */
	public ArgumentGeneratorTests() {

	}

	/**
	 * @param arg0
	 */
	public ArgumentGeneratorTests(String arg0) {
		super(arg0);

	}

	public void testInitialize() {

	}

	public void testGetAllDisplayNames() {

		for (String displayName : displayNames) {
			System.out.println(displayName);
		}
	}

	public void testGetArgumentGenerator() {

		try {
			ArgumentGenerator argGen = ArgumentGeneratorFactory
					.getArgumentGenerator(displayNames[0], parametersFileName);
			
			argGen.setNumberOfInstances(32);
			String[] args = argGen.generateArguments();
			for (String arg : args) {
				System.out.print("\t" + arg);
			}
		} catch (FileNotFoundException fnfe) {
			System.out.println(fnfe.getMessage());
		} catch (IOException ioe) {
			System.out.println(ioe.getMessage());
		}
	}
}
