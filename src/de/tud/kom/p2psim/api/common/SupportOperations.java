package de.tud.kom.p2psim.api.common;
/**
 * Components implement this interface to receive the operation's result 
 * (mainly overlays and applications do this). This means they can create new operations
 * from the configuration data (via the <code>createOperation</code> method) and can provide
 * methods with <code>OperationCallback</code> as parameter. Additionally, this component
 * must implement the <code>OperationCallback</code> interface to be informed itself about the operation status. 
 * <p>
 * There is a contract how the public method of components with operations should look like: Each method
 * using an operation internally should accept an operation callback as its last interface and
 * return the operation's id, e.g.
 * <code> public int foo(OperationCallback callback); or public int foo2(Object someParam; OperationCallback callback); 
 * </code> where the returned value is the operation's id.
 * 
 * @author Sebastian Kaune <kaune@kom.tu-darmstadt.de>
 * @author Konstantin Pussep
 * @version 0.3, 27.11.2007
 * 
 * @see Operation
 * @see OperationCallback
 */
//Note, owner can receive any operationSucceeded/Failed callbacks, so no paramerization for the Operation callback
public interface SupportOperations extends Component, OperationCallback{//<T  extends Component>

	/**
	 * Create an appropriate Operation. The component checks whether it
	 * can create an operation with the given name and number of parameters.
	 * This method is used <b>only</b> to specify operations via configuration (action) files. 
	 * @param opName - operation name
	 * @param params - operation parameters if required.
	 * @param caller - caller Object which will be informed when the operation finishes (successfully or with a failure)
	 * @return operation - created (can be scheduled or executed immediately)
	 */
	public Operation createOperation(String opName, String[] params, OperationCallback caller);

}