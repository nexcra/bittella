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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * The <code>DksRandomArgsGenerator</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class DksRandomArgsGenerator extends AbstractArgumentGenerator {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger.getLogger(DksRandomArgsGenerator.class);

	/**
	 * Arity in DKS ring. Attribute required to know ring size
	 */
	private int arity;

	/**
	 * Number of levels in DKS ring. Attribute required to know ring size
	 */
	private int levels;

	/**
	 * The port on which DKS nodes will listen
	 */
	private String port = "12345";

	/**
	 * Milliseconds to wait before joining the ring
	 */
	int millisecs = 1000;

	/**
	 * @param numberOfInstances
	 */
	public DksRandomArgsGenerator(String parametersFileName)
			throws FileNotFoundException, IOException {
		super(parametersFileName);
	}

	/**
	 * {@link gods.churn.AbstractArgumentGenerator#initialize(java.lang.String)}
	 */
	public boolean initialize(String parametersFileName)
			throws FileNotFoundException, IOException {

		log.debug("INITIALIZING" + this);

		InputStream parametersFile = new FileInputStream(parametersFileName);
		Properties properties = new Properties();
		properties.loadFromXML(parametersFile);
		parametersFile.close();

		String propertyValue = null;
		if ((propertyValue = properties.getProperty("dks.arity")) != null) {
			arity = Integer.parseInt(propertyValue);
		} else {
			log.error("Parameter \"arity\" not specified in file: "
					+ parametersFileName);
			return false;
		}

		if ((propertyValue = properties.getProperty("dks.levels")) != null) {
			levels = Integer.parseInt(propertyValue);
		} else {
			log.error("Parameter \"levels\" not specified in file: "
					+ parametersFileName);
			return false;
		}

		if ((propertyValue = properties.getProperty("dks.port")) != null) {
			port = propertyValue;
		} else {
			log.error("Parameter \"port\" not specified in file: "
					+ parametersFileName);
			return false;
		}

		log.debug("HERE NOW DKS Arguments Generator: " + " Arity= " + arity
				+ " Levels= " + levels + " Nodes= " + numberOfInstances);

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gods.churn.AbstractArgumentGenerator#generateArguments()
	 */
	@Override
	public String[] generateArguments() {
		log.debug("GENERATING" + this);

		log
				.debug("DKS ID Generation Parameters: "
						+ " Arity= " + arity + " Levels= " + levels
						+ " Nodes= " + numberOfInstances);

		String[] args = new String[numberOfInstances];

		int ringSize = (int) Math.pow(arity, levels);
		log.debug("DKS RING SIZE: " + ringSize);

		int interval = ringSize / numberOfInstances;
		log.debug("INTERVAL: " + interval);

		int nodeId = 0;
		StringBuffer nodeIds = new StringBuffer();

		args[0] = " create 1 " + port;
		
		for (int i = 1; i < numberOfInstances; i++) {
			nodeId = interval * i;
			args[i] = " join " + nodeId + " " + port;
			nodeIds.append("\t" + nodeId);
		}

		log.debug(nodeIds);

		return args;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see gods.churn.AbstractArgumentGenerator#setNumberOfInstances(int)
	 */
	@Override
	public void setNumberOfInstances(int numberOfInstances) {
		super.setNumberOfInstances(numberOfInstances);
		log.debug("Arity = " + arity + " Levels = " + levels);
	}

}
