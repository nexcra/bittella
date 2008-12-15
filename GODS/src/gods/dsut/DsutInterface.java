/**
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.dsut;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The <code>DsutInterface</code> is a remote interface for GODS Agents to
 * communicate with Distributed Systems Under Test.
 * 
 * @author Ozair Kafray
 * @version $Id: DsutInterface.java 258 2006-11-28 13:05:40Z cosmin $
 */
public interface DsutInterface extends Remote {

	/**
	 * invokes an operation on the Distributed System Under Test. The operation
	 * must have been selected for test earlier through conifg files.
	 * 
	 * @return Object which is the result of invoking an operation.
	 * @throws RemoteException
	 */
	public Object invokeOperation(/* OP_ID, OP_ARGS[] */)
			throws RemoteException;

	/**
	 * interface for an agent to get DSUT state.
	 * 
	 * @throws RemoteException
	 */
	public void readDSUTState(/* STATE_ID */) throws RemoteException;
}
