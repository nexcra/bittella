package de.tud.kom.p2psim.impl.churn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.tud.kom.p2psim.api.common.Host;
import de.tud.kom.p2psim.api.scenario.Configurator;
import de.tud.kom.p2psim.api.scenario.HostBuilder;
import de.tud.kom.p2psim.impl.common.DefaultHost;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.util.ComponentTest;

public abstract class ChurnGeneratorTest extends ComponentTest {

	ExponentialChurnModel model;

	DefaultChurnGenerator churnGen;

	List<Host> churnHosts;

	List<Host> allHosts;

	protected static final long SIM_END = 2 * Simulator.HOUR_UNIT;

	@Before
	public void setUp() {
		super.setUp();
		churnHosts = new ArrayList<Host>();
		allHosts = new ArrayList<Host>();
		Host host;
		for (int i = 0; i < 10000; i++) {
			host = createChurnHost();
			churnHosts.add(host);
			allHosts.add(host);
		}

		for (int i = 0; i < 100; i++) {
			host = createWithoutChurnHost();
			allHosts.add(host);
		}
		HostBuilder builder = new TestBuilder(allHosts);
		churnGen = new DefaultChurnGenerator();
		churnGen.hostBuilder = builder;
		churnGen.setStart(0);
		churnGen.setStop(SIM_END);
	}
	
	@After
	public void tearDown() {
		super.tearDown();
	}

	protected Host createChurnHost() {
		DefaultHost host = createEmptyHost();
		createHostProperties(host);
		host.getProperties().setEnableChurn(true);
		createNetworkWrapper(host);
		createTransLayer(host);
		return host;
	}

	protected Host createWithoutChurnHost() {
		DefaultHost host = createEmptyHost();
		createHostProperties(host);
		host.getProperties().setEnableChurn(false);
		createNetworkWrapper(host);
		createTransLayer(host);
		return host;
	}

	protected class TestBuilder implements HostBuilder {

		List<Host> allHosts;

		public TestBuilder(List<Host> hosts) {
			this.allHosts = hosts;
		}

		public List<Host> getAllHosts() {
			return this.allHosts;
		}

		public Map<String, List<Host>> getAllHostsWithGroupIDs() {
			return null;
		}

		public List<Host> getHosts(String groupId) {
			return null;
		}

		public void parse(Element elem, Configurator config) {
		}

	}
}
