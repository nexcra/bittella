package de.tud.kom.p2psim.impl.network.gnp;

import java.util.HashMap;
import java.util.Map;

import de.tud.kom.p2psim.api.common.ConnectivityEvent;
import de.tud.kom.p2psim.api.common.ConnectivityListener;
import de.tud.kom.p2psim.api.common.Message;
import de.tud.kom.p2psim.api.common.Monitor.Reason;
import de.tud.kom.p2psim.api.network.NetID;
import de.tud.kom.p2psim.api.network.NetLayer;
import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.api.network.NetProtocol;
import de.tud.kom.p2psim.api.simengine.SimulationEvent;
import de.tud.kom.p2psim.api.simengine.SimulationEventHandler;
import de.tud.kom.p2psim.api.transport.TransProtocol;
import de.tud.kom.p2psim.impl.network.AbstractNetLayer;
import de.tud.kom.p2psim.impl.network.gnp.topology.GeographicPosition;
import de.tud.kom.p2psim.impl.network.gnp.topology.GnpPosition;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.transport.AbstractTransMessage;

/**
 * 
 * @author geraldklunker
 * 
 */
public class GnpNetLayer extends AbstractNetLayer implements SimulationEventHandler {

	private String country;
	
	private GeographicPosition geoPosition;
	
	private GnpSubnet subnet;

	private long nextFreeSendingTime = 0;
	private long nextFreeReceiveTime = 0;

	private Map<GnpNetLayer, GnpNetBandwidthAllocation> connections = new HashMap<GnpNetLayer, GnpNetBandwidthAllocation>();

	
	public GnpNetLayer(GnpSubnet subNet, IPv4NetID netID, GnpPosition netPosition, GeographicPosition geoPosition, double downBandwidth, double upBandwidth, String country) {
		super(downBandwidth, upBandwidth, netPosition);
		this.subnet = subNet;
		this.myID = netID;
		this.online = true;
		this.country = country;
		this.geoPosition = geoPosition;
		subNet.registerNetLayer(this);
	}

	/**
	 * 
	 * @return 2-digit country code
	 */
	public String getCountryCode() {
		return country;
	}

	/**
	 * 
	 * @return first time sending is possible (line is free)
	 */
	public long getNextFreeSendingTime() {
		return nextFreeSendingTime;
	}

	/**
	 * 
	 * @param time
	 *            first time sending is possible (line is free)
	 */
	public void setNextFreeSendingTime(long time) {
		nextFreeSendingTime = time;
	}

	/**
	 * 
	 * @param netLayer
	 * @return
	 */
	public boolean isConnected(GnpNetLayer netLayer) {
		return connections.containsKey(netLayer);
	}

	/**
	 * 
	 * @param netLayer
	 * @param allocation
	 */
	public void addConnection(GnpNetLayer netLayer,
			GnpNetBandwidthAllocation allocation) {
		connections.put(netLayer, allocation);
	}

	/**
	 * 
	 * @param netLayer
	 * @return
	 */
	public GnpNetBandwidthAllocation getConnection(GnpNetLayer netLayer) {
		return connections.get(netLayer);
	}

	/**
	 * 
	 * @param netLayer
	 */
	public void removeConnection(GnpNetLayer netLayer) {
		connections.remove(netLayer);
	}

	/**
	 * 
	 * @param msg
	 */
	public void addToReceiveQueue(IPv4Message msg) {
		long receiveTime = subnet.getLatencyModel().getTransmissionDelay(msg.getSize(), getMaxDownloadBandwidth());
		long currenTime = Simulator.getCurrentTime();
		long arrivalTime = nextFreeReceiveTime + receiveTime;
		if (arrivalTime <= currenTime) {
			nextFreeReceiveTime = currenTime;
			receive(msg);
		} else {
			nextFreeReceiveTime = arrivalTime;
			Simulator.scheduleEvent(msg, arrivalTime, this, SimulationEvent.Type.MESSAGE_RECEIVED);
		}
	}

	@Override
	public boolean isSupported(TransProtocol transProtocol) {
		return (transProtocol.equals(TransProtocol.UDP) || transProtocol.equals(TransProtocol.TCP));
	}

	public void send(Message msg, NetID receiver, NetProtocol netProtocol) {
		TransProtocol usedTransProtocol = ((AbstractTransMessage) msg)
				.getProtocol();
		if (this.isSupported(usedTransProtocol)) {
			NetMessage netMsg = new IPv4Message(msg, receiver, this.myID);
			log.info(Simulator.getSimulatedRealtime() + " Sending " + netMsg);
			Simulator.getMonitor().netMsgEvent(netMsg, myID, Reason.SEND);
			this.subnet.send(netMsg);
		} else
			throw new IllegalArgumentException("Transport protocol "
					+ usedTransProtocol
					+ " not supported by this NetLayer implementation.");

	}
	
	public void send(Message msg, NetID receiver, NetProtocol netProtocol, long sendTime) {
		Object[] msgInfo = new Object[3];
		msgInfo[0] = msg;
		msgInfo[1] = receiver;
		msgInfo[2] = netProtocol;
		Simulator.scheduleEvent(msgInfo, sendTime, this, SimulationEvent.Type.TEST_EVENT);
	}
	

	public GeographicPosition getGeographicPosition() {
		return geoPosition;
	}
	
	
	@Override
	public String toString() {
		return this.getNetID().toString() + " ( "
				+ this.getHost().getProperties().getGroupID() + " )";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tud.kom.p2psim.api.simengine.SimulationEventHandler#eventOccurred(de.tud.kom.p2psim.api.simengine.SimulationEvent)
	 */
	public void eventOccurred(SimulationEvent se) {
		if (se.getType() == SimulationEvent.Type.MESSAGE_RECEIVED)
			receive((NetMessage) se.getData());
		else if (se.getType() == SimulationEvent.Type.TEST_EVENT) {
			Object[] msgInfo = (Object[]) se.getData();
			send( (Message)msgInfo[0], (NetID)msgInfo[1], (NetProtocol)msgInfo[2]);
		}
		
		else if (se.getType() == SimulationEvent.Type.SCENARIO_ACTION && se.getData() == null) {
			goOffline();
		} else if (se.getType() == SimulationEvent.Type.SCENARIO_ACTION) {
			cancelTransmission((Integer) se.getData());
		}			
	}




	public void goOffline() {
		super.goOffline();
		subnet.goOffline(this);		
	}



	
	
	public void cancelTransmission(int commId) {
		subnet.cancelTransmission(commId);
	}
	
	
	// for JUnit Test
	
	public void goOffline(long time) {
		Simulator.scheduleEvent(null, time, this, SimulationEvent.Type.SCENARIO_ACTION);		
	}

	public void cancelTransmission(int commId, long time) {
		Simulator.scheduleEvent(new Integer(commId), time, this, SimulationEvent.Type.SCENARIO_ACTION);		
	}

}