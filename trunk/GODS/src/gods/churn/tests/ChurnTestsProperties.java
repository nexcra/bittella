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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * The <code>ChurnTestsProperties</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class ChurnTestsProperties {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger.getLogger(ChurnTestsProperties.class);

	/**
	 * The display name of the ArgumentGenerator to be used for passing
	 * arguments to DKS nodes
	 */
	private static String argumentGeneratorName = "";

	/**
	 * Name of the file which has the parameters for arguments generation
	 */
	private static String parametersFileName = "";

	/**
	 * Number of nodes on which DKS is to be launched
	 */
	private static int nodes;

	/**
	 * 
	 */
	private ChurnTestsProperties() {

	}

	public static void initialize(String propertiesFileName)
			throws FileNotFoundException {

		try {
			InputStream churnTestPropFile = new FileInputStream(
					propertiesFileName);
			Properties properties = new Properties();
			properties.loadFromXML(churnTestPropFile);
			churnTestPropFile.close();

			if (properties.containsKey("dks.arggen")) {
				argumentGeneratorName = properties.getProperty("dks.arggen");
			}

			if (properties.containsKey("dks.arggen.params")) {
				parametersFileName = GodsProperties.getGodsHome()
						+ properties.getProperty("dks.arggen.params");
			}

			if (properties.containsKey("dks.nodes")) {
				nodes = Integer.parseInt(properties.getProperty("dks.nodes"));
			}

			log.debug("Churn Test Properties: " + "ArgumentGenerator = "
					+ argumentGeneratorName + "Parameters File = "
					+ parametersFileName + " Nodes= " + nodes);

		} catch (IOException ioe) {
			log.error(ioe.getMessage());
		}

	}

	/**
	 * @return the nodes
	 */
	public static int getNodes() {
		return nodes;
	}

	/**
	 * @return the argumentGeneratorName
	 */
	public static String getArgumentGeneratorName() {
		return argumentGeneratorName;
	}

	/**
	 * @return the parametersFileName
	 */
	public static String getParametersFileName() {
		return parametersFileName;
	}

}
