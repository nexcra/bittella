package de.tud.kom.p2psim.impl.application.filesharing;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.tud.kom.p2psim.api.overlay.DistributionStrategy;
import de.tud.kom.p2psim.api.overlay.OverlayKey;
import de.tud.kom.p2psim.api.storage.ContentStorage;
import de.tud.kom.p2psim.api.storage.Document;
import de.tud.kom.p2psim.impl.application.filesharing.FileSharingClient;
import de.tud.kom.p2psim.impl.application.filesharing.Torrent;
import de.tud.kom.p2psim.impl.common.DefaultHost;
import de.tud.kom.p2psim.impl.overlay.cd.DocumentImpl;
import de.tud.kom.p2psim.impl.overlay.dht.centralized.ClientNode;
import de.tud.kom.p2psim.impl.overlay.dht.centralized.ClientServerFactory;
import de.tud.kom.p2psim.impl.overlay.dht.centralized.OverlayKeyImpl;
import de.tud.kom.p2psim.impl.overlay.dht.centralized.ServerNode;
import de.tud.kom.p2psim.impl.util.logging.SimLogger;
import de.tud.kom.p2psim.util.ComponentTest;

/**
 * Test of a file sharing application.
 * 
 * @author Konstantin Pussep
 * 
 */
public class NapsterTest extends ComponentTest {
	private static final Logger log = SimLogger.getLogger(NapsterTest.class);

	ServerNode server;

	private ClientServerFactory factory = new ClientServerFactory();

	private FileSharingClient client1, client2;

	Document doc1 = new DocumentImpl("first_doc");

	Document doc2 = new DocumentImpl("second_doc");

	Document doc3 = new DocumentImpl("third_doc");

	@Before
	public void setUp() {
		super.setUp();

		server = createServer();

		client1 = createAppl();
		client2 = createAppl();
	}

	@Test
	public void testStoreAndPublish() {
		// bootstrapping of the server
		server.join(getOperationCallback());

		assertTrue(server.listIndex().isEmpty());

		client1.storeDocument(doc1, getOperationCallback());
		assertTrue(server.listIndex().isEmpty());

		runSimulation(this.seconds(1000));
		OverlayKey key1 = doc1.getKey();

		// check post-conditions
		assertTrue(server.listIndex().containsKey(key1));
		assertTrue(client1.getStorage().listDocuments().contains(doc1));

	}

	/**
	 * Test download of published document.
	 * 
	 */
	@Test
	public void testDownloadExisting() {
		// bootstrapping of the server
		server.join(getOperationCallback());

		// prepare a document at client 2
		ContentStorage client2Storage = client2.getHost().getStorage();
		client2Storage.storeDocument(doc1);
		assertTrue(client2.getStorage().listDocuments().contains(doc1));

		// prepare torrent
		DistributionStrategy cs2 = client2.getDistributionStrategy();
		Torrent torrent = new Torrent(doc1.getKey(), cs2.getOverlayID(), cs2.getTransInfo());
		server.updateIndex(doc1.getKey(), torrent);
		assertTrue(server.listIndex().containsKey(doc1.getKey()));

		// pre-conditions
		assertTrue(client1.getStorage().listDocuments().isEmpty());

		// try to download from client 2
		int downloadId = client1.downloadDocument(doc1.getKey(), getOperationCallback());

		runSimulation(seconds(1000));

		// check post-conditions

		// still inside
		assertTrue(client2.getStorage().listDocuments().contains(doc1));

		// received
		assertTrue(client1.getStorage().listDocuments().contains(doc1));

		Collection<Document> expected = Arrays.asList(new Document[] { doc1 });
		assertEquals(expected, new LinkedList<Document>(client1.getStorage().listDocuments()));

		assertEquals(doc1, results.get(downloadId));
		assertTrue(failedOperations.isEmpty());
	}

	/**
	 * Test the overlay key implementation used, hashCode() and equals()
	 * integrity with the specification.
	 */
	@Test
	public void testOverlayKeyImpl() {
		OverlayKey key1 = new OverlayKeyImpl("test");
		OverlayKey key2 = new OverlayKeyImpl("test");
		assertEquals(key1, key2);
		assertEquals(key1.hashCode(), key2.hashCode());
	}

	private FileSharingClient createAppl() {
		DefaultHost host = new DefaultHost();
		createHostProperties(host);
		createNetworkWrapper(host);
		createTransLayer(host);
		DistributionStrategy cd = createDistributionStrategy(host);

		createContentStorage(host);

		factory.setIsServer(false);
		ClientNode client = (ClientNode) factory.createComponent(host);
		host.setOverlayNode(client);

		FileSharingClient app = new FileSharingClient(client, cd);
		host.setApplication(app);
		assertNotNull(app.getHost());
		return app;
	}

	private ServerNode createServer() {
		DefaultHost host = new DefaultHost();
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
