package de.tud.kom.p2psim.impl.churn;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.stat.StatUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.tud.kom.p2psim.api.common.Host;
import de.tud.kom.p2psim.impl.churn.ExponentialChurnModel.ChurnData;
import de.tud.kom.p2psim.impl.churn.ExponentialChurnModel.UserType;
import de.tud.kom.p2psim.impl.simengine.Simulator;

public class ExponentialChurnModelTest extends ChurnGeneratorTest {

	ExponentialChurnModel model;

	ChurnTestStub testStub;

	@Before
	public void setUp() {
		super.setUp();
		model = new ExponentialChurnModel();
		model.setChurnFactor(0.5d);
		model.setMeanSessionLength(1 * Simulator.HOUR_UNIT);
		testStub = new ChurnTestStub(this.churnHosts, this);
//		churnGen.setTestStub(testStub);
		churnGen.setChurnModel(model);

	}

	ChurnData getChurnData(Host host) {
		return model.hosts.get(host);
	}

	@Test
	public void testHostFiltering() {
		runSimulation(SIM_END);
		Assert.assertEquals(churnHosts, churnGen.hosts);
	}


	@Test
	public void testFractions() {
		runSimulation(SIM_END);
		int longL = 0;
		int normal = 0;
		int trans = 0;

		for (ChurnData churnData : model.hosts.values()) {
			if (churnData.type.equals(UserType.LONG_LASTING))
				longL++;
			else if (churnData.type.equals(UserType.NORMAL))
				normal++;
			else
				trans++;
		}

		Assert.assertTrue(model.longLastingFraction * churnHosts.size() * 0.90 < longL && longL < model.longLastingFraction * churnHosts.size() * 1.1);
		Assert.assertTrue(model.normalFraction * churnHosts.size() * 0.90 < normal && normal < model.normalFraction * churnHosts.size() * 1.1);
		Assert.assertTrue(model.transientFraction * churnHosts.size() * 0.90 < trans && trans < model.transientFraction * churnHosts.size() * 1.1);
	}

	@Ignore
	@Test
	public void testUptimeChurnfactor() {
		runSimulation(SIM_END);

		double[] sample = new double[testStub.uptimePeers.size()];
		int i = 0;
		for (Double samp : testStub.uptimePeers) {
			sample[i] = samp;
			i++;
		}
		double result = StatUtils.mean(sample);
		Assert.assertTrue(model.churnFactor * churnHosts.size() * 0.7 < result && result < model.churnFactor * churnHosts.size() * 1.3);

	}
	
	@After
	public void tearDown() {
		super.tearDown();
	}

	@Ignore
	@Test
	public void testTransientUptimes() {
		this.runSimulation(SIM_END);

		double[] sample = new double[testStub.trans.size()];
		int i = 0;
		for (Double samp : testStub.trans) {
			sample[i] = samp;
			i++;
		}
		double result = StatUtils.mean(sample) / Simulator.MINUTE_UNIT;
		System.out.println(result);
	}

	@Ignore
	@Test
	public void testNormalUptimes() {
		this.runSimulation(SIM_END);

		double[] sample = new double[testStub.normal.size()];
		int i = 0;
		for (Double samp : testStub.normal) {
			sample[i] = samp;
			i++;
		}
		double result = StatUtils.mean(sample) / Simulator.MINUTE_UNIT;
		System.out.println(result);
	}

	@Ignore
	@Test
	public void testLongUptimes() {
		this.runSimulation(SIM_END);

		double[] sample = new double[testStub.longL.size()];
		int i = 0;
		for (Double samp : testStub.longL) {
			sample[i] = samp;
			i++;
		}
		double result = StatUtils.mean(sample) / Simulator.MINUTE_UNIT;
		System.out.println(result);

	}

	@Ignore
	@Test
	public void testAllUptimes() {
		this.runSimulation(SIM_END);

		double[] sample = new double[testStub.all.size()];
		int i = 0;
		for (Double samp : testStub.all) {
			sample[i] = samp;
			i++;
		}
		double result = StatUtils.mean(sample) / Simulator.MINUTE_UNIT;

		System.out.println(result);

	}

	public class ChurnTestStub {
		List<Double> trans;

		List<Double> normal;

		List<Double> longL;

		List<Double> all;

		Map<Host, Long> online;

		Map<Host, Long> offline;

		ExponentialChurnModelTest testClass;

		List<Double> uptimePeers;

		int onlineCounter;

		public ChurnTestStub(List<Host> hosts, ExponentialChurnModelTest testClass) {
			online = new HashMap<Host, Long>();
			offline = new HashMap<Host, Long>();
			longL = new LinkedList<Double>();
			normal = new LinkedList<Double>();
			trans = new LinkedList<Double>();
			all = new LinkedList<Double>();
			uptimePeers = new LinkedList<Double>();
			this.testClass = testClass;
			for (Host host : hosts) {
				online.put(host, 0l);
			}
			uptimePeers.add(new Double(onlineCounter));

		}

		public void offlineEvent(Host host, long time) {
			onlineCounter--;
			System.out.println(onlineCounter);
			uptimePeers.add(new Double(onlineCounter));
			Long online = this.online.remove(host);
			ChurnData data = testClass.getChurnData(host);
			if (data.type.equals(UserType.LONG_LASTING))
				this.longL.add(new Double(time - online));
			else if (data.type.equals(UserType.NORMAL))
				this.normal.add(new Double(time - online));
			else
				this.trans.add(new Double(time - online));
			this.all.add(new Double(time - online));
		}

		public void onlineEvent(Host host, long time) {
			this.online.put(host, time);
			onlineCounter++;
			uptimePeers.add(new Double(onlineCounter));
		}
	}
}
