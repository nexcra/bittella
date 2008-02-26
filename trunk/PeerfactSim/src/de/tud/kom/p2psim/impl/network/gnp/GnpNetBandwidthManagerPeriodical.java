package de.tud.kom.p2psim.impl.network.gnp;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.tud.kom.p2psim.api.network.NetLayer;
import de.tud.kom.p2psim.impl.network.AbstractNetLayer;

public class GnpNetBandwidthManagerPeriodical extends AbstractGnpNetBandwidthManager {

	private HashSet<NetLayer> changedSenders;
	private HashSet<NetLayer> changedReceivers;

	private Set<GnpNetBandwidthAllocation> changedAllocations; // within last
																// realocation

	public GnpNetBandwidthManagerPeriodical() {
		super();
		changedSenders = new HashSet<NetLayer>();
		changedReceivers = new HashSet<NetLayer>();
		changedAllocations = new HashSet<GnpNetBandwidthAllocation>();
	}

	@Override
	public GnpNetBandwidthAllocation addConnection(AbstractNetLayer sender,
			AbstractNetLayer receiver, double bandwidth) {
		changedSenders.add(sender);
		changedReceivers.add(receiver);
//		sender.setCurrentUpBandwidth(sender.getMaxUploadBandwidth());
//		receiver.setCurrentDownBandwidth(receiver.getMaxDownloadBandwidth());
		GnpNetBandwidthAllocation ba = super.addConnection(sender, receiver, bandwidth);
		return ba;
	}

	@Override
	public GnpNetBandwidthAllocation removeConnection(AbstractNetLayer sender,AbstractNetLayer receiver, double bandwidth) {
		GnpNetBandwidthAllocation ba =  super.removeConnection(sender, receiver, bandwidth);
		if (connectionsSenderToReceiver.containsKey(sender))
			changedSenders.add(sender);
		else
			changedSenders.remove(sender);
		if (connectionsReceiverToSender.containsKey(receiver))
			changedReceivers.add(receiver);
		else
			changedReceivers.remove(receiver);
		return ba;
	};

	@Override
	public void allocateBandwidth() {
		HashSet<AbstractNetLayer> chSenders = (HashSet<AbstractNetLayer>) changedSenders.clone();
		HashSet<AbstractNetLayer> chReceivers = (HashSet<AbstractNetLayer>) changedReceivers.clone();
		changedSenders.clear();
		changedReceivers.clear();
		changedAllocations.clear();
		for (AbstractNetLayer host : chSenders) {
			host.setCurrentUpBandwidth(host.getMaxUploadBandwidth());
			Set<GnpNetBandwidthAllocation> temp = new HashSet<GnpNetBandwidthAllocation>();
			temp.addAll(connectionsSenderToReceiver.get(host).values());
			fairShare(temp, true);
		}
		for (AbstractNetLayer host : chReceivers) {
			host.setCurrentDownBandwidth(host.getMaxDownloadBandwidth());
			Set<GnpNetBandwidthAllocation> temp = new HashSet<GnpNetBandwidthAllocation>();
			temp.addAll(connectionsReceiverToSender.get(host).values());
			fairShare(temp, false);
		}
	}

	private void fairShare(Collection<GnpNetBandwidthAllocation> unassigned,
			boolean isSender) {

		if (unassigned.isEmpty())
			return;

		double x = Double.POSITIVE_INFINITY;

		GnpNetBandwidthAllocation min = null;
		for (GnpNetBandwidthAllocation ba : unassigned) {

			double bandwidth = 0;
			if (isSender)
				bandwidth = Math.min(ba.getReceiver()
						.getCurrentDownloadBandwidth()
						+ ba.getAllocatedBandwidth(), ba.getBandwidthNeeds()); 
			else
				bandwidth = Math.min(ba.getSender().getCurrentUploadBandwidth()
						+ ba.getAllocatedBandwidth(), ba.getBandwidthNeeds());
			
			if (bandwidth < x) {
				x = bandwidth;
				min = ba;
			} 				
		}

		
		
		double bw = 0;
		if (isSender)
			bw = min.getSender().getCurrentUploadBandwidth() / unassigned.size();
		else
			bw = min.getReceiver().getCurrentDownloadBandwidth() / unassigned.size();

		if (x < bw) {
			if (min.getAllocatedBandwidth() != x) {
				changedAllocations.add(min);
				min.setAllocatedBandwidth(x);
				if (isSender)
					changedReceivers.add(min.getReceiver());
				else
					changedSenders.add(min.getSender());
			}

			if (isSender)
				min.getSender().setCurrentUpBandwidth(
						min.getSender().getCurrentUploadBandwidth()
								- min.getAllocatedBandwidth());
			else
				min.getReceiver().setCurrentDownBandwidth(
						min.getReceiver().getCurrentDownloadBandwidth()
								- min.getAllocatedBandwidth());

			unassigned.remove(min);
			fairShare(unassigned, isSender);
		} else {
			for (GnpNetBandwidthAllocation ba : unassigned) {
				if (ba.getAllocatedBandwidth() != bw) {
					changedAllocations.add(ba);
					ba.setAllocatedBandwidth(bw);
					if (isSender)
						changedReceivers.add(ba.getReceiver());
					else
						changedSenders.add(ba.getSender());
				}
				if (isSender)
					ba.getSender().setCurrentUpBandwidth(0);
				else
					ba.getReceiver().setCurrentDownBandwidth(0);
			}

		}
	}


	@Override
	public BandwidthAllocation getBandwidthAllocationType() {
		return AbstractGnpNetBandwidthManager.BandwidthAllocation.PERIODICAL;
	}

	@Override
	public Set<GnpNetBandwidthAllocation> getChangedAllocations() {
		return changedAllocations;
	}
}