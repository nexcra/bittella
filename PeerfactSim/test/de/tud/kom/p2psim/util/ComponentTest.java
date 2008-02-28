package de.tud.kom.p2psim.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.After;

import de.tud.kom.p2psim.api.common.ComponentFactory;
import de.tud.kom.p2psim.api.common.Host;
import de.tud.kom.p2psim.api.common.Operation;
import de.tud.kom.p2psim.api.common.OperationCallback;
import de.tud.kom.p2psim.api.network.NetLayer;
import de.tud.kom.p2psim.api.overlay.DistributionStrategy;
import de.tud.kom.p2psim.api.transport.TransLayer;
import de.tud.kom.p2psim.impl.common.DefaultHost;
import de.tud.kom.p2psim.impl.common.DefaultHostProperties;
import de.tud.kom.p2psim.impl.network.simple.SimpleNetFactory;
import de.tud.kom.p2psim.impl.network.simple.SimpleStaticLatencyModel;
import de.tud.kom.p2psim.impl.overlay.cd.CDFactory;
import de.tud.kom.p2psim.impl.simengine.SimulatorTest;
import de.tud.kom.p2psim.impl.storage.DefaultContentStorage;
import de.tud.kom.p2psim.impl.transport.DefaultTransLayer;

/**
 * Class with a lot of usefull methods.
 * 
 * @author pussep
 * @version 3.0, 29.11.2007
 * 
 */
public abstract class ComponentTest extends SimulatorTest {

	private ComponentFactory netFactory;

	/**
	 * Ids of failed operations with error descriptions
	 */
	protected List<Integer> failedOperations;

	/**
	 * Ids of successful operation with results.
	 */
	protected Map<Integer, Object> results;

	protected List<Integer> processedOpIds;

	private ComponentFactory dsFactory;

	protected NetLayer createNetworkWrapper(Host host) {
		if (netFactory == null) {
			this.netFactory = new SimpleNetFactory();
			((SimpleNetFactory) this.netFactory).setLatencyModel(new SimpleStaticLatencyModel(10l));
		}
		NetLayer wrapper = (NetLayer) netFactory.createComponent(host);
		((DefaultHost) host).setNetwork(wrapper);
		return wrapper;
	}

	protected TransLayer createTransLayer(DefaultHost host) {
		TransLayer transLayer = new DefaultTransLayer(host.getNetLayer());
		host.setTransport(transLayer);
		return transLayer;
	}

	protected OperationCallback getOperationCallback() {
		return new OperationCallback() {
			public void calledOperationSucceeded(Operation op) {
				log.debug("operation finished with success " + op.getOperationID());
				processedOpIds.add(op.getOperationID());
				results.put(op.getOperationID(), op.getResult());
			}

			public void calledOperationFailed(Operation op) {
				log.debug("operation finished with failure " + op.getOperationID());
				processedOpIds.add(op.getOperationID());
				failedOperations.add(op.getOperationID());
			}

			@Override
			public String toString() {
				return "Operation Callback TestStub";
			}
		};
	}

	public ComponentTest(){
		failedOperations = new LinkedList<Integer>();
		results = new HashMap<Integer, Object>();
		processedOpIds = new LinkedList<Integer>();
	}
	
	@Override
	public void setUp() {
		super.setUp();
	}

	protected DefaultHost createEmptyHost() {
		DefaultHost host = new DefaultHost();
		host.setProperties(new DefaultHostProperties());
		return host;
	}

	protected void createHostProperties(DefaultHost host) {
		DefaultHostProperties hostProperties = new DefaultHostProperties();
		host.setProperties(hostProperties);
	}

	protected void createContentStorage(DefaultHost host) {
		host.setContentStorage(new DefaultContentStorage());
	}

	protected DistributionStrategy createDistributionStrategy(Host host) {
		if (dsFactory == null) {
			dsFactory = new CDFactory();
		}
		DistributionStrategy ds = (DistributionStrategy) dsFactory.createComponent(host);
		((DefaultHost) host).setOverlayNode(ds);
		return ds;
	}

	@After
	public void tearDown() {
		super.tearDown();
		failedOperations.clear();
		results.clear();
		processedOpIds.clear();
	}

}
