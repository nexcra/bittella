package uc3m.netcom.overlay.bt;



import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Logger;

import de.tud.kom.p2psim.api.common.Operation;
import de.tud.kom.p2psim.api.common.OperationCallback;
import de.tud.kom.p2psim.api.overlay.OverlayKey;
import de.tud.kom.p2psim.api.storage.ContentStorage;
import de.tud.kom.p2psim.api.storage.Document;
import de.tud.kom.p2psim.impl.application.AbstractApplication;
import de.tud.kom.p2psim.impl.util.logging.SimLogger;
import de.tud.kom.p2psim.overlay.bt.operation.BTOperationDistribute;



/**
 * This class represents the client application and is the top most class of the client.
 * It has two <code>OverlayNode</code>s: 
 * The <code>BTPeerSearchNode</code> and the <code>BTPeerDistributeNode</code>.
 * This class contains methods for starting a download, making this client to a seed
 * and accessing its databus.
 * @author Jan Stolzenburg
 */
public class BTClientApplication extends AbstractApplication {
	
	
	
	/**
	 * This <code>OverlayNode</code> is used for the contact to the tracker.
	 * "SearchNode" means: Searching for other peers (via the tracker).
	 */
	private BTPeerSearchNode itsSearchNode;
	
	/**
	 * This node is used for the contact to other peers.
	 * "DistributeNode" means, this node is responsible for distributing the content of the torrents.
	 * It handles upload and download of the content.
	 */
	private BTPeerDistributeNode itsDistributionNode;
	
	/**
	 * This class just stores all documents.
	 */
	private ContentStorage itsDocumentStorage;
	
//	private Collection<ApplicationEventHandler> itsApplicationEventHandler;
	
	/**
	 * All currently running distribute operations.
	 * If the clients is closed, we stop them over this list.
	 */
	private Map<OverlayKey, BTOperationDistribute<BTClientApplication>> itsDistributeOperations;
	
	/**
	 * The databus is a large storage for every kind of information
	 * that is created in one part of the client and
	 * can be reused by other parts of the client.
	 */
	private BTDataStore itsDataBus;
	
	static final Logger log = SimLogger.getLogger(BTClientApplication.class);
	
	
	
	public BTClientApplication(BTDataStore theDataBus, BTPeerSearchNode theSearchNode, BTPeerDistributeNode theDownloadNode) {
		super();
		this.itsDataBus = theDataBus;
		this.itsSearchNode = theSearchNode;
//		this.itsSearchNode.registerEventHandler(this);
		this.itsDistributionNode = theDownloadNode;
//		this.itsDistributionNode.registerEventHandler(this);
//		this.itsApplicationEventHandler = new LinkedList<ApplicationEventHandler>();
		this.itsDistributeOperations = new HashMap<OverlayKey, BTOperationDistribute<BTClientApplication>>();
	}
	
//	/**
//	 * @see de.tud.kom.p2psim.api.application.Application#registerEventHandler(de.tud.kom.p2psim.api.application.ApplicationEventHandler)
//	 */
//	public void registerEventHandler(ApplicationEventHandler handler) {
//		this.itsApplicationEventHandler.add(handler);
//	}
	
//	/**
//	 * @see de.tud.kom.p2psim.api.overlay.OverlayEventHandler#eventOccurred(de.tud.kom.p2psim.api.overlay.OverlayEvent)
//	 */
//	public void eventOccurred(OverlayEvent theOverlayEvent) {
//		log.debug("Received an event: '" + theOverlayEvent.toString() + "'; Type: '" + theOverlayEvent.getType() + "'; Data: '" + theOverlayEvent.getData() + "'.");
//	}
	
	/**
	 * This method will start a download.
	 * You can start any number of parallel downloads. Every download will use the same <code>BTPeerDistributionNode</code>.
	 * It starts a <code>BTOperationDistribute<code>, that will start all neccessary operations for upload, download and so on.
	 * @param theTorrent The torrent object for the file you want to download.
	 */
	@SuppressWarnings("unchecked")
	public void downloadDocument(BTTorrent theTorrent) {
		if (this.itsDistributeOperations.containsKey(theTorrent.getKey()) && ! this.itsDistributeOperations.get(theTorrent.getKey()).isFinished())
			throw new RuntimeException("This document is already beeing downloaded.");
		BTOperationDistribute<BTClientApplication> distributeOperation = new BTOperationDistribute<BTClientApplication>(this.itsDataBus, theTorrent, this.itsSearchNode, this.itsDistributionNode, this, this);
		this.itsDistributeOperations.put(theTorrent.getKey(), distributeOperation);
		distributeOperation.scheduleImmediately();
	}
	
	/**
	 * Returns the list of documents this client know. The download state doesn't matter.
	 * @return the list of document this client knows.
	 */
	public Collection<BTDocument> listDocuments() {
		Collection<BTDocument> result = new LinkedList<BTDocument>();
		for (Document aDocument : this.itsDocumentStorage.listDocuments()) {
			result.add((BTDocument)aDocument);
		}
		return result;
	}
	
	/**
	 * Use this method if you want to create a seed.
	 * This method stores the document. It keeps the state of it.
	 * If this client should be a seed, you have to set the download state of the document to "completed" or something alike.
	 * @param theDocument
	 */
	public void storeDocument(BTDocument theDocument) {
		this.itsDocumentStorage.storeDocument(theDocument);
		if (theDocument.getDocumentState() == 1) { //We are the seed!
			if (! this.itsDataBus.isTorrentKnown(theDocument.getKey()))
				this.itsDataBus.addTorrent(theDocument.getKey());
			this.itsDataBus.storePerTorrentData(theDocument.getKey(), "Seed", true, (new Boolean(true)).getClass());
		}
	}
	
	public void connect() {
		this.itsDocumentStorage = this.getHost().getStorage();
		this.itsSearchNode.connect();
		this.itsDistributionNode.connect();
	}
	
//	protected void notifyEvent(ApplicationEvent event) {
//		log.debug("notify " + this.itsApplicationEventHandler + " of event " + event);
//		for (ApplicationEventHandler handler : this.itsApplicationEventHandler) {
//			handler.eventOccurred(event);
//		}
//	}
	
	/**
	 * Use this method, if you want to access the databus from outside the client.
	 * For example, if you want to manipulate some internals.
	 * But please: Be carefull!
	 * @return the data bus of this client.
	 */
	public BTDataStore getDataBus() {
		return this.itsDataBus;
	}

	public Operation createOperation(String opName, String[] params, OperationCallback caller) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Method 'createOperation' in class 'BTClientApplication' not yet implemented!");
		//return null;
	}
	
}
