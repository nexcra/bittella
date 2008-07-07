package uc3m.netcom.peerfactsim.overlay.bt.operation;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.math.random.RandomGenerator;

import de.tud.kom.p2psim.api.common.OperationCallback;
import de.tud.kom.p2psim.api.overlay.DistributionStrategy;
import de.tud.kom.p2psim.api.transport.TransLayer;
import de.tud.kom.p2psim.overlay.bt.BTConstants;
import de.tud.kom.p2psim.overlay.bt.BTContact;
import de.tud.kom.p2psim.overlay.bt.BTDataStore;
import de.tud.kom.p2psim.overlay.bt.BTDocument;
import de.tud.kom.p2psim.overlay.bt.BTInternStatistic;
import de.tud.kom.p2psim.overlay.bt.manager.BTConnectionManager;
import de.tud.kom.p2psim.overlay.bt.message.BTMessage;
import de.tud.kom.p2psim.overlay.bt.message.BTPeerMessageChoke;
import de.tud.kom.p2psim.overlay.bt.message.BTPeerMessageUnchoke;
import de.tud.kom.p2psim.overlay.bt.operation.BTOperation;

import uc3m.netcom.peerfactsim.overlay.bt.algorithm.BTAlgorithmChoking;

/**
 * This class takes care about the choking algorithm.
 * It gets regularly called, calls the choking algorithm and
 * tells the peers if they got (un)choked.
 * @param <OwnerType> the class of the component that owns this operation
 * @author Jan Stolzenburg
 */
public class BTOperationChoking<OwnerType extends DistributionStrategy> extends BTOperation<OwnerType, Void> {
	
	private BTDocument itsDocument;
	
	private BTInternStatistic itsStatistic;
	
	private RandomGenerator itsRandomGenerator;
	
	private BTAlgorithmChoking itsAlgorithm;
	
	private BTConnectionManager itsConnectionManager;
	
	private TransLayer itsTransportLayer;
	
	private BTDataStore itsDataBus;
	
	private final static long theirPeriod = BTConstants.CHOKING_REGULAR_CHOKING_RECALC_PERIOD;
	
	public BTOperationChoking(BTDataStore theDataBus, BTDocument theDocument, OwnerType theOwningComponent, OperationCallback<Void> theOperationCallback, BTInternStatistic theStatistic, RandomGenerator theRandomGenerator, BTConnectionManager theConnectionManager) {
		super(theOwningComponent, theOperationCallback);
		this.itsDataBus = theDataBus;
		this.itsDocument = theDocument;
		this.itsStatistic = theStatistic;
		this.itsRandomGenerator = theRandomGenerator;
		this.itsConnectionManager = theConnectionManager;
		this.itsTransportLayer = this.getComponent().getHost().getTransLayer();
		this.itsAlgorithm = new BTAlgorithmChoking();
		this.itsAlgorithm.setup(this.itsDocument, this.itsConnectionManager, this.itsStatistic, this.itsRandomGenerator);
	}
	
	@Override
	protected void execute() {
		if (this.isFinished())
			return;
		if (this.uploadFinished()) {
			this.operationFinished(true);
			return;
		}
		this.scheduleWithDelay(theirPeriod);
		this.recalc();
	}
	
	/**
	 * @return Has the upload finished?
	 */
	private boolean uploadFinished() {
		if (! this.itsDataBus.isPerTorrentDataStored(this.itsDocument.getKey(), "Upload Stopped"))
			return false;
		return (Boolean) this.itsDataBus.getPerTorrentData(this.itsDocument.getKey(), "Upload Stopped");
	}
	
	
//	private boolean amISeed() {
//		return ((Map<OverlayKey, Map<String, Object>>)this.itsDataBus.get("Torrents")).get(this.itsDocument.getKey()).containsKey("Seed") && (Boolean)((Map<OverlayKey, Map<String, Object>>)this.itsDataBus.get("Torrents")).get(this.itsDocument.getKey()).get("Seed");
//	}
	
	/**
	 * Recalc the choking for all peers.
	 * For those peers that were (un)choked, send the appropriate message to them.
	 */
	public void recalc() {
		Collection<BTContact> interesstedPeers = new LinkedList<BTContact>();
		for( BTContact anOtherPeer : this.itsConnectionManager.getConnectedContacts()) {
			if (this.itsConnectionManager.getConnection(anOtherPeer).isInterestedInMe())
				interesstedPeers.add(anOtherPeer);
		}
		
		//Check for which peers the choking state changed.
		Collection<BTContact> winner = new LinkedList<BTContact>(interesstedPeers);
		this.itsAlgorithm.filterUploadContacts(winner);
		for (BTContact anOtherPeer : interesstedPeers) {
			boolean choking = ! winner.contains(anOtherPeer);
			if (choking == this.itsConnectionManager.getConnection(anOtherPeer).amIChoking())
				continue; //Nothing changed, nothing to do for this peer.
			if (choking)
				this.sendChokingMessage(anOtherPeer);
			else
				this.sendUnchokingMessage(anOtherPeer);
			this.itsConnectionManager.getConnection(anOtherPeer).setChoking(choking); //We just switch the choke state. If it was choked before, we unchoke it. If it was not choked, we choke it.
		}
	}
	
	private void sendChokingMessage(BTContact theOtherPeer) {
		BTMessage chokeMessage = new BTPeerMessageChoke(this.itsDocument.getKey(), this.itsConnectionManager.getLocalAddress().getOverlayID(), theOtherPeer.getOverlayID());
//		this.itsConnectionManager.getConnection(theOtherPeer).addMessage(chokeMessage);
		this.itsTransportLayer.send(chokeMessage, theOtherPeer.getTransInfo(), this.itsConnectionManager.getLocalAddress().getTransInfo().getPort(), BTPeerMessageChoke.getStaticTransportProtocol());
	}
	
	private void sendUnchokingMessage(BTContact theOtherPeer) {
		BTMessage chokeMessage = new BTPeerMessageUnchoke(this.itsDocument.getKey(), this.itsConnectionManager.getLocalAddress().getOverlayID(), theOtherPeer.getOverlayID());
//		this.itsConnectionManager.getConnection(theOtherPeer).addMessage(chokeMessage);
		this.itsTransportLayer.send(chokeMessage, theOtherPeer.getTransInfo(), this.itsConnectionManager.getLocalAddress().getTransInfo().getPort(), BTPeerMessageUnchoke.getStaticTransportProtocol());
	}
	
	@Override
	public Void getResult() {
		return null;
	}
	
	@Override
	protected void operationTimeoutOccured() {
		this.operationFinished(false);
	}
	
       protected BTAlgorithmChoking getAlgorithm(){
            return this.itsAlgorithm;
        }       

}
