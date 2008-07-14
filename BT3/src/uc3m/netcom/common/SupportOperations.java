package uc3m.netcom.common;
/**
 * Components implement this interface to receive the operation's result 
 * (mainly overlays and applications do this). This means they can provide
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
public interface SupportOperations extends OperationCallback{//<T  extends Component>

}