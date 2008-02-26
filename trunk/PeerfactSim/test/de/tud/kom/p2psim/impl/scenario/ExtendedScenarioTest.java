package de.tud.kom.p2psim.impl.scenario;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import de.tud.kom.p2psim.api.common.Component;
import de.tud.kom.p2psim.api.common.Host;
import de.tud.kom.p2psim.api.overlay.OverlayID;
import de.tud.kom.p2psim.impl.common.DefaultHost;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.simengine.SimulatorTest;
import de.tud.kom.p2psim.impl.util.logging.SimLogger;

/**
 * @author Konstantin Pussep
 * @version 0.1, 20.08.2007
 * 
 */

public class ExtendedScenarioTest extends SimulatorTest {
	private static final String GROUP_ID = "host";

	class OIDDummy implements OverlayID {

		private String value;

		OIDDummy(String value) {
			this.value = value;
		}

		public byte[] getBytes() {
			return value.getBytes();
		}

		public Object getUniqueValue() {
			return value;
		}

		public int compareTo(OverlayID arg0) {
			return 0;
		}

		@Override
		public String toString() {
			return "OID value =" + value;
		}

	}

	class OIDParser implements Parser {

		public Object parse(String stringValue) {
			return new OIDDummy(stringValue);
		}

		public Class getType() {
			return OverlayID.class;
		}

	}

	protected final static Logger log = SimLogger.getLogger(ExtendedScenarioTest.class);

	private ExtendedScenario scenario;

	private List<DefaultHost> hosts;

	/**
	 * Create new ExtendedScenarioTest.
	 */
	public ExtendedScenarioTest() {
		super();
	}

	@Before
	public void setUp() {
		super.setUp();
		List<Parser> parsers = new LinkedList<Parser>();
		parsers.add(new OIDParser());
		List<Class> additionalCompClasses = Arrays.asList((Class)ComponentDummy2.class);
		scenario = new ExtendedScenario(ComponentDummy.class,additionalCompClasses, parsers);

	}

	private void createHosts(int number) {
		Map<String, List<Host>> hostsMap = new HashMap<String, List<Host>>();
		hosts = new LinkedList<DefaultHost>();

		for (int i = 0; i < number; i++) {
			DefaultHost host = new DefaultHost();
			host.setComponent(new ComponentDummy());
			host.setComponent(new ComponentDummy2());
			hosts.add(host);
		}
		hostsMap.put(GROUP_ID, new LinkedList<Host>(hosts));
		scenario.setHosts(hostsMap);
	}

	/**
	 * Primitive implementation of a component.
	 */
	public class ComponentDummy implements Component {

		public Host getHost() {
			// TODO Auto-generated method stub
			return null;
		}

		public void setHost(Host host) {
			// TODO Auto-generated method stub

		}

		public void foo0() {
			log.info("method foo0 was called at " + Simulator.getCurrentTime());
		}

		public void foo1(double d) {
			log.info("method foo1 was called with param d=" + d + " at " + Simulator.getCurrentTime());
		}

		public void foo2(int i, long l) {
			log.info("method foo2 was called with params i=" + i + " and l=" + l + " at " + Simulator.getCurrentTime());
		}

		public void fooWithOID(OverlayID id) {
			log.info("method fooWithOID was called with params id=" + id + " at " + Simulator.getCurrentTime());
		}

		public void fooWithOIDandInt(OverlayID id, int i) {
			log.info("method fooWithOID was called with params id=" + id + " and i=" + i + " at " + Simulator.getCurrentTime());
		}

	}

	/**
	 * Second primitive implementation of a component.
	 */
	public class ComponentDummy2 implements Component, Comparable {

		public Host getHost() {
			// TODO Auto-generated method stub
			return null;
		}

		public void setHost(Host host) {
			// TODO Auto-generated method stub

		}

		public void foo0() {
			log.info("method foo0 was called at " + Simulator.getCurrentTime());
		}

		public int compareTo(Object arg0) {
			// unused
			return 0;
		}
	}

	/**
	 * Test creation of different actions with different number and type of
	 * parameters.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreateAction() throws Exception {

		createHosts(1);
		DefaultHost host = hosts.get(0);

		assertEquals(0, scenario.actions.size());

		// try a method without params
		scenario.createAction(host, ComponentDummy.class, seconds(5), "foo0", new String[0]);
		assertEquals(1, scenario.actions.size());

		// try a method with one simple param
		String[] params = new String[1];
		params[0] = "10.2";
		scenario.createAction(host, ComponentDummy.class, seconds(10), "foo1", params);
		assertEquals(2, scenario.actions.size());

		// try a method with two simple params
		params = new String[2];
		params[0] = "10";
		params[1] = "-10";
		scenario.createAction(host, ComponentDummy.class, seconds(12), "foo2", params);
		assertEquals(3, scenario.actions.size());

		// try a method with one complex param
		params = new String[1];
		params[0] = "someID";
		scenario.createAction(host, ComponentDummy.class, seconds(10), "fooWithOID", params);
		assertEquals(4, scenario.actions.size());

		// try a method with one complex and one simple param
		params = new String[2];
		params[0] = "someID";
		params[1] = "127";
		scenario.createAction(host, ComponentDummy.class, seconds(10), "fooWithOIDandInt", params);
		assertEquals(5, scenario.actions.size());

		// now schedule actions and run the simulation
		scenario.prepare();

		runSimulation(seconds(10000));
	}

	/**
	 * Test creating an action for a group of hosts.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreateActionForGroup() throws Exception {
		createHosts(3);

		String[] params;

		// try a method
		params = new String[0];
		scenario.createActions(GROUP_ID, "10s-20s", "foo0", params);

		assertEquals(hosts.size(), scenario.actions.size());

		// now schedule actions and run the simulation
		scenario.prepare();

		runSimulation(seconds(10000));
	}

	/**
	 * Test creating an action for a non-default component.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testCreateActionForNotDefaultComponent() throws Exception {
		createHosts(1);

		String[] params;

		assertEquals(0, scenario.actions.size());

		// try a method
		params = new String[0];
		scenario.createActions(GROUP_ID, "10s", "Comparable:foo0", params);

		assertEquals(hosts.size(), scenario.actions.size());

		// now schedule actions and run the simulation
		scenario.prepare();

		runSimulation(seconds(10000));
	}

	/**
	 * Test one configuration file for consistency and format correctness.
	 * 
	 */
	@Test
	public void testTimeInterval() {
		long start = 10;
		int size = 20;
		long delta = 5;
		List<Long> expected = new ArrayList<Long>(size);
		for (int i = 0; i < size; i++) {
			expected.add(start + i * delta);
		}
		String range = expected.get(0).toString() + "-" + expected.get(expected.size() - 1).toString();
		long[] times = scenario.createTimePoints(expected.size(), range);

		List<Long> got = new ArrayList<Long>(size);
		for (int i = 0; i < times.length; i++) {
			got.add(times[i]);
		}
		Assert.assertEquals(expected, got);

	}
}
