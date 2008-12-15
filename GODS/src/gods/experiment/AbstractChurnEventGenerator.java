/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.experiment;

/**
 * The <code>ChurnEventGenerator</code> class
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public abstract class AbstractChurnEventGenerator implements
		ChurnEventGenerator {

	/**
	 * The parameters required to construct an EventGenerator
	 */
	public static Class[] generatorConstructorParameters = { int.class, int.class };

	/**
	 * Size of the deployed model network
	 */
	private int noOfVnodes;

	/**
	 * A seed for random generation of experiment events.
	 */
	private int seed;

	/**
	 * @param noOfVnodes
	 * @param seed
	 */
	public AbstractChurnEventGenerator(int noOfVnodes, int seed) {
		this.noOfVnodes = noOfVnodes;
		this.seed = seed;
	}

	/**
	 * @return - ExperimentEvent which is the next generated event for the
	 *         experiment
	 */
	public abstract AbstractExperimentEvent getNextChurnEvent();

	/**
	 * @return the netSize
	 */
	public int getNoOfVnodes() {
		return noOfVnodes;
	}

	/**
	 * @param netSize
	 *            the netSize to set
	 */
	public void setNoOfVnodes(int noOfVnodes) {
		this.noOfVnodes = noOfVnodes;
	}

	/**
	 * @return the seed
	 */
	public int getSeed() {
		return seed;
	}

	/**
	 * @param seed
	 *            the seed to set
	 */
	public void setSeed(int seed) {
		this.seed = seed;
	}
}
