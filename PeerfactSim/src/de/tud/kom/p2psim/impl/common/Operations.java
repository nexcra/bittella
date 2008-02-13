package de.tud.kom.p2psim.impl.common;

import de.tud.kom.p2psim.api.common.Operation;
import de.tud.kom.p2psim.api.common.OperationCallback;
import de.tud.kom.p2psim.api.common.SupportOperations;

/**
 * This class consists exclusively of static methods that either operate on
 * operations, return empty operations or empty operation callbacks.
 * 
 * @author Sebastian Kaune
 * @author Konstantin Pussep
 * @version 3.0, 12/05/2007
 * 
 */
public class Operations {

	/**
	 * Returns the empty operation callback (immutable)
	 */
	public static final OperationCallback EMPTY_CALLBACK = new EmptyCallback();

	static class EmptyCallback implements OperationCallback {

		public void calledOperationFailed(Operation op) {
			// do nothing
		}

		public void calledOperationSucceeded(Operation op) {
			// do nothing
		}

	}

	/**
	 * Invoking this method schedules immediately an empty operation without any
	 * functionality. In addition, the given callback will be informed that the
	 * operation has finished with success.
	 * 
	 * @param component
	 *            the owner (component) of the operation
	 * @param callback
	 *            the given callback
	 * @return the unique operation identifier
	 */
	@SuppressWarnings("unchecked")
	public static int scheduleEmptyOperation(SupportOperations component, OperationCallback callback) {
		Operation op = createEmptyOperation(component, callback);
		op.scheduleImmediately();
		return op.getOperationID();
	}

	/**
	 * Invoking this method creates an empty operation withoun an functionality.
	 * The execution of this operation will finish immediately with success.
	 * 
	 * @param component
	 *            The owner (component) of the operation
	 * @param callback
	 *            the given callback
	 * @return the created empty operation
	 */
	@SuppressWarnings("unchecked")
	public static Operation createEmptyOperation(SupportOperations component, OperationCallback callback) {
		Operation op = new AbstractOperation<SupportOperations, Object>(component, callback) {
			@Override
			protected void execute() {
				operationFinished(true);
			}

			@Override
			public Object getResult() {
				return null;
			}

		};
		return op;
	}
}
