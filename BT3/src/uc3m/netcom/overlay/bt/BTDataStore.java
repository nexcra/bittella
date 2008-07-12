package uc3m.netcom.overlay.bt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * This class can be used for storing all kinds of data.
 * Currently it is used for storing data that are used in different classes in the BitTorrent implementation.
 * It additionaly stores the classes of the stored data. This can be used for ensuring data safety when casting.
 * @author Jan Stolzenburg
 */
@SuppressWarnings("unchecked")
public class BTDataStore {
	
	/**
	 * Stores data, that don't belong to a certain torrent.
	 */
	private Map<String, Object> itsGeneralData;
	
	/**
	 * Stores the classes of the objects saved in <code>itsGeneralData</code>.
	 */
	private Map<String, Class> itsGeneralDataClasses;
	
	/**
	 * Stores data that belong to a certain torrent.
	 */
	private Map<String, Map<String, Object>> itsPerTorrentData;
	
	/**
	 * Stores the classes of the objects saved in <code>itsPerTorrentData</code>.
	 */
	private Map<String, Map<String, Class>> itsPerTorrentDataClasses;
	
	/**
	 * Contains a list of all the peers that we know for the given torrent.
	 */
	private Map<String, Set<BTContact>> itsPeersPerTorrent;
	
	/**
	 * Lists for every peer, which torrent it is associated with.
	 */
	private Map<BTContact, String> itsTorrentOfAPeer;
	
	/**
	 * Stores data that belong to a certain torrent.
	 */
	private Map<BTContact, Map<String, Object>> itsPerPeerData;
	
	/**
	 * Stores the classes of the objects saved in <code>itsPerTorrentData</code>.
	 */
	private Map<BTContact, Map<String, Class>> itsPerPeerDataClasses;
	
	public BTDataStore() {
		this.itsGeneralData = new HashMap<String, Object>();
		this.itsGeneralDataClasses = new HashMap<String, Class>();
		this.itsPerTorrentData = new HashMap<String, Map<String, Object>>();
		this.itsPerTorrentDataClasses = new HashMap<String, Map<String, Class>>();
		this.itsPeersPerTorrent = new HashMap<String, Set<BTContact>>();
		this.itsTorrentOfAPeer = new HashMap<BTContact, String>();
		this.itsPerPeerData = new HashMap<BTContact, Map<String, Object>>();
		this.itsPerPeerDataClasses = new HashMap<BTContact, Map<String, Class>>();
	}
	
	public void storeGeneralData(String theKey, Object theData, Class theDataClass) {
		if (! theDataClass.isInstance(theData))
			throw new RuntimeException("Tried to store a false type information!");
		this.itsGeneralData.put(theKey, theData);
		this.itsGeneralDataClasses.put(theKey, theDataClass);
	}
	
	public void removeGeneralData(String theKey) {
		this.itsGeneralData.remove(theKey);
		this.itsGeneralDataClasses.remove(theKey);
	}
	
	public boolean isGeneralDataStored(String theKey) {
		return this.itsGeneralData.containsKey(theKey);
	}
	
	public Object getGeneralData(String theKey) {
		return this.itsGeneralData.get(theKey);
	}
	
	public Class getGeneralDataClass(String theKey) {
		return this.itsGeneralDataClasses.get(theKey);
	}
	
	public void addTorrent(String theTorrentKey) {
		if (this.itsPerTorrentData.containsKey(theTorrentKey))
			throw new RuntimeException("Tried to add an already stored torrent.");
		this.itsPerTorrentData.put(theTorrentKey, new HashMap<String, Object>());
		this.itsPerTorrentDataClasses.put(theTorrentKey, new HashMap<String, Class>());
		this.itsPeersPerTorrent.put(theTorrentKey, new HashSet<BTContact>());
	}
	
	public void removeTorrent(String theTorrentKey) {
		this.itsPerTorrentData.remove(theTorrentKey);
		this.itsPerTorrentDataClasses.remove(theTorrentKey);
		this.itsPeersPerTorrent.remove(theTorrentKey);
	}
	
	public boolean isTorrentKnown(String theTorrentKey) {
		return this.itsPerTorrentData.containsKey(theTorrentKey);
	}
	
	public List<String> getListOfTorrents() {
		return new LinkedList<String>(this.itsPerTorrentData.keySet());
	}
	
	public void storePerTorrentData(String theTorrentKey, String theKey, Object theData, Class theDataClass) {
		if (! theDataClass.isInstance(theData))
			throw new RuntimeException("Tried to store a false type information!");
		this.itsPerTorrentData.get(theTorrentKey).put(theKey, theData);
		this.itsPerTorrentDataClasses.get(theTorrentKey).put(theKey, theDataClass);
	}
	
	public void removePerTorrentData(String theTorrentKey, String theKey) {
		this.itsPerTorrentData.get(theTorrentKey).remove(theKey);
		this.itsPerTorrentDataClasses.get(theTorrentKey).remove(theKey);
	}
	
	public boolean isPerTorrentDataStored(String theTorrentKey, String theKey) {
		return this.itsPerTorrentData.get(theTorrentKey).containsKey(theKey);
	}
	
	public Object getPerTorrentData(String theTorrentKey, String theKey) {
		return this.itsPerTorrentData.get(theTorrentKey).get(theKey);
	}
	
	public Class getPerTorrentDataClass(String theTorrentKey, String theKey) {
		return this.itsPerTorrentDataClasses.get(theTorrentKey).get(theKey);
	}
	
	public void storePeer(String theTorrentKey, BTContact thePeer) {
		if (this.itsTorrentOfAPeer.containsKey(thePeer)) {
			if (this.itsTorrentOfAPeer.get(thePeer).equals(theTorrentKey))
				return;
			else
				throw new RuntimeException("Tried to store a peer for a second torrent.");
		}
		this.itsPeersPerTorrent.get(theTorrentKey).add(thePeer);
		this.itsTorrentOfAPeer.put(thePeer, theTorrentKey);
		this.itsPerPeerData.put(thePeer, new HashMap<String, Object>());
		this.itsPerPeerDataClasses.put(thePeer, new HashMap<String, Class>());
	}
	
	public void removePeer(String theTorrentKey, BTContact thePeer) {
		if (! this.itsTorrentOfAPeer.get(thePeer).equals(theTorrentKey))
			throw new RuntimeException("Tried to remove a peer for a false torrent.");
		this.itsPeersPerTorrent.get(theTorrentKey).remove(thePeer);
		this.itsTorrentOfAPeer.remove(thePeer);
		this.itsPerPeerData.remove(thePeer);
		this.itsPerPeerDataClasses.remove(thePeer);
	}
	
	public boolean isPeerKnown(BTContact thePeer) {
		return this.itsTorrentOfAPeer.containsKey(thePeer);
	}
	
	public String getTorrentOfPeer(BTContact thePeer) {
		return this.itsTorrentOfAPeer.get(thePeer);
	}
	
	public List<BTContact> getListOfPeersForTorrent(String theTorrentKey) {
		return new LinkedList<BTContact>(this.itsPeersPerTorrent.get(theTorrentKey));
	}
	
	public List<BTContact> getListOfAllPeer() {
		return new LinkedList<BTContact>(this.itsTorrentOfAPeer.keySet());
	}
	
	public void storePerPeerData(BTContact thePeer, String theKey, Object theData, Class theDataClass) {
		if (! theDataClass.isInstance(theData))
			throw new RuntimeException("Tried to store a false type information!");
		this.itsPerPeerData.get(thePeer).put(theKey, theData);
		this.itsPerPeerDataClasses.get(thePeer).put(theKey, theDataClass);
	}
	
	public void removePerPeerData(BTContact thePeer, String theKey) {
		this.itsPerPeerData.get(thePeer).remove(theKey);
		this.itsPerPeerDataClasses.get(thePeer).remove(theKey);
	}
	
	public boolean isPerPeerDataStored(BTContact thePeer, String theKey) {
		return this.itsPerPeerData.get(thePeer).containsKey(theKey);
	}
	
	public Object getPerPeerData(BTContact thePeer, String theKey) {
		return this.itsPerPeerData.get(thePeer).get(theKey);
	}
	
	public Class getPerPeerDataClass(BTContact thePeer, String theKey) {
		return this.itsPerPeerDataClasses.get(thePeer).get(theKey);
	}
	
}
