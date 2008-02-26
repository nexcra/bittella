package de.tud.kom.p2psim.impl.network.gnp;

import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.impl.simengine.Simulator;

public class TransferProgress {

	private NetMessage message;

	private double bandwidth; // in Simulator Time Units;
	private double remainingBytes;
	private long scheduledAt;

	public TransferProgress(NetMessage msg, double bandwidth,
			double remainingBytes, long scheduledAt) {
		this.message = msg;
		this.bandwidth = bandwidth / Simulator.SECOND_UNIT;
		this.remainingBytes = remainingBytes;
		this.scheduledAt = scheduledAt;
	}

	public NetMessage getMessage() {
		return message;
	}

	public double getRemainingBytes(long time) {
		long interval = time - scheduledAt;
		return remainingBytes - (interval * bandwidth);
	}

	/*
	 * public long getArrivalTime() { return arrivalTime; }
	 */

}
