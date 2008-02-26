package de.tud.kom.p2psim.impl.churn;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StaticChurnModelTest extends ChurnGeneratorTest {

	@Before
	public void setUp() {
		super.setUp();
		churnGen.setChurnModel(new StaticChurnModel());
	}

	@Test
	public void testHostFiltering() {
		runSimulation(SIM_END);
		Assert.assertEquals(churnHosts, churnGen.hosts);
	}

}
