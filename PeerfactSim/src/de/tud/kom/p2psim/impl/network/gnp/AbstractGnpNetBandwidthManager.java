package de.tud.kom.p2psim.impl.network.gnp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.tud.kom.p2psim.api.network.NetLayer;
import de.tud.kom.p2psim.impl.network.AbstractNetLayer;

/**
 * 
 * @author Gerald Klunker
 * @version 0.1, 09.01.2008
 * 
 */
public abstract class AbstractGnpNetBandwidthManager {

	public enum BandwidthAllocation {
		PERIODICAL, EVENT
	}

	protected Map<AbstractNetLayer, Map<AbstractNetLayer, GnpNetBandwidthAllocation>> connectionsSenderToReceiver;
	protected Map<AbstractNetLayer, Map<AbstractNetLayer, GnpNetBandwidthAllocation>> connectionsReceiverToSender;

	public AbstractGnpNetBandwidthManager() {
		connectionsSenderToReceiver = new HashMap<AbstractNetLayer, Map<AbstractNetLayer, GnpNetBandwidthAllocation>>();
		connectionsReceiverToSender = new HashMap<AbstractNetLayer, Map<AbstractNetLayer, GnpNetBandwidthAllocation>>();
	}

	public GnpNetBandwidthAllocation addConnection(AbstractNetLayer sender, AbstractNetLayer receiver, double bandwidth) {
		if (!connectionsSenderToReceiver.containsKey(sender))
			connectionsSenderToReceiver.put(sender,
					new HashMap<AbstractNetLayer, GnpNetBandwidthAllocation>());
		if (!connectionsSenderToReceiver.get(sender).containsKey(receiver))
			connectionsSenderToReceiver.get(sender).put(receiver,
					new GnpNetBandwidthAllocation(sender, receiver));
		GnpNetBandwidthAllocation c = connectionsSenderToReceiver.get(sender).get(receiver);
		if (!connectionsReceiverToSender.containsKey(receiver))
			connectionsReceiverToSender.put(receiver,
					new HashMap<AbstractNetLayer, GnpNetBandwidthAllocation>());
		if (!connectionsReceiverToSender.get(receiver).containsKey(sender))
			connectionsReceiverToSender.get(receiver).put(sender, c);
		c.setAllocatedBandwidth(0);
		sender.setCurrentUpBandwidth(sender.getMaxUploadBandwidth());
		receiver.setCurrentDownBandwidth(receiver.getMaxDownloadBandwidth());
		c.setBandwidthNeeds(c.getBandwidthNeeds() + bandwidth);
		return c;
	}

	public GnpNetBandwidthAllocation removeConnection(AbstractNetLayer sender, AbstractNetLayer receiver, double bandwidth) {
		GnpNetBandwidthAllocation ba = null;
		if (connectionsSenderToReceiver.containsKey(sender)) {
			ba = connectionsSenderToReceiver.get(sender).get(receiver);
			if (bandwidth < 0)
				ba.setBandwidthNeeds(0);
			else {
				ba.setBandwidthNeeds(ba.getBandwidthNeeds()-bandwidth);
			}
			if (ba.getBandwidthNeeds() == 0.0) {
				connectionsSenderToReceiver.get(sender).remove(receiver);
				if (connectionsSenderToReceiver.get(sender).isEmpty())
					connectionsSenderToReceiver.remove(sender);
				connectionsReceiverToSender.get(receiver).remove(sender);
				if (connectionsReceiverToSender.get(receiver).isEmpty())
					connectionsReceiverToSender.remove(receiver);
			}
			ba.setAllocatedBandwidth(0);
		}
		sender.setCurrentUpBandwidth(sender.getMaxUploadBandwidth());
		receiver.setCurrentDownBandwidth(receiver.getMaxDownloadBandwidth());
		return ba;
	}

	public Set<GnpNetBandwidthAllocation> removeConnections(
			AbstractNetLayer netLayer) {
		Set<GnpNetBandwidthAllocation> connections = new HashSet<GnpNetBandwidthAllocation>();
		if (connectionsSenderToReceiver.containsKey(netLayer)) {
			for (AbstractNetLayer receiver : connectionsSenderToReceiver.get(netLayer).keySet()) {
				connections.add(removeConnection(netLayer, receiver,
						-1));
			}
		}
		if (connectionsReceiverToSender.containsKey(netLayer)) {
			for (AbstractNetLayer sender : connectionsReceiverToSender.get(netLayer).keySet()) {
				connections.add(removeConnection(sender, netLayer,
						-1));
			}
		}
		return connections;
	}
	

	public GnpNetBandwidthAllocation getBandwidthAllocation(NetLayer sender,
			NetLayer receiver) {
		if (connectionsSenderToReceiver.get(sender) == null)
			return null;
		else
			return connectionsSenderToReceiver.get(sender).get(receiver);
	}

	public abstract void allocateBandwidth();

	public abstract BandwidthAllocation getBandwidthAllocationType();

	public abstract Set<GnpNetBandwidthAllocation> getChangedAllocations();

}
