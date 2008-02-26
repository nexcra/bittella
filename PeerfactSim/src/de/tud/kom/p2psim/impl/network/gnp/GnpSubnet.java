package de.tud.kom.p2psim.impl.network.gnp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import de.tud.kom.p2psim.api.network.NetLayer;
import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.api.simengine.SimulationEvent;
import de.tud.kom.p2psim.api.simengine.SimulationEventHandler;
import de.tud.kom.p2psim.impl.network.AbstractNetLayer;
import de.tud.kom.p2psim.impl.network.AbstractSubnet;
import de.tud.kom.p2psim.impl.network.gnp.AbstractGnpNetBandwidthManager.BandwidthAllocation;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.transport.AbstractTransMessage;
import de.tud.kom.p2psim.impl.transport.TCPMessage;
import de.tud.kom.p2psim.impl.transport.UDPMessage;
import de.tud.kom.p2psim.impl.util.logging.SimLogger;

/**
 * 
 * @author Gerald Klunker
 * @version 0.1, 17.01.2008
 * 
 */
public class GnpSubnet extends AbstractSubnet implements SimulationEventHandler {

	private static Logger log = SimLogger.getLogger(GnpSubnet.class);

	private static final Byte REALLOCATE_BANDWIDTH_PERIODICAL_EVENT = 0;
	private static final Byte REALLOCATE_BANDWIDTH_EVENTBASED_EVENT = 1;

	private AbstractGnpNetBandwidthManager bandwidthManager;

	private long pbaPeriod = 1 * Simulator.SECOND_UNIT;
	private long nextPbaTime = 0;
	private long nextResheduleTime = -1;

	private HashMap<GnpNetBandwidthAllocation, Set<TransferProgress>> currentlyTransferedStreams;
	private HashMap<Integer, TransferProgress> currentStreams;

	private int lastCommId = 0;
	
	private Set<TransferProgress> obsoleteEvents;

	
	private GnpLatencyModel netLatencyModel;
	private Map<IPv4NetID, GnpNetLayer> layers;

	
	public GnpSubnet() {
		this.layers = new HashMap<IPv4NetID, GnpNetLayer>();
		this.netLatencyModel = new GnpLatencyModel();
		this.obsoleteEvents = new HashSet<TransferProgress>();
		this.currentlyTransferedStreams = new HashMap<GnpNetBandwidthAllocation, Set<TransferProgress>>();
		this.currentStreams = new HashMap<Integer, TransferProgress>();
	}

	public void setLatencyModel(GnpLatencyModel netLatencyModel) {
		this.netLatencyModel = netLatencyModel;
	}

	public GnpLatencyModel getLatencyModel() {
		return netLatencyModel;
	}

	public void setBandwidthManager(AbstractGnpNetBandwidthManager bm) {
		this.bandwidthManager = bm;
	}

	public void setPbaPeriod(long timeUnits) {
		this.pbaPeriod = timeUnits;
	}

	/**
	 * Registers a NetWrapper in the SubNet.
	 * 
	 * @param wrapper
	 *            The NetWrapper.
	 */
	@Override
	public void registerNetLayer(NetLayer netLayer) {
		this.layers
				.put((IPv4NetID) netLayer.getNetID(), (GnpNetLayer) netLayer);
	}


	/**
	 * 
	 */
	@Override
	public void send(NetMessage msg) {

		GnpNetLayer sender = this.layers.get(msg.getSender());
		GnpNetLayer receiver = this.layers.get(msg.getReceiver());

		// sender & receiver are registered in the SubNet
		if (sender == null || receiver == null)
			throw new IllegalStateException("Receiver or Sender is not registered");

		if (msg.getPayload() instanceof UDPMessage) {
			double packetLossProb = this.netLatencyModel.getUDPerrorProbability(sender, receiver, (IPv4Message) msg);
			if (Simulator.getRandom().nextDouble() < packetLossProb) {
				log.info("Packet loss occured while transfer \"" + msg + "\" (packetLossProb: " + packetLossProb + ")");
				AbstractTransMessage transMsg = (AbstractTransMessage) msg.getPayload();
				transMsg.getCommId();
			}
		}
		
		sendMessage((IPv4Message) msg, sender, receiver);
	}

	/**
	 * 
	 * @param msg
	 * @param sender
	 * @param receiver
	 */
	private void sendMessage(IPv4Message msg, GnpNetLayer sender, GnpNetLayer receiver) {

		long currentTime = Simulator.getCurrentTime();

		int currentCommId = lastCommId;
		
		AbstractTransMessage transMsg = (AbstractTransMessage) msg.getPayload();
		if (transMsg.getCommId() == -1) {
			lastCommId++;
			currentCommId = lastCommId;
			transMsg.setCommId(currentCommId);
		} else {
			currentCommId = transMsg.getCommId();
		}
			
		
		
		// Case 1: message only consists of 1 Segment => no bandwidth allocation
		if (msg.getNoOfFragments() == 1) {
			long propagationTime = netLatencyModel.getPropagationDelay(sender, receiver);
			long transmissionTime = netLatencyModel.getTransmissionDelay(msg.getSize(), Math.min(sender.getMaxUploadBandwidth(), receiver.getMaxDownloadBandwidth()));
			long sendingTime = Math.max(sender.getNextFreeSendingTime(), currentTime) + transmissionTime;
			long arrivalTime = sendingTime + propagationTime;
			sender.setNextFreeSendingTime(sendingTime);
			TransferProgress newTp = new TransferProgress(msg, Double.POSITIVE_INFINITY, 0, currentTime);
			Simulator.scheduleEvent(newTp, arrivalTime, this, SimulationEvent.Type.MESSAGE_RECEIVED);
		}

		// Case 2: message consists minimum 2 Segments => bandwidth allocation
		else {

			// Add streams to current transfers
			double maximumRequiredBandwidth = sender.getMaxUploadBandwidth();
			if (msg.getPayload() instanceof TCPMessage) {
				double tcpThroughput = netLatencyModel.getTcpThroughput(sender, receiver);
				maximumRequiredBandwidth = Math.min(maximumRequiredBandwidth, tcpThroughput);
			}
			GnpNetBandwidthAllocation ba = bandwidthManager.addConnection( sender, receiver, maximumRequiredBandwidth);
			TransferProgress newTp = new TransferProgress(msg, 0, msg.getSize(), currentTime);
			if (!currentlyTransferedStreams.containsKey(ba))
				currentlyTransferedStreams.put(ba, new HashSet<TransferProgress>());
			currentlyTransferedStreams.get(ba).add(newTp);
			currentStreams.put(currentCommId, newTp);

			// Case 2a: Periodical Bandwidth Allocation
			// Schedule the first Periodical Bandwidth Allocation Event
			if (bandwidthManager.getBandwidthAllocationType() == BandwidthAllocation.PERIODICAL) {
				if (nextPbaTime == 0) {
					nextPbaTime = Simulator.getCurrentTime() + pbaPeriod;
					Simulator.scheduleEvent(
							REALLOCATE_BANDWIDTH_PERIODICAL_EVENT, nextPbaTime,
							this, SimulationEvent.Type.MESSAGE_RECEIVED);
				}
			}

			// Case 2b: Eventbased Bandwidth Allocation
			// Schedule an realocation Event after current timeunit
			else if (bandwidthManager.getBandwidthAllocationType() == BandwidthAllocation.EVENT) {
				if (nextResheduleTime <= currentTime + 1) {
					nextResheduleTime = currentTime + 1;
					Simulator.scheduleEvent(
							REALLOCATE_BANDWIDTH_EVENTBASED_EVENT,
							nextResheduleTime, this,
							SimulationEvent.Type.MESSAGE_RECEIVED);
				}
			}
		}
	}

	/**
	 * 
	 * @param netLayer
	 */
	public void goOffline(NetLayer netLayer) {
		if (bandwidthManager!= null && bandwidthManager.getBandwidthAllocationType() == BandwidthAllocation.EVENT) {
			for (GnpNetBandwidthAllocation ba : bandwidthManager.removeConnections((AbstractNetLayer) netLayer)) {
				obsoleteEvents.addAll(currentlyTransferedStreams.get(ba));
				currentStreams.values().removeAll(currentlyTransferedStreams.get(ba));
				currentlyTransferedStreams.remove(ba);
				
			}
			// Reschedule messages after current timeunit
			long currentTime = Simulator.getCurrentTime();
			if (nextResheduleTime <= currentTime + 1) {
				nextResheduleTime = currentTime + 1;
				Simulator.scheduleEvent(REALLOCATE_BANDWIDTH_EVENTBASED_EVENT, nextResheduleTime, this, SimulationEvent.Type.MESSAGE_RECEIVED);
			}
		} else if (bandwidthManager!= null) {
			for (GnpNetBandwidthAllocation ba : bandwidthManager.removeConnections((AbstractNetLayer) netLayer)) {
				currentStreams.values().removeAll(currentlyTransferedStreams.get(ba));
				currentlyTransferedStreams.remove(ba);
			}
		}
	}
	
	
	
	/**
	 * 
	 * @param msg
	 */
	public void cancelTransmission(int commId) {

		if (bandwidthManager!= null) {

			GnpNetLayer sender = layers.get(currentStreams.get(commId).getMessage().getSender());
			GnpNetLayer receiver = layers.get(currentStreams.get(commId).getMessage().getReceiver());
			
			// remove message from current transfers
			double maximumRequiredBandwidth = sender.getMaxUploadBandwidth();
			if (currentStreams.get(commId).getMessage().getPayload() instanceof TCPMessage) {
				double tcpThroughput = netLatencyModel.getTcpThroughput(sender, receiver);
				maximumRequiredBandwidth = Math.min(maximumRequiredBandwidth, tcpThroughput);
			}
			bandwidthManager.removeConnection(sender, receiver, maximumRequiredBandwidth);
			
			TransferProgress tp = currentStreams.get(commId);
			
			obsoleteEvents.add(tp);
			
			// Reschedule messages after current timeunit
			long currentTime = Simulator.getCurrentTime();
			if (bandwidthManager.getBandwidthAllocationType() == BandwidthAllocation.EVENT && nextResheduleTime <= currentTime + 1) {	
				nextResheduleTime = currentTime + 1;
				Simulator.scheduleEvent(REALLOCATE_BANDWIDTH_EVENTBASED_EVENT, nextResheduleTime, this, SimulationEvent.Type.MESSAGE_RECEIVED);
			}		
		}
	}
	

	/**
	 * Processes a SimulationEvent. (Is called by the Scheduler.)
	 * 
	 * @param se
	 *            SimulationEvent that is returned by the Scheduler.
	 */
	public void eventOccurred(SimulationEvent se) {

		long currentTime = Simulator.getCurrentTime();

		/*
		 * 
		 */
		if (se.getData() == REALLOCATE_BANDWIDTH_PERIODICAL_EVENT) {

			log.debug("PBA Event at " + Simulator.getSimulatedRealtime());

			nextPbaTime = Simulator.getCurrentTime() + pbaPeriod;
			bandwidthManager.allocateBandwidth();
			Set<GnpNetBandwidthAllocation> delete = new HashSet<GnpNetBandwidthAllocation>();
			for (GnpNetBandwidthAllocation ba : currentlyTransferedStreams.keySet()) {
				reschedulePeriodical(ba);
				if (currentlyTransferedStreams.get(ba).isEmpty())
					delete.add(ba);
			}
			currentlyTransferedStreams.keySet().removeAll(delete);
				
			// Schedule next Periodic Event
			if (currentlyTransferedStreams.size() > 0)
				Simulator.scheduleEvent(REALLOCATE_BANDWIDTH_PERIODICAL_EVENT, nextPbaTime, this, SimulationEvent.Type.MESSAGE_RECEIVED);			
			else
				nextPbaTime = 0;
		} 

		/*
		 * 
		 */
		else if (se.getData() == REALLOCATE_BANDWIDTH_EVENTBASED_EVENT) {
			bandwidthManager.allocateBandwidth();
			Set<GnpNetBandwidthAllocation> bas = bandwidthManager.getChangedAllocations();
			for (GnpNetBandwidthAllocation ba : bas)
				rescheduleEventBased(ba);
		}

		/*
		 * 
		 */
		else if (se.getType() == SimulationEvent.Type.MESSAGE_RECEIVED) {

			TransferProgress tp = (TransferProgress) se.getData();
			IPv4Message msg = (IPv4Message) tp.getMessage();
			GnpNetLayer sender = this.layers.get(msg.getSender());
			GnpNetLayer receiver = this.layers.get(msg.getReceiver());
			

			// Case 1: message only consists of 1 Segment => no bandwidth
			// allocation
			if (msg.getNoOfFragments() == 1) {
				receiver.addToReceiveQueue(msg);
			}

			// Case 2: message consists minimum 2 Segments => bandwidth
			// allocation
			else {

				// Case 2a: Periodical Bandwidth Allocation
				// Schedule the first Periodical Bandwidth Allocation Event
				if (bandwidthManager.getBandwidthAllocationType() == BandwidthAllocation.PERIODICAL) {
					receiver.receive(msg);
				}

				// Case 2b: Eventbased Bandwidth Allocation
				// Schedule an realocation Event after current timeunit
				else if (bandwidthManager.getBandwidthAllocationType() == BandwidthAllocation.EVENT) {
					// Dropp obsolete Events
					if (obsoleteEvents.contains(tp)) {
						obsoleteEvents.remove(tp);
						return;
					} else {
						receiver.receive(msg);
						// Reschedule messages after current timeunit
						if (nextResheduleTime <= currentTime + 1) {
							nextResheduleTime = currentTime + 1;
							Simulator.scheduleEvent(
									REALLOCATE_BANDWIDTH_EVENTBASED_EVENT,
									nextResheduleTime, this,
									SimulationEvent.Type.MESSAGE_RECEIVED);
						}
					}
				}

				// remove message from current transfers
				double maximumRequiredBandwidth = sender.getMaxUploadBandwidth();
				if (msg.getPayload() instanceof TCPMessage) {
					double tcpThroughput = netLatencyModel.getTcpThroughput(sender, receiver);
					maximumRequiredBandwidth = Math.min(maximumRequiredBandwidth, tcpThroughput);
				}
				GnpNetBandwidthAllocation ba = bandwidthManager.removeConnection(sender, receiver, maximumRequiredBandwidth);
				if (bandwidthManager.getBandwidthAllocationType() == BandwidthAllocation.EVENT) {
					if (currentlyTransferedStreams.get(ba) != null) {
						if (currentlyTransferedStreams.get(ba).size() == 1) {
							currentlyTransferedStreams.remove(ba);
						} else {
							currentlyTransferedStreams.get(ba).remove(tp);
						}
					} else {
						currentlyTransferedStreams.remove(ba);
					}
					currentStreams.values().remove(tp);

				} else {
					if (currentlyTransferedStreams.get(ba) != null && currentlyTransferedStreams.get(ba).isEmpty()) {
						currentlyTransferedStreams.remove(ba);			
					}
				}
				
			}
		}
	}

	/**
	 * ToDo
	 * 
	 * @param ba
	 */
	private void reschedulePeriodical(GnpNetBandwidthAllocation ba) {
		Set<TransferProgress> oldIncomplete = currentlyTransferedStreams.get(ba);
		Set<TransferProgress> newIncomplete = new HashSet<TransferProgress>(oldIncomplete.size());
		GnpNetLayer sender = (GnpNetLayer) ba.getSender();
		GnpNetLayer receiver = (GnpNetLayer) ba.getReceiver();
		long currentTime = Simulator.getCurrentTime();
		double leftBandwidth = ba.getAllocatedBandwidth();
		
		
		Set<TransferProgress> temp = new HashSet<TransferProgress>();
		temp.addAll(oldIncomplete);
		
		oldIncomplete.removeAll(obsoleteEvents);
		obsoleteEvents.removeAll(temp);
		int leftStreams = oldIncomplete.size();

		
		for (TransferProgress tp : oldIncomplete) {
		
			double remainingBytes = tp.getRemainingBytes(currentTime);
			double bandwidth = leftBandwidth / leftStreams;
			if (tp.getMessage().getPayload() instanceof TCPMessage) {
				double throughput = netLatencyModel.getTcpThroughput(sender, receiver);
				if (throughput <  bandwidth)
					bandwidth = throughput;
			}
			leftBandwidth -= bandwidth;
			leftStreams--;
			long transmissionTime = this.netLatencyModel.getTransmissionDelay(remainingBytes, bandwidth);
			TransferProgress newTp = new TransferProgress(tp.getMessage(), bandwidth, remainingBytes, currentTime);
			
			if (currentTime + transmissionTime < nextPbaTime) {
				long propagationTime = this.netLatencyModel.getPropagationDelay(sender, receiver);
				long arrivalTime = currentTime + transmissionTime + propagationTime;
				Simulator.scheduleEvent(newTp, arrivalTime, this, SimulationEvent.Type.MESSAGE_RECEIVED);
			} else {
				newIncomplete.add(newTp);
				int commId = ((AbstractTransMessage)tp.getMessage().getPayload()).getCommId();
				currentStreams.put(commId, newTp);
			}			
		}
		currentlyTransferedStreams.put(ba, newIncomplete);
	}

	/**
	 * ToDo
	 * 
	 * @param ba
	 */
	private void rescheduleEventBased(GnpNetBandwidthAllocation ba) {
		Set<TransferProgress> oldIncomplete = currentlyTransferedStreams.get(ba);
		if (oldIncomplete == null) {
			return;
		}
		Set<TransferProgress> newIncomplete = new HashSet<TransferProgress>(oldIncomplete.size());
		GnpNetLayer sender = (GnpNetLayer) ba.getSender();
		GnpNetLayer receiver = (GnpNetLayer) ba.getReceiver();
		long currentTime = Simulator.getCurrentTime();
		double leftBandwidth = ba.getAllocatedBandwidth();
		oldIncomplete.removeAll(obsoleteEvents);
		int leftStreams = oldIncomplete.size();
		
		for (TransferProgress tp : oldIncomplete) {
			double remainingBytes = tp.getRemainingBytes(currentTime);
			
			double bandwidth = leftBandwidth / leftStreams;
			if (tp.getMessage().getPayload() instanceof TCPMessage) {
				double throughput = netLatencyModel.getTcpThroughput(sender, receiver);
				if (throughput <  bandwidth)
					bandwidth = throughput;
			}
			leftBandwidth -= bandwidth;
			leftStreams--;
			
			long transmissionTime = this.netLatencyModel.getTransmissionDelay(remainingBytes, bandwidth);
			long propagationTime = this.netLatencyModel.getPropagationDelay(sender, receiver);
			long arrivalTime = currentTime + transmissionTime + propagationTime;
			
			TransferProgress newTp = new TransferProgress(tp.getMessage(),bandwidth, remainingBytes, currentTime);
			Simulator.scheduleEvent(newTp, arrivalTime, this,SimulationEvent.Type.MESSAGE_RECEIVED);
			newIncomplete.add(newTp);
			int commId = ((AbstractTransMessage)tp.getMessage().getPayload()).getCommId();
			currentStreams.put(commId, newTp);
		}
		currentlyTransferedStreams.put(ba, newIncomplete);
		obsoleteEvents.addAll(oldIncomplete);
	}

}