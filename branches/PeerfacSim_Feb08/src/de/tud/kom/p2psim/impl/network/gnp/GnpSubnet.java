package de.tud.kom.p2psim.impl.network.gnp;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import de.tud.kom.p2psim.api.network.NetLayer;
import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.api.simengine.SimulationEvent;
import de.tud.kom.p2psim.api.simengine.SimulationEventHandler;
import de.tud.kom.p2psim.impl.network.AbstractSubnet;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.util.logging.SimLogger;

/**
 * 
 * @author Sebastian Kaune
 */
public class GnpSubnet extends AbstractSubnet implements SimulationEventHandler {

	private static Logger log = SimLogger.getLogger(GnpSubnet.class);

	/**
	 * The NetLatencyModel for the delivery of a NetworkMessage within the
	 * SubNet.
	 */
	private GnpLatencyModel netLatencyModel;

	/**
	 * Datastructure, that manages all included NetworkWrappers.
	 */
	private Map<IPv4NetID, GnpNetLayer> wrappers;

	/**
	 * Initialization of the used datastructures. (This constructor can never be
	 * call from outside. It ovverrides the dynamically generated public
	 * constructor.)
	 */
	public GnpSubnet() {
		this.wrappers = new HashMap<IPv4NetID, GnpNetLayer>();
		this.netLatencyModel = new GnpLatencyModel();
	}

	/**
	 * Enables the manual setting of a NetLatencyModel.
	 * 
	 * @param netLatencyModel
	 *            The NetLatencyModel.
	 */
	public void setLatencyModel(GnpLatencyModel netLatencyModel) {
		this.netLatencyModel = netLatencyModel;
	}

	/**
	 * Registers a NetWrapper in the SubNet.
	 * 
	 * @param wrapper
	 *            The NetWrapper.
	 */
	@Override
	public void registerNetLayer(NetLayer netLayer) {
		this.wrappers.put((IPv4NetID) netLayer.getNetID(), (GnpNetLayer) netLayer);
	}

	/**
	 * Sends a NetworkMessage by using the UDP-ServiceCategory.
	 * 
	 * @param msg
	 *            The NetworkMessage that should be send.
	 */
	private void sendUDP(IPv4Message msg) {
		GnpNetLayer sender = this.wrappers.get(msg.getSender());
		GnpNetLayer receiver = this.wrappers.get(msg.getReceiver());
		// UDP is unreliable, no in-order-delivery garanteed
		double packetLossProb = this.netLatencyModel.getErrorProbability(msg.getSize(), sender, receiver);

		if (Simulator.getRandom().nextDouble() > packetLossProb) {

			// calculation of the simulation time
			long currentTime = Simulator.getCurrentTime();
			long latency = this.netLatencyModel.getLatency(sender, receiver);
			long arrivalTime = currentTime + latency;

			Simulator.scheduleEvent(msg, arrivalTime, this, SimulationEvent.Type.MESSAGE_RECEIVED);
		} else {
			// TODO: monitor packetloss
			log.debug("Packet loss occured (packetLossProb: " + packetLossProb + ")");

		}
	}

	/**
	 * Sends a NetworkMessage
	 * 
	 * @param msg
	 *            The NetworkMessage that should be send.
	 */
	public void send(NetMessage msg) {
		GnpNetLayer sender = this.wrappers.get(msg.getSender());
		GnpNetLayer receiver = this.wrappers.get(msg.getReceiver());
		// sender & receiver are registered in the SubNet
		if (sender != null && receiver != null) {
			sendUDP((IPv4Message) msg);
		} else
			throw new IllegalStateException("Receiver or Sender is not registered");
	}

	/**
	 * Processes a SimulationEvent. (Is called by the Scheduler.)
	 * 
	 * @param se
	 *            SimulationEvent that is returned by the Scheduler.
	 */
	public void eventOccurred(SimulationEvent se) {
		IPv4Message msg = (IPv4Message) se.getData();
		GnpNetLayer receiver = this.wrappers.get(msg.getReceiver());
		// message is given to the receiver
		receiver.receive(msg);
	}

}