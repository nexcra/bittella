package de.tud.kom.p2psim.impl.network.gnp;

import java.util.HashMap;
import java.util.Set;

import org.apache.log4j.Logger;

import de.tud.kom.p2psim.api.network.NetLatencyModel;
import de.tud.kom.p2psim.api.network.NetLayer;
import de.tud.kom.p2psim.api.network.NetPosition;
import de.tud.kom.p2psim.impl.network.gnp.topology.LinkProperty;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.util.logging.SimLogger;

/**
 * Model for the 3 lower ISO/OSI layers.
 * 
 * @author Andreas Sebastian Kaune
 */
public class GnpLatencyModel implements NetLatencyModel {

	private double rttMultiplicator;

	private static HashMap<String, HashMap<String, LinkProperty>> linkProperty;

	private static HashMap<String, String[]> countryLockup;

	private static LinkProperty averageLinkProperty;

	private static Logger log = SimLogger.getLogger(GnpLatencyModel.class);

	private boolean usePingERinsteadOfGnp = false;

	public GnpLatencyModel() {
		this.rttMultiplicator = Simulator.MILLISECOND_UNIT;
		// this.rttMultiplicator = 1;
	}

	public void initLinkProperty(HashMap<String, HashMap<String, LinkProperty>> regionLinkProperty, HashMap<String, String[]> countryLockup) {
		GnpLatencyModel.linkProperty = regionLinkProperty;
		GnpLatencyModel.countryLockup = countryLockup;
		GnpLatencyModel.averageLinkProperty = calculateAverageLinkProperty();
	}

	/**
	 * The maximum segment size (in bytes): An IP-packet over Ethernet has a
	 * payload of up to 1500 bytes. For simplicity, the inner headers (of TCP or
	 * UDP) are disregarded.
	 */
	public final static long MSS = 1500;

	/**
	 * Calculates the round trip time between two NetworkWrappers.
	 * 
	 * @param sender
	 *            First NetWrapper.
	 * @param receiver
	 *            Second NetWrapper.
	 * @return The RTT (in ZE).
	 */
	private double getRTT(GnpNetLayer sender, GnpNetLayer receiver) {

		LinkProperty lp = this.getLinkProperty(sender, receiver);

		double jitter = lp.nextJitter() * rttMultiplicator;

		double minRtt = 0.0;
		if (this.usePingERinsteadOfGnp) {
			minRtt = lp.getMinRtt();
		} else {
			NetPosition senderPos = sender.getNetPosition();
			NetPosition receiverPos = receiver.getNetPosition();
			minRtt = senderPos.getDistance(receiverPos);
		}

		minRtt *= rttMultiplicator;

		double rtt = minRtt + jitter;

		log.debug("Rtt(ms): " + rtt / Simulator.MILLISECOND_UNIT + " [Simulation Time Units: " + rtt + " minRtt(ms): " + minRtt / Simulator.MILLISECOND_UNIT + ", jitter(ms): " + jitter
				/ Simulator.MILLISECOND_UNIT + ", packetLoss(%): " + lp.getPacketLoss() + " avg PingER Rtt(ms): " + lp.getAverageRtt() + " Deviation: " + lp.getDelayVariation() + "]");
		return rtt;
	}

	private double getPacketLossProbability(GnpNetLayer sender, GnpNetLayer receiver) {
		LinkProperty lp = this.getLinkProperty(sender, receiver);
		return lp.getPacketLoss() / 100.0;
	}

	/**
	 * Calculates the error probability for the transmission of a
	 * NetworkMessage. (Maps the error probability of a single packet to the
	 * size of the whole NetworkMessage.)
	 * 
	 * @param size
	 *            The size of the NetworkMessage (in bytes).
	 * @return The error probability.
	 */
	protected double getErrorProbability(long size, GnpNetLayer sender, GnpNetLayer receiver) {
		double lp = getPacketLossProbability(sender, receiver);
		double errorProb =  1 - Math.pow(1-lp, (size / MSS) );
		return errorProb;
	}

	/**
	 * Calculates the latency for a small NetworkMessage that is send
	 * successfully (using TCP or UDP). Bandwidth and size are ignored, because
	 * a small NetworkMessage is received instantaniously.
	 * 
	 * @param sender
	 *            The NetWrapper that acts as sender.
	 * @param receiver
	 *            The NetWrapper that acts as receiver.
	 * @return The latency (in ZE).
	 */
	public long getLatency(NetLayer sender, NetLayer receiver) {
		// after this time, the beginning and end of the message reach the
		// receiver (because message is small)
		double receiveTime = getRTT((GnpNetLayer) sender, (GnpNetLayer) receiver) / 2;
		long latency = Math.round(receiveTime);
		log.info("Calculated latency: " + sender.getHost().getProperties().getGroupID() + " -> " + receiver.getHost().getProperties().getGroupID() + " = " + receiveTime / Simulator.MILLISECOND_UNIT
				+ " ms");
		return latency;
	}

	public void setRttData(String data) {
		if (data.equals("pingER"))
			this.usePingERinsteadOfGnp = true;
	}

	private LinkProperty getLinkProperty(GnpNetLayer sender, GnpNetLayer receiver) {

		String codeSender = sender.getCountryCode();
		String codeReceiver = receiver.getCountryCode();
		String countrySender = countryLockup.get(codeSender)[0];
		String countryReceiver = countryLockup.get(codeReceiver)[0];
		String regionSender = countryLockup.get(codeSender)[1];
		String regionReceiver = countryLockup.get(codeReceiver)[1];

		if (linkProperty.containsKey(countrySender)) {
			if (linkProperty.get(countrySender).containsKey(countryReceiver))
				return linkProperty.get(countrySender).get(countryReceiver);
			else if (linkProperty.get(countrySender).containsKey(regionReceiver))
				return linkProperty.get(countrySender).get(regionReceiver);
		} else if (linkProperty.containsKey(regionSender)) {
			if (linkProperty.get(regionSender).containsKey(countryReceiver))
				return linkProperty.get(regionSender).get(countryReceiver);
			else if (linkProperty.get(regionSender).containsKey(regionReceiver))
				return linkProperty.get(regionSender).get(regionReceiver);
		} else if (linkProperty.containsKey(countryReceiver)) {
			if (linkProperty.get(countryReceiver).containsKey(countrySender))
				return linkProperty.get(countryReceiver).get(countrySender);
			else if (linkProperty.get(countryReceiver).containsKey(regionSender))
				return linkProperty.get(countryReceiver).get(regionSender);
		} else if (linkProperty.containsKey(regionReceiver)) {
			if (linkProperty.get(regionReceiver).containsKey(countrySender))
				return linkProperty.get(regionReceiver).get(countrySender);
			else if (linkProperty.get(regionReceiver).containsKey(regionSender))
				return linkProperty.get(regionReceiver).get(regionSender);
		}
		return averageLinkProperty;
	}

	private LinkProperty calculateAverageLinkProperty() {
		double minRtt = 0.0;
		double averageRtt = 0.0;
		double delayVariation = 0.0;
		double packetLoss = 0.0;
		int counter = 0;

		Set<String> country1 = linkProperty.keySet();
		for (String c1 : country1) {
			Set<String> country2 = linkProperty.get(c1).keySet();
			for (String c2 : country2) {
				minRtt += linkProperty.get(c1).get(c2).getMinRtt();
				averageRtt += linkProperty.get(c1).get(c2).getAverageRtt();
				delayVariation += linkProperty.get(c1).get(c2).getDelayVariation();
				packetLoss += linkProperty.get(c1).get(c2).getPacketLoss();
				counter++;
			}
		}
		minRtt /= counter;
		averageRtt /= counter;
		delayVariation /= counter;
		packetLoss /= counter;
		LinkProperty lp = new LinkProperty(minRtt, averageRtt, delayVariation, packetLoss);
		return lp;
	}

}