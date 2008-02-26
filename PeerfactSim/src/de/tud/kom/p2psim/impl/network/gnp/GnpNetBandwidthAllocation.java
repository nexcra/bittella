package de.tud.kom.p2psim.impl.network.gnp;

import de.tud.kom.p2psim.impl.network.AbstractNetLayer;

/**
 * 
 * @author Gerald Klunker
 * @version 0.01, 07/12/12
 */
public class GnpNetBandwidthAllocation {

	private double allocatedBandwidth = 0.0;
	private double bandwidthNeeds = 0.0;

	private AbstractNetLayer receiver;
	private AbstractNetLayer sender;

	private double bidSender[] = new double[2];
	private double bidReciever[] = new double[2];

	private boolean minBidSender = false;
	private boolean minBidReceiver = false;
	
	public GnpNetBandwidthAllocation(AbstractNetLayer sender, AbstractNetLayer receiver) {
		this.sender = sender;
		this.receiver = receiver;
		bidSender[0] = 0;
		bidSender[1] = 0;
		bidReciever[0] = 0;
		bidReciever[1] = 0;
	}
	
	public double getAllocatedBandwidth() {
		return allocatedBandwidth;
	}

	public void setAllocatedBandwidth(double allocatedBandwidth) {
		this.allocatedBandwidth = allocatedBandwidth;
	}

	public double getBandwidthNeeds() {
		return bandwidthNeeds;
	}

	public void setBandwidthNeeds(double bandwidthNeeds) {
		this.bandwidthNeeds = bandwidthNeeds;
	}

	public AbstractNetLayer getReceiver() {
		return receiver;
	}

	public AbstractNetLayer getSender() {
		return sender;
	}

	/*
	 * Eventbased Allocation only
	 */
	
	
	public void initConnection() {
		bidSender[0] = 0;
		bidSender[1] = 0;
		bidReciever[0] = 0;
		bidReciever[1] = 0;
		minBidSender = false;
		minBidReceiver = false;
		sender.setCurrentUpBandwidth(sender.getMaxUploadBandwidth());
		receiver.setCurrentDownBandwidth(receiver.getMaxDownloadBandwidth());
	}
	
	public void setBid(double bid, boolean isMinimal, boolean sender, long step) {
		if (sender) {
			int posC = (int) (step % 2);
			bidSender[posC] = bid;
			minBidSender = isMinimal;
		} else {
			int posC = (int) (step % 2);
			bidReciever[posC] = bid;
			minBidReceiver = isMinimal;
		}
	}

	public double getCurrentBid(boolean sender, long step) {
		if (sender) {
			return bidSender[(int) (step % 2)];
		} else {
			return bidReciever[(int) (step % 2)];
		}
	}

	public double getPreviousBid(boolean sender, long step) {
		if (sender) {
			return bidSender[(int) ((step + 1) % 2)];
		} else {
			return bidReciever[(int) ((step + 1) % 2)];
		}
	}

	public boolean isBidRepeated(boolean sender) {
		if (sender) {
			return Math.abs(bidSender[0] - bidSender[1]) <= 0.0001;
		} else {
			return Math.abs(bidReciever[0] - bidReciever[1]) <= 0.0001;
		}
	}

	public boolean isMinBid(boolean sender) {
		if (sender) {
			return minBidSender;
		} else {
			return minBidReceiver;
		}
	}

}