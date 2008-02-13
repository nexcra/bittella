package de.tud.kom.p2psim.impl.overlay.dht.centralized;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.tud.kom.p2psim.api.overlay.OverlayKey;
import de.tud.kom.p2psim.impl.common.DefaultHost;
import de.tud.kom.p2psim.impl.overlay.dht.centralized.messages.StoreResultMsg;
import de.tud.kom.p2psim.impl.overlay.dht.centralized.operations.LookupOperation;
import de.tud.kom.p2psim.impl.util.logging.SimLogger;
import de.tud.kom.p2psim.util.ComponentTest;

/**
 * The concrete subclass should select an implementation of the email system
 * through decision which <code>Server</code> and <code>Client</code> to
 * use.
 * 
 * @author Konstantin Pussep
 * 
 */
public class CentralizedTest extends ComponentTest {
	private static final Logger log = SimLogger.getLogger(CentralizedTest.class);

	ClientNode client1, client2, client3;

	ServerNode server;

	private ClientServerFactory factory = new ClientServerFactory();

	private OverlayKey key1 = new OverlayKeyImpl("first_doc");

	private OverlayKey key2 = new OverlayKeyImpl("second_doc");

	@Before
	public void setUp() {
		super.setUp();

		this.server = createServer();
		OverlayIDImpl serverID = this.server.getServerID();
		assertNotNull(serverID);

		this.client1 = createClient(serverID);
		this.client2 = createClient(serverID);
		this.client3 = createClient(serverID);

		// pre-conditions
		assertTrue(this.server.isIndexEmpty());
	}

	/**
	 * Test join and leave of new clients
	 * 
	 * @throws Exception
	 */
	@Test
	public void testJoinClient() throws Exception {
		final int id1 = client1.join(getOperationCallback());
		assertFalse(processedOpIds.contains(id1));

		runSimulation(milliseconds(1000));

		assertTrue(processedOpIds.contains(id1));
	}

	/**
	 * Test client's leave operation.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testLeaveClient() throws Exception {

		final int id2 = client1.leave(getOperationCallback());
		assertFalse(processedOpIds.contains(id2));

		runSimulation(milliseconds(1000));

		assertTrue(processedOpIds.contains(id2));
	}

	/**
	 * Test store while the server is online.
	 * 
	 */
	@Test
	public void testStore() {
		SimpleDHTObject value1 = new SimpleDHTObject(key1, null);
		server.join(getOperationCallback());
		int id1 = this.client1.store(key1, value1, getOperationCallback());

		runSimulation(seconds(1000));

		assertEquals(value1, this.server.getDHTObject(key1));
		assertEquals(StoreResultMsg.STORE_SUCCEEDED, results.get(id1));
		assertTrue(failedOperations.isEmpty());
	}

	/**
	 * Test store operation without server being online - must fail.
	 * 
	 */
	@Test
	public void testStoreWithoutServer() {
		SimpleDHTObject value1 = new SimpleDHTObject(key1, null);// client1.getAddress()
		int id1 = this.client1.store(key1, value1, getOperationCallback());

		runSimulation(seconds(1000));
		assertTrue(processedOpIds.contains(id1));
		assertTrue(failedOperations.contains(id1));
		assertFalse(results.containsKey(id1));
	}

	/**
	 * Check lookup of stored values.
	 * 
	 */
	@Test
	public void testLookupExisting() {
		// pre-conditions
		assertTrue(server.isIndexEmpty());
		SimpleDHTObject value1 = new SimpleDHTObject(key2, null);
		server.join(getOperationCallback());
		server.updateIndex(key2, value1);
		assertTrue(server.containsIndexKey(key2));

		int id1 = client3.valueLookup(key2, getOperationCallback());

		runSimulation(seconds(1000));

		// check post-conditions
		assertEquals(value1, results.get(id1));
		assertTrue(failedOperations.isEmpty());
	}

	/**
	 * Test lookup for a non-existing value.
	 * 
	 */
	@Test
	public void testLookupMissing() {
		// pre-conditions
		assertTrue(server.isIndexEmpty());

		server.join(getOperationCallback());
		
		client3.valueLookup(key2, getOperationCallback());

		runSimulation(seconds(1000));

		// check post-conditions
		assertEquals(null, results.get(0));
		assertTrue(failedOperations.isEmpty());
	}

	/**
	 * Test sending of several store messages from several clients. Stored
	 * objects can overwrite each other.
	 * 
	 */
	@Test
	public void testManyStores() {

		SimpleDHTObject value1 = new SimpleDHTObject(key1, null);
		SimpleDHTObject value2 = new SimpleDHTObject(key2, null);
		SimpleDHTObject value3 = new SimpleDHTObject(key2, null);
		assertNotNull(value1.getKey());

		server.join(getOperationCallback());

		// perform stores
		client1.store(value1.getKey(), value1, getOperationCallback());
		client2.store(value2.getKey(), value2, getOperationCallback());
		client3.store(value3.getKey(), value3, getOperationCallback());

		runSimulation(seconds(1000));

		Map<OverlayKey, SimpleDHTObject> expectedIndex = new HashMap<OverlayKey, SimpleDHTObject>();
		expectedIndex.put(key1, value1);
		expectedIndex.put(key2, value3); // value3 will overwrite value2

		// check index
		assertEquals(expectedIndex.keySet(), this.server.listIndex().keySet());
		log.debug("Server stores " + this.server.listIndex());
	}

	/**
	 * Test sending and fetching of a single message.
	 * 
	 */
	@Test
	public void testStoreAndLookup() {
		SimpleDHTObject value1 = new SimpleDHTObject(key1, null);

		server.join(getOperationCallback());

		int idStore = this.client1.store(key1, value1, getOperationCallback());

		// now send lookup and continue
		LookupOperation lookup = new LookupOperation(client2, null, key1, getOperationCallback());
		lookup.scheduleAtTime(seconds(100));

		runSimulation(seconds(1000));

		// check store
		assertEquals(StoreResultMsg.STORE_SUCCEEDED, results.get(idStore));
		assertTrue(failedOperations.isEmpty());

		// check lookup result
		assertEquals(value1, results.get(lookup.getOperationID()));
		assertTrue(failedOperations.isEmpty());
	}

	private ClientNode createClient(OverlayIDImpl serverID) {
		DefaultHost host = createEmptyHost();
		createHostProperties(host);
		createNetworkWrapper(host);
		createTransLayer(host);
		factory.setIsServer(false);
		ClientNode client = (ClientNode) factory.createComponent(host);
		host.setOverlayNode(client);
		return client;
	}

	private ServerNode createServer() {
		DefaultHost host = createEmptyHost();
		createHostProperties(host);
		createNetworkWrapper(host);
		createTransLayer(host);
		factory.setIsServer(true);
		ServerNode serverNode = (ServerNode) factory.createComponent(host);
		host.setOverlayNode(serverNode);
		return serverNode;
	}

	@After
	public void tearDown() {
		super.tearDown();
	}

}
