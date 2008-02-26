package de.tud.kom.p2psim.impl.transport;

import java.util.Arrays;
import java.util.HashMap;
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
import de.tud.kom.p2psim.api.network.NetMessageListener;
import de.tud.kom.p2psim.api.network.NetMsgEvent;
import de.tud.kom.p2psim.api.network.NetProtocol;
import de.tud.kom.p2psim.api.simengine.SimulationEvent;
import de.tud.kom.p2psim.api.simengine.SimulationEventHandler;
import de.tud.kom.p2psim.api.transport.TransInfo;
import de.tud.kom.p2psim.api.transport.TransLayer;
import de.tud.kom.p2psim.api.transport.TransMessageCallback;
import de.tud.kom.p2psim.api.transport.TransMessageListener;
import de.tud.kom.p2psim.api.transport.TransMsgEvent;
import de.tud.kom.p2psim.api.transport.TransProtocol;
import de.tud.kom.p2psim.impl.common.DefaultHost;
import de.tud.kom.p2psim.impl.network.gnp.AbstractGnpNetBandwidthManager;
import de.tud.kom.p2psim.impl.network.gnp.GnpLatencyModel;
import de.tud.kom.p2psim.impl.network.gnp.GnpNetBandwidthManagerPeriodical;
import de.tud.kom.p2psim.impl.network.gnp.GnpNetLayer;
import de.tud.kom.p2psim.impl.network.gnp.GnpSubnet;
import de.tud.kom.p2psim.impl.network.gnp.IPv4Message;
import de.tud.kom.p2psim.impl.network.gnp.IPv4NetID;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.transport.DefaultTransLayerTest.DummyMessage;
import de.tud.kom.p2psim.impl.util.logging.SimLogger;
import de.tud.kom.p2psim.util.ComponentTest;

/**
 *
 * 
 * @author Gerald Klunker
 * @version 0.1, 19.12.2007
 * 
 */
public class DefaultTransLayerInOrderDeliveryTest extends ComponentTest {
	protected final static Logger log = SimLogger.getLogger(DefaultTransLayerInOrderDeliveryTest.class);

	GnpNetLayer s1, r1;

	short port = 100;
	
	HashMap<Message, Long> receivedTime;
	
	GnpSubnet subnet;
	
	TestLatencyModel latencyModel;
	AbstractGnpNetBandwidthManager bandwidthManager;
	

	TransLayer tlSender, tlReceiver;
	DummyMessage msg1, msg2, msg3;

	

	// private SimpleNetworkFactory netFactory;

	@Override
	@Before
	public void setUp() {
		super.setUp();

		receivedTime = new HashMap<Message, Long>();
		
		subnet = new GnpSubnet();
		
		latencyModel = new TestLatencyModel();
		bandwidthManager = new GnpNetBandwidthManagerPeriodical();
		
		subnet.setBandwidthManager(bandwidthManager);
		subnet.setLatencyModel(latencyModel);
		subnet.setPbaPeriod(300 * Simulator.MILLISECOND_UNIT);
		
		s1 = new GnpNetLayer(subnet, new IPv4NetID(1L), null, null, 1000, 100, "--");
		r1 = new GnpNetLayer(subnet, new IPv4NetID(10L), null, null, 1000, 100, "--");

		tlSender = new DefaultTransLayer(s1);
		tlReceiver = new DefaultTransLayer(r1);

		tlReceiver.addTransMsgListener(getTransListener(), port);
		
		
	}

	
	/**
	 * UDP Message without IP Fragmentation => one IP - Packet (size < MTU.size)
	 * Delivery: msg1 <= msg2 <= msg3
	 * Different Size, but Packets will be send one after another
	 */
	@Test
	public void testDelivery_SinglePackets() {
		msg1 = new DummyMessage(300);
		msg2 = new DummyMessage(100);
		msg3 = new DummyMessage(200);		
		assertTrue(receivedTime.isEmpty());
		tlSender.send(msg1, tlReceiver.getLocalTransInfo(port), port, TransProtocol.UDP);
		tlSender.send(msg2, tlReceiver.getLocalTransInfo(port), port, TransProtocol.UDP);
		tlSender.send(msg3, tlReceiver.getLocalTransInfo(port), port, TransProtocol.UDP);
		runSimulation(10 * Simulator.MINUTE_UNIT);
		assertTrue(receivedTime.get(msg1) < receivedTime.get(msg2));
		assertTrue(receivedTime.get(msg2) < receivedTime.get(msg3));
	}
	
	
	/**
	 * UDP Message with IP Fragmentation => multiple IP - Packets per Message (size > MTU.size)
	 * Delivery: msg2 <= msg3 <= msg1
	 * Smaller Messages will delivered first
	 */
	@Test
	public void testDelivery_UDPStream() {
		msg1 = new DummyMessage(30000);
		msg2 = new DummyMessage(10000);
		msg3 = new DummyMessage(20000);		
		assertTrue(receivedTime.isEmpty());
		tlSender.send(msg1, tlReceiver.getLocalTransInfo(port), port, TransProtocol.UDP);
		tlSender.send(msg2, tlReceiver.getLocalTransInfo(port), port, TransProtocol.UDP);
		tlSender.send(msg3, tlReceiver.getLocalTransInfo(port), port, TransProtocol.UDP);
		runSimulation(100 * Simulator.MINUTE_UNIT);
		assertTrue(receivedTime.get(msg2) < receivedTime.get(msg3));
		assertTrue(receivedTime.get(msg3) < receivedTime.get(msg1));
	}

	/**
	 * TCP Stream with => multiple IP - Packets per Message (size > MTU.size)
	 * Delivery: msg1 <= msg2 <= msg2
	 * Reliable in-order delivery
	 */
	@Test
	public void testDelivery_TCPStream() {
		msg1 = new DummyMessage(30000);
		msg2 = new DummyMessage(10000);
		msg3 = new DummyMessage(20000);		
		assertTrue(receivedTime.isEmpty());
		tlSender.send(msg1, tlReceiver.getLocalTransInfo(port), port, TransProtocol.TCP);
		tlSender.send(msg2, tlReceiver.getLocalTransInfo(port), port, TransProtocol.TCP);
		tlSender.send(msg3, tlReceiver.getLocalTransInfo(port), port, TransProtocol.TCP);
		runSimulation(100 * Simulator.MINUTE_UNIT);
		assertTrue(receivedTime.get(msg1) <= receivedTime.get(msg2));
		assertTrue(receivedTime.get(msg2) <= receivedTime.get(msg3));
	}
	
	
	
	protected TransMessageListener getTransListener() {
		return new TransMessageListener() {
			public void messageArrived(TransMsgEvent receivingEvent) {
				Assert.assertTrue(receivingEvent.getPayload() instanceof DummyMessage);
				receivedTime.put(receivingEvent.getPayload(), Simulator.getCurrentTime());
			}
		};
	}
	
	
	class TestLatencyModel extends GnpLatencyModel {
		
		@Override
		public double getUDPerrorProbability(GnpNetLayer sender, GnpNetLayer receiver, IPv4Message msg) {
			return 0.0;
		}
		
		@Override
		public double getTcpThroughput(GnpNetLayer sender, GnpNetLayer receiver) {
			return Double.MAX_VALUE;
		}
		
		@Override
		public long getPropagationDelay(GnpNetLayer sender, GnpNetLayer receiver) {
			return 0;
		}
	}
	
	
	
	class DummyMessage implements Message {
		long size;

		DummyMessage(long size) {
			this.size = size;
		}

		public Message getPayload() {
			return null;
		}

		public long getSize() {
			return size;
		}

	}

	
	
}
