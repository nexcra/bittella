package uc3m.netcom.overlay.bt;

/**
 * Some simple utils for debugging:
 * If you use this methods, after you have finished,
 * you can easiely find all uses of this methods(with eclipse) and delete them fast.
 * This makes sure, you don't forget some debug output.
 * @author Jan Stolzenburg
 */
public class BTDebugUtils {
	
	public static final void pout(String theMessage) {
		System.out.println(">>>DEBUG: " + theMessage);
	}
	
	public static final void perr(String theMessage) {
		System.err.println(">>>DEBUG: " + theMessage);
	}
	
	/**
	 * Use this method for asserts. The java build in asserts are normaly turnd off and therefore useless.
	 * @param theAssertion The assertion you make.
	 */
	public static final void checkAssert(boolean theAssertion) {
		checkAssert(theAssertion, "[NO TEXT]");
	}
	
	/**
	 * Use this method for asserts. The java build in asserts are normaly turnd off and therefore useless.
	 * @param theAssertion The assertion you make.
	 * @param theMessage The message that will be printed, if the assertion fails.
	 */
	public static final void checkAssert(boolean theAssertion, String theMessage) {
		if (! theAssertion)
			throw new RuntimeException("An assertion failed: '" + theMessage + "'!");
	}
	
}
