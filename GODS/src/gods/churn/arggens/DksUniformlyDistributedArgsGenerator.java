/**
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
 * The <code>DksUniformlyDistributedIds</code> class generates node ids that
 * are uniformly distributed on the DKS Ring for launching multiple DKS nodes
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public class DksUniformlyDistributedArgsGenerator extends
		AbstractArgumentGenerator {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger
			.getLogger(DksUniformlyDistributedArgsGenerator.class);

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
	 * @param parametersFileName
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public DksUniformlyDistributedArgsGenerator(String parametersFileName)
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

	/**
	 * 
	 * {@link gods.churn.AbstractArgumentGenerator#generateArguments()}
	 */
	public String[] generateArguments() {
		
		log.debug("GENERATING" + this);
		
		log.debug("SOMEWHERE NOW DKS ID Generation Parameters: " + " Arity= " + arity
				+ " Levels= " + levels + " Nodes= " + numberOfInstances);

		String[] args = new String[numberOfInstances];

		int ringSize = (int) Math.pow(arity, levels);
		log.debug("DKS RING SIZE: " + ringSize);

		int interval = ringSize / numberOfInstances;
		log.debug("INTERVAL: " + interval);

		int nodeId = 0;
		StringBuffer nodeIds = new StringBuffer();

		for (int i = 0; i < numberOfInstances; i++) {
			nodeId = interval * i;
			args[i] = " join " + Integer.toString(nodeId) + " " + port;
			nodeIds.append("\t" + nodeId);
		}

		log.debug(nodeIds);

		return args;
	}

	/**
	 * @return the arity of DKS ring
	 */
	public int getArity() {
		return arity;
	}

	/**
	 * @param arity
	 *            the arity of DKS ring
	 */
	public void setArity(int arity) {
		this.arity = arity;
	}

	/**
	 * @return the levels in DKS ring
	 */
	public int getLevels() {
		return levels;
	}

	/**
	 * @param levels
	 *            the levels in DKS ring
	 */
	public void setLevels(int levels) {
		this.levels = levels;
	}

	/**
	 * @return the port on which DKS nodes are to be launched
	 */
	public String getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port on which to launch DKS nodes
	 */
	public void setPort(String port) {
		this.port = port;
	}

	/* (non-Javadoc)
	 * @see gods.churn.AbstractArgumentGenerator#setNumberOfInstances(int)
	 */
	@Override
	public void setNumberOfInstances(int numberOfInstances) {
		super.setNumberOfInstances(numberOfInstances);
		log.debug("Arity = " + arity + " Levels = " + levels);
	}

}
