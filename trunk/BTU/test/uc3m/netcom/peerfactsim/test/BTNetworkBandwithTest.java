package uc3m.netcom.peerfactsim.test;

import de.tud.kom.p2psim.api.common.ConnectivityEvent;
import de.tud.kom.p2psim.api.common.Message;
import de.tud.kom.p2psim.api.common.Operation;
import de.tud.kom.p2psim.api.common.OperationCallback;
import de.tud.kom.p2psim.api.common.SupportOperations;
import de.tud.kom.p2psim.api.network.NetLayer;
import de.tud.kom.p2psim.api.overlay.OverlayID;
import de.tud.kom.p2psim.api.transport.TransInfo;
import de.tud.kom.p2psim.api.transport.TransLayer;
import de.tud.kom.p2psim.api.transport.TransMessageListener;
import de.tud.kom.p2psim.api.transport.TransMsgEvent;
import de.tud.kom.p2psim.api.transport.TransProtocol;
import de.tud.kom.p2psim.impl.common.AbstractOperation;
import de.tud.kom.p2psim.impl.common.DefaultHost;
import de.tud.kom.p2psim.impl.common.DefaultHostProperties;
import de.tud.kom.p2psim.impl.common.Operations;
import de.tud.kom.p2psim.impl.network.simple.SimpleNetFactory;
import de.tud.kom.p2psim.impl.network.simple.SimpleStaticLatencyModel;
import de.tud.kom.p2psim.impl.overlay.AbstractOverlayMessage;
import de.tud.kom.p2psim.impl.overlay.AbstractOverlayNode;
import de.tud.kom.p2psim.impl.overlay.DefaultOverlayID;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.simengine.SimulatorTest;
import de.tud.kom.p2psim.impl.transport.DefaultTransLayer;

public class BTNetworkBandwithTest extends SimulatorTest {
	
	public class DummyNode extends AbstractOverlayNode implements TransMessageListener {
		
		protected DummyNode(OverlayID peerId, short port) {
			super(peerId, port);
		}
		
		@Override
		public TransLayer getTransLayer() {
			return this.getHost().getTransLayer();
		}
		
		public void messageArrived(TransMsgEvent msg) {
			((DummyMessage)msg.getPayload()).received();
		}
		
		public void sendTestMessage(long delay, DummyMessage message, TransInfo address) {
			(new DummyOperation<DummyNode>(message, address, this)).scheduleWithDelay(delay);
		}
		
		public void connect() {
			this.getTransLayer().addTransMsgListener(this, this.getPort());
		}

		@SuppressWarnings("unchecked")
		public Operation createOperation(String opName, String[] params, OperationCallback caller) {
			// TODO Auto-generated method stub
			throw new RuntimeException("Method 'createOperation' in class 'DummyNode' not yet implemented!");
			//return null;
		}

		public void connectivityChanged(ConnectivityEvent ce) {
			// TODO Auto-generated method stub
			throw new RuntimeException("Method 'connectivityChanged' in class 'DummyNode' not yet implemented!");
			//
		}
		
	}
	
	public class DummyOperation<OwnerType extends SupportOperations> extends AbstractOperation<OwnerType, Void> {
		
		TransLayer itsTransLayer;
		
		DummyMessage itsMessage;
		
		TransInfo itsTransInfo;
		
		@SuppressWarnings("unchecked")
		public DummyOperation(DummyMessage theMessage, TransInfo theTransInfo, OwnerType theOwner) {
			super(theOwner, Operations.EMPTY_CALLBACK);
			this.itsTransLayer = this.getComponent().getHost().getTransLayer();
			this.itsMessage = theMessage;
			this.itsTransInfo = theTransInfo;
		}
		
		@Override
		protected void execute() {
			this.itsMessage.sendNow();
			this.itsTransLayer.send(this.itsMessage, this.itsTransInfo, this.itsTransInfo.getPort(), protocol);
		}
		
		@Override
		public Void getResult() {
			return null;
		}
		
		@Override
		protected void operationTimeoutOccured() {
			throw new RuntimeException();
		}
		
	}
	
	public class DummyMessage extends AbstractOverlayMessage<OverlayID> {
		
		public long sendTime = -12345, receiveTime = -12345;
		
		private int itsSize;
		
		protected DummyMessage(int size, OverlayID sender, OverlayID destination) {
			super(sender, destination);
			this.itsSize = size;
		}
		
		public void sendNow() {
			this.sendTime = Simulator.getCurrentTime();
		}
		
		public void received() {
			this.receiveTime = Simulator.getCurrentTime();
		}
		
		@Override
		public String toString() {
			if (this.sendTime == -12345)
				return "Has not been sendet.";
			if (this.receiveTime == -12345)
				return "Not yet received.";
			long sendDurationInTicks = this.receiveTime - this.sendTime;
			long sendDurationInSeconds = sendDurationInTicks / Simulator.SECOND_UNIT;
			long sendDurationInMilliSeconds = sendDurationInTicks / Simulator.MILLISECOND_UNIT;
			double bytesPerTick = this.itsSize / (double)sendDurationInTicks;
			double bytesPerSecond = this.itsSize / (sendDurationInTicks / (double)Simulator.SECOND_UNIT); //If we use the long value "sendDurationInSeconds" we get problems if this value is very small.
			double kibiPerSecond = (this.itsSize / 1024d) / (sendDurationInTicks / (double)Simulator.SECOND_UNIT);
			double mibiPerSecond = (this.itsSize / (1024d * 1024d)) / (sendDurationInTicks / (double)Simulator.SECOND_UNIT);
			double gibiPerSecond = (this.itsSize / (1024d * 1024d * 1024d)) / (sendDurationInTicks / (double)Simulator.SECOND_UNIT);
			String result = "Size: " + this.itsSize + "; Send: " + this.sendTime + "; Received: " + this.receiveTime + ";";
			result += "\n\tSend in " + sendDurationInTicks + " ticks";
			if (sendDurationInSeconds > 10) {
				result += " (" + sendDurationInSeconds + " seconds),";
			}
			else {
				result += " (" + sendDurationInMilliSeconds + " milliseconds),";
			}
			result += "\n\tthat are " + bytesPerTick + " bytes per tick";
			result += " (" + bytesPerSecond + " bytes per second)!";
			if (kibiPerSecond > 0.125) {
				result += "\n\t\t" + kibiPerSecond + " kibi bytes per second)!";
				if (mibiPerSecond > 0.125) {
					result += "\n\t\t" + mibiPerSecond + " mibi bytes per second)!";
					if (gibiPerSecond > 0.125) {
						result += "\n\t\t" + gibiPerSecond + " gibi bytes per second)!";
					}
				}
			}
			return result;
		}

		public Message getPayload() {
			return null;
		}

		public long getSize() {
			return this.itsSize;
		}
		
	}
	
	private SimpleNetFactory netFactory;
	
	@SuppressWarnings("unused")
	private DummyNode node1, node2, node3, node4;
	
	public static TransProtocol protocol = TransProtocol.UDP;
	
	public static int bandwith = 16 * 1024;
	
	@Override
	public void setUp() {
		super.setUp();
		this.netFactory = new SimpleNetFactory();
		this.netFactory.setLatencyModel(new SimpleStaticLatencyModel(10l));
		int idCounter = 0;
		
		
		//Create peer 1:
		this.netFactory.setUpBandwidth(bandwith);
		this.netFactory.setDownBandwidth(bandwith);
		DefaultHost host1 = new DefaultHost();
		host1.setProperties(new DefaultHostProperties());
		NetLayer net1 = this.netFactory.createComponent(host1);
		host1.setNetwork(net1);
		TransLayer transLayer1 = new DefaultTransLayer(net1);
		host1.setTransport(transLayer1);
		this.node1 = new DummyNode(new DefaultOverlayID(idCounter++), (short)1);
		host1.setOverlayNode(this.node1);
		this.node1.connect();
		
		//Create peer 2:
		this.netFactory.setUpBandwidth(bandwith);
		this.netFactory.setDownBandwidth(bandwith);
		DefaultHost host2 = new DefaultHost();
		host2.setProperties(new DefaultHostProperties());
		NetLayer net2 = this.netFactory.createComponent(host2);
		host2.setNetwork(net2);
		TransLayer transLayer2 = new DefaultTransLayer(net2);
		host2.setTransport(transLayer2);
		this.node2 = new DummyNode(new DefaultOverlayID(idCounter++), (short)1);
		host2.setOverlayNode(this.node2);
		this.node2.connect();
		
		//Create peer 3:
		this.netFactory.setUpBandwidth(bandwith);
		this.netFactory.setDownBandwidth(bandwith);
		DefaultHost host3 = new DefaultHost();
		host3.setProperties(new DefaultHostProperties());
		NetLayer net3 = this.netFactory.createComponent(host3);
		host3.setNetwork(net3);
		TransLayer transLayer3 = new DefaultTransLayer(net3);
		host3.setTransport(transLayer3);
		this.node3 = new DummyNode(new DefaultOverlayID(idCounter++), (short)1);
		host3.setOverlayNode(this.node3);
		this.node3.connect();
		
		//Create peer 4:
		this.netFactory.setUpBandwidth(bandwith);
		this.netFactory.setDownBandwidth(bandwith);
		DefaultHost host4 = new DefaultHost();
		host4.setProperties(new DefaultHostProperties());
		NetLayer net4 = this.netFactory.createComponent(host4);
		host4.setNetwork(net4);
		TransLayer transLayer4 = new DefaultTransLayer(net4);
		host4.setTransport(transLayer4);
		this.node4 = new DummyNode(new DefaultOverlayID(idCounter++), (short)1);
		host4.setOverlayNode(this.node4);
		this.node4.connect();
	}
	
	public void testBandwith() {
		/*
		 * If the bandwith is 16*1024 Kilobyte per second:
		 * To send or receive 1 MB I would need 64 Seconds.
		 * To receive the 3 messages, I would need 192 Seconds.
		 */
		
		long timeOffset = 0;
		
		TransInfo addressOfNode1 = this.node1.getTransLayer().getLocalTransInfo(this.node1.getPort());
		int messageSize = 1024 * 1024;
		
		//Nacheinander
		DummyMessage message1 = new DummyMessage(messageSize, this.node2.getOverlayID(), this.node1.getOverlayID());
		DummyMessage message2 = new DummyMessage(messageSize, this.node2.getOverlayID(), this.node1.getOverlayID());
		DummyMessage message3 = new DummyMessage(messageSize, this.node2.getOverlayID(), this.node1.getOverlayID());
		
		this.node2.sendTestMessage(0 * Simulator.MINUTE_UNIT + timeOffset, message1, addressOfNode1);
		this.node3.sendTestMessage(2 * Simulator.MINUTE_UNIT + timeOffset, message2, addressOfNode1);
		this.node4.sendTestMessage(4 * Simulator.MINUTE_UNIT + timeOffset, message3, addressOfNode1);
		
		timeOffset += Simulator.HOUR_UNIT;
		
		//Um 1 Tick verschoben:
		DummyMessage message4 = new DummyMessage(messageSize, this.node2.getOverlayID(), this.node1.getOverlayID());
		DummyMessage message5 = new DummyMessage(messageSize, this.node2.getOverlayID(), this.node1.getOverlayID());
		DummyMessage message6 = new DummyMessage(messageSize, this.node2.getOverlayID(), this.node1.getOverlayID());
		
		this.node2.sendTestMessage(0 + timeOffset, message4, addressOfNode1);
		this.node3.sendTestMessage(1 + timeOffset, message5, addressOfNode1);
		this.node4.sendTestMessage(2 + timeOffset, message6, addressOfNode1);
		
		timeOffset += Simulator.HOUR_UNIT;
		
		//Um 45 Sekunden verschoben:
		DummyMessage message7 = new DummyMessage(messageSize, this.node2.getOverlayID(), this.node1.getOverlayID());
		DummyMessage message8 = new DummyMessage(messageSize, this.node2.getOverlayID(), this.node1.getOverlayID());
		DummyMessage message9 = new DummyMessage(messageSize, this.node2.getOverlayID(), this.node1.getOverlayID());
		
		this.node2.sendTestMessage( 0 * Simulator.SECOND_UNIT + timeOffset, message7, addressOfNode1);
		this.node3.sendTestMessage(45 * Simulator.SECOND_UNIT + timeOffset, message8, addressOfNode1);
		this.node4.sendTestMessage(90 * Simulator.SECOND_UNIT + timeOffset, message9, addressOfNode1);
		
		timeOffset += Simulator.HOUR_UNIT;
		
		DummyMessage messageA = new DummyMessage(1024 * 1024 * 1024, this.node2.getOverlayID(), this.node1.getOverlayID());
		this.node2.sendTestMessage(timeOffset, messageA, addressOfNode1);
		
		timeOffset += (5 * 24 * Simulator.HOUR_UNIT);
		
		DummyMessage messageB = new DummyMessage(1024 * 1024, this.node2.getOverlayID(), this.node1.getOverlayID());
		DummyMessage messageC = new DummyMessage(        100, this.node2.getOverlayID(), this.node1.getOverlayID());
		DummyMessage messageD = new DummyMessage(1024 * 1024, this.node2.getOverlayID(), this.node1.getOverlayID());
		
		DummyMessage messageE = new DummyMessage(1024 * 1024, this.node2.getOverlayID(), this.node1.getOverlayID());
		DummyMessage messageF = new DummyMessage(          1, this.node2.getOverlayID(), this.node1.getOverlayID());
		DummyMessage messageG = new DummyMessage(1024 * 1024, this.node2.getOverlayID(), this.node1.getOverlayID());
		
		DummyMessage messageH = new DummyMessage(1024 * 1024, this.node2.getOverlayID(), this.node1.getOverlayID());
		DummyMessage messageI = new DummyMessage(          0, this.node2.getOverlayID(), this.node1.getOverlayID());
		DummyMessage messageJ = new DummyMessage(1024 * 1024, this.node2.getOverlayID(), this.node1.getOverlayID());
		
		DummyMessage messageK = new DummyMessage(1024 * 1024, this.node2.getOverlayID(), this.node1.getOverlayID());
		DummyMessage messageL = new DummyMessage(1024 * 1024, this.node2.getOverlayID(), this.node1.getOverlayID());
		DummyMessage messageM = new DummyMessage(1024 * 1024, this.node2.getOverlayID(), this.node1.getOverlayID());
		
		this.node2.sendTestMessage(0 + timeOffset, messageB, addressOfNode1);
		this.node2.sendTestMessage(1 + timeOffset, messageC, addressOfNode1);
		this.node2.sendTestMessage(2 + timeOffset, messageD, addressOfNode1);
		
		timeOffset += Simulator.HOUR_UNIT;
		
		this.node2.sendTestMessage(0 + timeOffset, messageE, addressOfNode1);
		this.node2.sendTestMessage(1 + timeOffset, messageF, addressOfNode1);
		this.node2.sendTestMessage(2 + timeOffset, messageG, addressOfNode1);
		
		timeOffset += Simulator.HOUR_UNIT;
		
		this.node2.sendTestMessage(0 + timeOffset, messageH, addressOfNode1);
		this.node2.sendTestMessage(1 + timeOffset, messageI, addressOfNode1);
		this.node2.sendTestMessage(2 + timeOffset, messageJ, addressOfNode1);
		
		timeOffset += Simulator.HOUR_UNIT;
		
		this.node2.sendTestMessage(0 + timeOffset, messageK, addressOfNode1);
		this.node2.sendTestMessage(1 + timeOffset, messageL, addressOfNode1);
		this.node2.sendTestMessage(2 + timeOffset, messageM, addressOfNode1);
		
		timeOffset += Simulator.HOUR_UNIT;
		
		
		DummyMessage messageN = new DummyMessage(1024 * 1024, this.node2.getOverlayID(), this.node1.getOverlayID());
		DummyMessage messageO = new DummyMessage(        100, this.node3.getOverlayID(), this.node1.getOverlayID());
		DummyMessage messageP = new DummyMessage(1024 * 1024, this.node4.getOverlayID(), this.node1.getOverlayID());
		
		DummyMessage messageQ = new DummyMessage(1024 * 1024, this.node2.getOverlayID(), this.node1.getOverlayID());
		DummyMessage messageR = new DummyMessage(          1, this.node3.getOverlayID(), this.node1.getOverlayID());
		DummyMessage messageS = new DummyMessage(1024 * 1024, this.node4.getOverlayID(), this.node1.getOverlayID());
		
		DummyMessage messageT = new DummyMessage(1024 * 1024, this.node2.getOverlayID(), this.node1.getOverlayID());
		DummyMessage messageU = new DummyMessage(          0, this.node3.getOverlayID(), this.node1.getOverlayID());
		DummyMessage messageV = new DummyMessage(1024 * 1024, this.node4.getOverlayID(), this.node1.getOverlayID());
		
		DummyMessage messageW = new DummyMessage(1024 * 1024, this.node2.getOverlayID(), this.node1.getOverlayID());
		DummyMessage messageX = new DummyMessage(1024 * 1024, this.node3.getOverlayID(), this.node1.getOverlayID());
		DummyMessage messageY = new DummyMessage(1024 * 1024, this.node4.getOverlayID(), this.node1.getOverlayID());
		
		
		this.node2.sendTestMessage(0 + timeOffset, messageN, addressOfNode1);
		this.node3.sendTestMessage(1 + timeOffset, messageO, addressOfNode1);
		this.node4.sendTestMessage(2 + timeOffset, messageP, addressOfNode1);
		
		timeOffset += Simulator.HOUR_UNIT;
		
		this.node2.sendTestMessage(0 + timeOffset, messageQ, addressOfNode1);
		this.node3.sendTestMessage(1 + timeOffset, messageR, addressOfNode1);
		this.node4.sendTestMessage(2 + timeOffset, messageS, addressOfNode1);
		
		timeOffset += Simulator.HOUR_UNIT;
		
		this.node2.sendTestMessage(0 + timeOffset, messageT, addressOfNode1);
		this.node3.sendTestMessage(1 + timeOffset, messageU, addressOfNode1);
		this.node4.sendTestMessage(2 + timeOffset, messageV, addressOfNode1);
		
		timeOffset += Simulator.HOUR_UNIT;
		
		this.node2.sendTestMessage(0 + timeOffset, messageW, addressOfNode1);
		this.node3.sendTestMessage(1 + timeOffset, messageX, addressOfNode1);
		this.node4.sendTestMessage(2 + timeOffset, messageY, addressOfNode1);
		
		timeOffset += Simulator.HOUR_UNIT;
		
		
		int numberOfMessages = 100;
		DummyMessage messages[] = new DummyMessage[numberOfMessages];
		for (int i = 0; i < numberOfMessages; i++) {
			messages[i] = new DummyMessage(1024 * 1024, this.node2.getOverlayID(), this.node1.getOverlayID());
			this.node2.sendTestMessage(timeOffset + i, messages[i], addressOfNode1);
		}
		
		timeOffset += numberOfMessages * Simulator.HOUR_UNIT;
		
		runSimulation(timeOffset);
		
		System.out.println("\nBandbreite in Byte pro Sekunde: " + bandwith);
		
		System.out.println("\nNacheinander mit gen�gend Abstand:");
		System.out.println(message1);
		System.out.println(message2);
		System.out.println(message3);
		
		System.out.println("\nMit einem Tick Abstand:");
		System.out.println(message4);
		System.out.println(message5);
		System.out.println(message6);
		
		System.out.println("\nUm 1 Sekunde verschoben:");
		System.out.println(message7);
		System.out.println(message8);
		System.out.println(message9);
		
		System.out.println('\n');
		System.out.println("\nEin Gigabyte:");
		System.out.println(messageA);
		
		System.out.println('\n');
		System.out.println("\nVier mal drei Nachrichten, alle drei von Node2 nach Node1:");
		System.out.println("\nEin MiByte, 100 Byte, ein MiByte:");
		System.out.println(messageB);
		System.out.println(messageC);
		System.out.println(messageD);
		
		System.out.println("\nEin MiByte, 1 Byte, ein MiByte:");
		System.out.println(messageE);
		System.out.println(messageF);
		System.out.println(messageG);
		
		System.out.println("\nEin MiByte, 0 Byte, ein MiByte:");
		System.out.println(messageH);
		System.out.println(messageI);
		System.out.println(messageJ);
		
		System.out.println("\nEin MiByte, ein MiByte, ein MiByte:");
		System.out.println(messageK);
		System.out.println(messageL);
		System.out.println(messageM);
		
		System.out.println('\n');
		System.out.println("\nVier mal drei Nachrichten, je eine von Node2, Node3 und Node4, alle zu Node1:");
		System.out.println("\nEin MiByte, 100 Byte, ein MiByte:");
		System.out.println(messageN);
		System.out.println(messageO);
		System.out.println(messageP);
		
		System.out.println("\nEin MiByte, 1 Byte, ein MiByte:");
		System.out.println(messageQ);
		System.out.println(messageR);
		System.out.println(messageS);
		
		System.out.println("\nEin MiByte, 0 Byte, ein MiByte:");
		System.out.println(messageT);
		System.out.println(messageU);
		System.out.println(messageV);
		
		System.out.println("\nEin MiByte, ein MiByte, ein MiByte:");
		System.out.println(messageW);
		System.out.println(messageX);
		System.out.println(messageY);
		
		
		System.out.println('\n');
		System.out.println("\nMehrere Nachrichten sehr kurz hintereinander (1 Tick Abstand); alle von Node2 zu Node1:");
		for (int i = 0; i < numberOfMessages; i++) {
			System.out.println(messages[i]);
		}
		
	}
	
	public static void main(String[] args) {
		BTNetworkBandwithTest aBandwithTest = new BTNetworkBandwithTest();
		aBandwithTest.setUp();
		aBandwithTest.testBandwith();
	}
	
}
