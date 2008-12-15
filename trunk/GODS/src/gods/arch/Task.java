/**
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.arch;

import java.io.Serializable;

/**
 * The <code>Task</code> class is a task sent by ControlCenter to a GodsAgent
 * for synchronous execution
 * 
 * @author Ozair Kafray
 * @version $Id: Task.java 258 2006-11-28 13:05:40Z cosmin $
 */
public interface Task extends Serializable {

	/**
	 * This method must be overwritten by all classes implementing this
	 * interface, in which the actual task code is to be written.
	 * 
	 * @return Object that is the result of the executing this task.
	 */
	public Object execute();
}
