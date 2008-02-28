package de.tud.kom.p2psim.impl.network.gnp;

import org.apache.log4j.Logger;

import umontreal.iro.lecuyer.probdist.LognormalDist;
import de.tud.kom.p2psim.api.network.NetLatencyModel;
import de.tud.kom.p2psim.api.network.NetLayer;
import de.tud.kom.p2psim.api.network.NetPosition;
import de.tud.kom.p2psim.impl.network.gnp.topology.CountryLookup;
import de.tud.kom.p2psim.impl.network.gnp.topology.GeographicPosition;
import de.tud.kom.p2psim.impl.network.gnp.topology.PingErLookup;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.transport.TCPMessage;
import de.tud.kom.p2psim.impl.util.logging.SimLogger;

public class GnpLatencyModel implements NetLatencyModel {

	private static Logger log = SimLogger.getLogger(GnpLatencyModel.class);

	public static final int MSS = IPv4Message.MTU_SIZE - IPv4Message.HEADER_SIZE - TCPMessage.HEADER_SIZE;

	private static PingErLookup pingErLockup;

	private static CountryLookup countryLockup;

	private boolean usePingErInsteadOfGnp = false;

	private boolean useAnalyticalFunctionInsteadOfGnp = false;

	private boolean usePingErJitter = false;

	private boolean usePingErPacketLoss = false;

	public void init(PingErLookup pingErLockup, CountryLookup countryLockup) {
		GnpLatencyModel.pingErLockup = pingErLockup;
		GnpLatencyModel.countryLockup = countryLockup;
	}

	private double getMinimumRTT(GnpNetLayer sender, GnpNetLayer receiver) {
		String ccSender = sender.getCountryCode();
		String ccReceiver = receiver.getCountryCode();
		double minRtt = 0.0;
		if (usePingErInsteadOfGnp) {
			minRtt = pingErLockup.getMinimumRtt(ccSender, ccReceiver, countryLockup);
		} else if (useAnalyticalFunctionInsteadOfGnp) {
			GeographicPosition senderPos = sender.getGeographicPosition();
			GeographicPosition receiverPos = receiver.getGeographicPosition();
			minRtt = 62 + (0.02 * senderPos.getDistance(receiverPos));
		} else {
			NetPosition senderPos = sender.getNetPosition();
			NetPosition receiverPos = receiver.getNetPosition();
			minRtt = senderPos.getDistance(receiverPos);
		}
		log.debug("Minimum RTT for " + ccSender + " to " + ccReceiver + ": " + minRtt + " ms");
		return minRtt;
	}

	private double getPacketLossProbability(GnpNetLayer sender, GnpNetLayer receiver) {
		String ccSender = sender.getCountryCode();
		String ccReceiver = receiver.getCountryCode();
		double twoWayLossRate = 0.0;
		double oneWayLossRate = 0.0;
		if (usePingErPacketLoss) {
			twoWayLossRate = pingErLockup.getPacktLossRate(ccSender, ccReceiver, countryLockup);
			twoWayLossRate /= 100;
			oneWayLossRate = 1 - Math.sqrt(1 - twoWayLossRate);
		}
		log.debug("Packet Loss Probability for " + ccSender + " to " + ccReceiver + ": " + (oneWayLossRate * 100) + " %");
		return oneWayLossRate;

	}

	private double getNextJitter(GnpNetLayer sender, GnpNetLayer receiver) {
		String ccSender = sender.getCountryCode();
		String ccReceiver = receiver.getCountryCode();
		double randomJitter = 0.0;
		if (usePingErJitter) {
			LognormalDist distri = pingErLockup.getJitterDistribution(ccSender, ccReceiver, countryLockup);
			randomJitter = distri.inverseF(Simulator.getRandom().nextDouble());
		}
		log.debug("Random Jitter for " + ccSender + " to " + ccReceiver + ": " + randomJitter + " ms");
		return randomJitter;

	}

	private double getAverageJitter(GnpNetLayer sender, GnpNetLayer receiver) {
		String ccSender = sender.getCountryCode();
		String ccReceiver = receiver.getCountryCode();
		double jitter = 0.0;
		if (usePingErJitter) {
			jitter = pingErLockup.getAverageRtt(ccSender, ccReceiver, countryLockup) - pingErLockup.getMinimumRtt(ccSender, ccReceiver, countryLockup);
		}
		log.debug("Average Jitter for " + ccSender + " to " + ccReceiver + ": " + jitter + " ms");
		return jitter;
	}

	public double getUDPerrorProbability(GnpNetLayer sender, GnpNetLayer receiver, IPv4Message msg) {
		if (msg.getPayload().getSize() > 65507)
			throw new IllegalArgumentException("Message-Size ist too big for a UDP-Datagramm (max 65507 byte)");
		double lp = getPacketLossProbability(sender, receiver);
		double errorProb = 1 - Math.pow(1 - lp, msg.getNoOfFragments());
		log.info("Error Probability for a " + msg.getPayload().getSize() + " byte UDP Datagram from " + sender.getCountryCode() + " to " + receiver.getCountryCode() + ": " + errorProb * 100 + " %");
		return errorProb;
	}

	public double getTcpThroughput(GnpNetLayer sender, GnpNetLayer receiver) {
		double minRtt = getMinimumRTT(sender, receiver);
		double averageJitter = getAverageJitter(sender, receiver);
		double packetLossRate = getPacketLossProbability(sender, receiver);
		double mathisBW = ((MSS * 1000) / (minRtt + averageJitter)) * Math.sqrt(1.5 / packetLossRate);
		return mathisBW;
	}

	public long getTransmissionDelay(double bytes, double bandwidth) {
		double messageTime = bytes / bandwidth;
		long delay = Math.round((messageTime * Simulator.SECOND_UNIT));
		log.info("Transmission Delay (s): " + messageTime + " ( " + bytes + " bytes  /  " + bandwidth + " bytes/s )");
		return delay;
	}

	public long getPropagationDelay(GnpNetLayer sender, GnpNetLayer receiver) {
		double minRtt = getMinimumRTT(sender, receiver);
		double randomJitter = getNextJitter(sender, receiver);
		double receiveTime = (minRtt + randomJitter) / 2.0;
		long latency = Math.round(receiveTime * Simulator.MILLISECOND_UNIT);
		log.info("Propagation Delay for " + sender.getCountryCode() + " to " + receiver.getCountryCode() + ": " + receiveTime + " ms");
		return latency;
	}

	public long getLatency(NetLayer sender, NetLayer receiver) {
		return getPropagationDelay((GnpNetLayer) sender, (GnpNetLayer) receiver);
	}

	public void setUsePingErRttData(boolean pingErRtt) {
		usePingErInsteadOfGnp = pingErRtt;
	}

	public void setUseAnalyticalRtt(boolean analyticalRtt) {
		useAnalyticalFunctionInsteadOfGnp = analyticalRtt;
	}

	public void setUsePingErJitter(boolean pingErRtt) {
		usePingErJitter = pingErRtt;
	}

	public void setUsePingErPacketLoss(boolean pingErPacketLoss) {
		usePingErPacketLoss = pingErPacketLoss;
	}

}