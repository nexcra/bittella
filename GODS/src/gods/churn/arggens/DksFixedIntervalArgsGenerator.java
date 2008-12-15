/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.churn.arggens;

import gods.churn.AbstractArgumentGenerator;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * The <code>DksFixedIntervalArgsGenerator</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class DksFixedIntervalArgsGenerator extends AbstractArgumentGenerator {

	/**
	 * @param parametersFileName
	 * @param numberOfInstances
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public DksFixedIntervalArgsGenerator(String parametersFileName)
			throws FileNotFoundException, IOException {
		super(parametersFileName);
	}

	/**
	 * {@link gods.churn.AbstractArgumentGenerator#generateArguments()}
	 */
	@Override
	public String[] generateArguments() {

		return null;
	}

	/**
	 * {@link gods.churn.AbstractArgumentGenerator#initialize(java.lang.String)}
	 */
	public boolean initialize(String parametersFileName)
			throws FileNotFoundException, IOException {

		return false;
	}

}
