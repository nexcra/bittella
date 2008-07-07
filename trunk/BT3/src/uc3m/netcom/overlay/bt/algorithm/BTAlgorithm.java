package uc3m.netcom.overlay.bt.algorithm;

/**
 * This interface is used by all algorithms.
 * It just provides a method to ask, if this algorithm is set up correctly and ready to work.
 * @author Jan Stolzenburg
 */
public interface BTAlgorithm {
	
	/**
	 * Is this algorithm set up correctly and ready to work?
	 * @return Is it ready to work?
	 */
	public boolean isSetup();
	
}
