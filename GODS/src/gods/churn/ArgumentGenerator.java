/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.churn;

/**
 * The <code>ArgumentGenerator</code> provides an interface for classes
 * required for generating arguments for instances of an application to be
 * launched simultaneously.
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public interface ArgumentGenerator {

	/**
	 * Generates Arguments for the number of instances specified for this
	 * instance of ArgumentGenerator. Calling this function before
	 * {@link gods.churn.AbstractArgumentGenerator#initialize(String)} might
	 * give undesirable results.
	 * 
	 * @return An array of strings where each string represents all command
	 *         arguments to be supplied to a single application instance
	 */
	public String[] generateArguments();

	/**
	 * @return the number Of Arguments Instances
	 */
	public int getNumberOfInstances();
	
	/**
	 * @param numberOfInstances - number of argument instances to generate
	 */
	public void setNumberOfInstances(int numberOfInstances);
	
	/*
	 * Initializes the parameters required for arguments generation throught the
	 * specified file. This function should be called before calling
	 * {@link gods.churn.AbstractArgumentGenerator#generateArguments()} function
	 * 
	 * @param parametersFileName
	 *            Name of the file specifying parameters for generation of
	 *            arguments by this class
	 * @return true for successful initialization of argument generation
	 *         parameters, false otherwise.
	 * @throws FileNotFoundException
	 *             if specified file is not found
	 * @throws IOException
	 *             for an IOException by the underlying FileInputStream
	 *
	public boolean initialize(String parametersFileName)
			throws FileNotFoundException, IOException;
	 */

}
