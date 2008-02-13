package de.tud.kom.p2psim.impl.network.gnp;

import de.tud.kom.p2psim.api.common.Message;
import de.tud.kom.p2psim.api.common.Monitor.Reason;
import de.tud.kom.p2psim.api.network.NetID;
import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.api.network.NetProtocol;
import de.tud.kom.p2psim.api.transport.TransProtocol;
import de.tud.kom.p2psim.impl.network.AbstractNetLayer;
import de.tud.kom.p2psim.impl.network.gnp.topology.GnpPosition;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.transport.AbstractTransMessage;

/**
 * Implementation of the Interface NetWrapper, that considers the bandwidth
 * management.
 * 
 * @author Andreas Glaser, Sebastian Kaune
 */
public class GnpNetLayer extends AbstractNetLayer {

	private String country;

	/**
	 * The SubNet the NetWrapper belongs to.
	 */
	private GnpSubnet subnet;

	public GnpNetLayer(GnpSubnet subNet, IPv4NetID netID, GnpPosition netPosition, double downBandwidth, double upBandwidth, String country) {
		super(downBandwidth, upBandwidth, netPosition);
		this.subnet = subNet;
		this.myID = netID;
		this.online = true;
		this.country = country;
		subNet.registerNetLayer(this);
	}

	public String getCountryCode() {
		return country;
	}

	public boolean isSupported(TransProtocol transProtocol) {
		return (transProtocol.equals(TransProtocol.UDP));
	}

	public void send(Message msg, NetID receiver, NetProtocol netProtocol) {
		TransProtocol usedTransProtocol = ((AbstractTransMessage) msg).getProtocol();
		if (this.isSupported(usedTransProtocol)) {
			NetMessage netMsg = new IPv4Message(msg, receiver, this.myID, netProtocol);
			Simulator.getMonitor().netMsgEvent(netMsg, myID, Reason.SEND);
			this.subnet.send(netMsg);
		} else
			throw new IllegalArgumentException("Transport protocol " + usedTransProtocol + " not supported by this NetLayer implementation.");

	}

}