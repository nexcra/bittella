package de.tud.kom.p2psim.impl.util.stats;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.DistributionFactory;
import org.apache.commons.math.distribution.DistributionFactoryImpl;
import org.apache.commons.math.distribution.TDistributionImpl;

/**
 * As it is not possible to get a perfect estimate of the population mean from
 * any finite number of finite size samples, it is necessary to calculate
 * probabilistic bounds. The best we can do is to get the probibilistic bounds,
 * for instance, x1 and x2, such that there is a high probability, 1 - alpha,
 * that the population mean is in the interval (x1, x2) which is also referred
 * to as the confidence interval for the population mean. Alpha denots the
 * significance level (expressed as a fraction), 100(1-alpha) the confidence
 * level (expressed as percentage) and 1-a is called the confidence coefficient.
 * 
 * @author Sebastian Kaune
 * @author Konstantin Pussep
 * @version 3.0, 12/06/2007
 * 
 */
public class ConfidenceInterval {

	/**
	 * Returns the delta between the mean and the lower(x1)/upper(x2) bound as
	 * positive number. That is, the probabilistic bounds of x1 and x2 are given
	 * by
	 * 					x1 <= mean <= x2 				<=> 
	 * 			mean-delta <= mean <= mean + delta
	 * 
	 * @param sdev
	 *            the given standard deviation
	 * @param n
	 *            the given sample size
	 * @param alpha
	 *            the given significance level
	 * @return the upper/lower bound as positiv number
	 */
	public static double getDeltaBound(double sdev, int n, double alpha) {
		TDistributionImpl tDist = new TDistributionImpl(n - 1);
		double errorConfCoeff = (1d - alpha) / 2;
		double delta;
		try {
			double t = Math.abs(tDist.inverseCumulativeProbability(errorConfCoeff));
			delta = t * sdev / Math.sqrt(n);
		} catch (MathException e) {
			throw new IllegalStateException(e);
		}
		return delta;
	}
}
