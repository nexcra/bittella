package de.tud.kom.p2psim.impl.overlay.cd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import de.tud.kom.p2psim.api.overlay.OverlayKey;
import de.tud.kom.p2psim.api.storage.ContentStorage;
import de.tud.kom.p2psim.api.storage.Document;
import de.tud.kom.p2psim.api.transport.TransInfo;
import de.tud.kom.p2psim.impl.common.DefaultHost;
import de.tud.kom.p2psim.impl.overlay.dht.centralized.CentralizedTest;
import de.tud.kom.p2psim.impl.util.logging.SimLogger;
import de.tud.kom.p2psim.util.ComponentTest;

/**
 * Test of the simple DistributionStrategy implementation.
 * 
 * @author Konstantin Pussep
 * 
 */
public class ContentDistributionTest extends ComponentTest {
	private static final Logger log = SimLogger.getLogger(CentralizedTest.class);

	private DistributionStrategyImpl cd1, cd2;

	Document doc1 = new DocumentImpl("first_doc");

	Document doc2 = new DocumentImpl("second_doc");

	Document doc3 = new DocumentImpl("third_doc");

	private static final short listenAtPort = 200;

	@Before
	public void setUp() {
		super.setUp();
		cd1 = createCD();
		cd2 = createCD();
	}

	private DistributionStrategyImpl createCD() {
		DefaultHost host = createEmptyHost();
		createHostProperties(host);
		createContentStorage(host);
		createNetworkWrapper(host);
		createTransLayer(host);
		DistributionStrategyImpl cd = new DistributionStrategyImpl(host.getTransLayer(), listenAtPort);
		host.setOverlayNode(cd);
		return cd;
	}

	/**
	 * Test the download of some content.
	 */
	@Test
	public void testDownloadExisting() {
		OverlayKey key1 = doc1.getKey();

		Document[] docs = new Document[] { doc1 };
		ContentStorage cs1 = cd1.getStorage();
		ContentStorage cs2 = cd2.getStorage();

		// pre-conditions: storages are empty
		assertTrue(cs1.listDocumentKeys().isEmpty());
		assertTrue(cs2.listDocumentKeys().isEmpty());

		cs1.storeDocument(doc1);

		// now cs1's storage contains doc1, cs2's storage is still empty
		assertTrue(cs1.containsDocument(key1));
		assertEquals(1, cs1.listDocuments().size());
		assertTrue(cs2.listDocumentKeys().isEmpty());

		// now cd2 tries to download from cd1
		List<TransInfo> peers = Collections.singletonList(cd1.getTransInfo());
		assertNotNull(peers.get(0));
		cd2.downloadDocument(key1, peers, getOperationCallback());

		runSimulation(milliseconds(10000));

		// check post-conditions: both contains the document
		assertTrue(cs1.containsDocument(key1));
		assertEquals(Arrays.asList(docs), new LinkedList<Document>(cs2.listDocuments()));
		assertEquals(Arrays.asList(docs), new LinkedList<Document>(cs1.listDocuments()));

		assertEquals(cs1.listDocuments().iterator().next(), cs2.listDocuments().iterator().next());
		assertEquals(cs1.listDocuments().size(), cs2.listDocuments().size());
		assertEquals(asList(cs1.listDocuments()), asList(cs2.listDocuments()));

		// and what has received the application ... doc1 too?
		assertEquals(Arrays.asList(docs), asList(results.values()));
		assertTrue(failedOperations.isEmpty());
	}

	private Object asList(Collection c) {
		return new ArrayList(c);
	}
	

}
