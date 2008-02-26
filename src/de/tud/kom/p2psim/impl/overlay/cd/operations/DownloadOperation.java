package de.tud.kom.p2psim.impl.overlay.cd.operations;

import java.util.List;

import org.apache.log4j.Logger;

import de.tud.kom.p2psim.api.common.Message;
import de.tud.kom.p2psim.api.common.OperationCallback;
import de.tud.kom.p2psim.api.overlay.OverlayKey;
import de.tud.kom.p2psim.api.storage.Document;
import de.tud.kom.p2psim.api.transport.TransInfo;
import de.tud.kom.p2psim.api.transport.TransMessageCallback;
import de.tud.kom.p2psim.api.transport.TransProtocol;
import de.tud.kom.p2psim.impl.common.AbstractOperation;
import de.tud.kom.p2psim.impl.overlay.cd.DistributionStrategyImpl;
import de.tud.kom.p2psim.impl.overlay.cd.messages.DownloadRequestMsg;
import de.tud.kom.p2psim.impl.overlay.cd.messages.DownloadResultMsg;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.util.logging.SimLogger;

public class DownloadOperation extends AbstractOperation<DistributionStrategyImpl, Document> implements TransMessageCallback {

	static final Logger log = SimLogger.getLogger(DownloadOperation.class);

	List<TransInfo> peers;

	OverlayKey key;

	private DistributionStrategyImpl distStrategy;

	/**
	 * Document to be downloaded.
	 */
	private Document document;

	public DownloadOperation(DistributionStrategyImpl distStrategy, List<TransInfo> peers, OverlayKey key, short port, OperationCallback callback) {
		super(distStrategy, callback);
		this.peers = peers;
		this.key = key;
		this.distStrategy = distStrategy;
	}

	@Override
	public void execute() {
		assert peers != null;
		assert !peers.isEmpty();
		TransInfo peer = peers.get(0);// TODO try next peers if download
		// fails?
		DownloadRequestMsg downloadRequest = new DownloadRequestMsg(key);
		log.info("Try to download " + key + " from " + peer);
		distStrategy.getTransLayer().sendAndWait(downloadRequest, peer, distStrategy.getPort(), TransProtocol.UDP, this, 2 * Simulator.SECOND_UNIT);
	}

	@Override
	protected void operationTimeoutOccured() {
		operationFinished(false);
	}

	public void messageTimeoutOccured(int commId) {
		operationFinished(false);
	}

	public void receive(Message msg, TransInfo senderAddr, int commId) {
		if (msg instanceof DownloadResultMsg) {
			DownloadResultMsg res = (DownloadResultMsg) msg;
			log.debug("received reply for key " + res.getDoc().getKey());
			document = res.getDoc();
			distStrategy.getStorage().storeDocument(document);
			log.info("RECEIVED REQUESTED DOCUMENT " + document);
			operationFinished(true);
		} else {
			log.warn("Unknown msg received " + msg + " wait for DownloadResutl");
		}
	}

	@Override
	public Document getResult() {
		return document;
	}

}
