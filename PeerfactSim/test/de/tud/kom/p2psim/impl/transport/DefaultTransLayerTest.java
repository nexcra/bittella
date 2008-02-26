package de.tud.kom.p2psim.impl.transport;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.tud.kom.p2psim.api.common.Message;
import de.tud.kom.p2psim.api.network.NetLayer;
import de.tud.kom.p2psim.api.simengine.SimulationEvent;
import de.tud.kom.p2psim.api.simengine.SimulationEventHandler;
import de.tud.kom.p2psim.api.transport.TransInfo;
import de.tud.kom.p2psim.api.transport.TransLayer;
import de.tud.kom.p2psim.api.transport.TransMessageCallback;
import de.tud.kom.p2psim.api.transport.TransMessageListener;
import de.tud.kom.p2psim.api.transport.TransMsgEvent;
import de.tud.kom.p2psim.api.transport.TransProtocol;
import de.tud.kom.p2psim.impl.common.DefaultHost;
import de.tud.kom.p2psim.impl.util.logging.SimLogger;
import de.tud.kom.p2psim.util.ComponentTest;

/**
 * Test DefaultTransLayer implementation.
 * 
 * @author Konstantin Pussep
 * @author Sebastian Kaune
 * @version 3.0, 30.11.2007
 * 
 */
public class DefaultTransLayerTest extends ComponentTest {
	protected final static Logger log = SimLogger.getLogger(DefaultTransLayerTest.class);

	List<Message> requests = new LinkedList<Message>();

	List<Message> replies = new LinkedList<Message>();

	TransLayer trans1, trans2, trans3;

	final static short port = 100;

	DummyMessage reqMsg;

	DummyMessage replyMsg;

	DummyMessage msg1, msg2, msg3, msg4, msg5;

	class DummyMessage implements Message {
		String value;

		DummyMessage(String value) {
			this.value = value;
		}

		public Message getPayload() {
			return null;
		}

		public long getSize() {
			return value.length();
		}

		@Override
		public String toString() {
			return "<" + value + ">";
		}
	}

	// private SimpleNetworkFactory netFactory;

	@Before
	public void setUp() {
		super.setUp();
		trans1 = createHostWithNetWrapperAndTransLayer();
		trans2 = createHostWithNetWrapperAndTransLayer();
		trans3 = createHostWithNetWrapperAndTransLayer();
		reqMsg = new DummyMessage("Request");
		replyMsg = new DummyMessage("Reply");
	}

	TransLayer createHostWithNetWrapperAndTransLayer() {
		DefaultHost host = createEmptyHost();
		createHostProperties(host);
		NetLayer net = createNetworkWrapper(host);
		TransLayer trans = new DefaultTransLayer(net);
		host.setTransport(trans);
		trans.addTransMsgListener(getTransListener(), port);
		return trans;
	}

	protected TransMessageListener getTransListener() {
		return new TransMessageListener() {

			public void messageArrived(TransMsgEvent receivingEvent) {
				DefaultTransLayerTest.log.debug("Received " + receivingEvent.getPayload() + " from " + receivingEvent.getSenderTransInfo());
				Assert.assertTrue(receivingEvent.getPayload() instanceof DummyMessage);
				requests.add(receivingEvent.getPayload());
				trans2.sendReply(replyMsg, receivingEvent, port, TransProtocol.UDP);
			}

		};
	}

	protected TransMessageCallback getTransCallback() {
		return new TransMessageCallback() {

			public void messageTimeoutOccured(int commId) {
				fail("Unexpected message timeout");
			}

			public void receive(Message msg, TransInfo senderAddr, int commId) {
				replies.add(msg);
			}

		};
	}

	/**
	 * Send one message which should arrive.
	 * 
	 */
	@Test
	public void testSendSingleMsg() {

		trans1.send(reqMsg, trans2.getLocalTransInfo(port), port, TransProtocol.UDP);

		runSimulation(milliseconds(100));

		assertEquals(1, requests.size());
		assertTrue(requests.contains(reqMsg));
	}

	/**
	 * Send one message which should arrive.
	 * 
	 */
	@Test
	public void testSendToItself() {
		trans1.send(reqMsg, trans1.getLocalTransInfo(port), port, TransProtocol.UDP);

		runSimulation(milliseconds(100));

		assertEquals(1, requests.size());
		assertTrue(requests.contains(reqMsg));
	}

	/**
	 * Send request and reply and expect them to arrive. (it's poetry, isn't
	 * it?).
	 * 
	 */
	@Test
	public void testRequestReply() {

		TransInfo adr1 = trans1.getLocalTransInfo(port);
		TransInfo adr2 = trans2.getLocalTransInfo(port);

		int commID = trans1.sendAndWait(reqMsg, adr2, port, TransProtocol.UDP, getTransCallback(), milliseconds(100));

		runSimulation(milliseconds(100));

		assertEquals(1, requests.size());
		assertTrue(requests.contains(reqMsg));
		assertEquals(1, replies.size());
		assertTrue(replies.contains(replyMsg));

	}


	@After
	public void tearDown() {
		super.tearDown();
		requests.clear();
	}

}
