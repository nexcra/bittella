package de.tud.kom.p2psim.util;

import org.junit.Assert;
import org.junit.Test;

import de.tud.kom.p2psim.impl.util.stats.ConfidenceInterval;

/**
 * *
 * 
 * @author Sebastian Kaune
 * @author Konstantin Pussep
 * @version 3.0, 06.12.2007
 * 
 */
public class ConfidenceIntervalTest {

	/**
	 * Test the calculation of the correct delta
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDelta() throws Exception {
		double mean = 3.90;
		double sDev = 0.95;
		int n = 32;
		double alpha = 0.9;
		double delta = ConfidenceInterval.getDeltaBound(sDev, n, alpha);
		Assert.assertTrue(3.61d < (mean-delta) &&  (mean-delta) < 3.62d);
		Assert.assertTrue(4.16d < (mean+delta) &&  (mean-delta) < 4.17d);
	}
}
