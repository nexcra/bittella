package de.tud.kom.p2psim.impl.network.gnp.topology;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

public class GnpHostGroup implements Serializable {

	private GnpHostMap mapRef;

	private String groupName;

	private HashSet<GnpHost> hosts;

	public GnpHostGroup(GnpHostMap mapRef) {
		super();
		this.mapRef = mapRef;
		hosts = new HashSet<GnpHost>();
		this.groupName = super.toString();
	}

	public GnpHostGroup(GnpHostMap mapRef, String name) {
		this(mapRef);
		this.groupName = name;
	}

	public void addPeer(GnpHost peer) {
		hosts.add(peer);
	}

	public void addPeer(ArrayList<GnpHost> peer) {
		hosts.addAll(peer);
	}

	public void addPeer(Hashtable<Long, GnpHost> peer) {
		Set<Long> ips = peer.keySet();
		for (Long ip : ips)
			this.addPeer(peer.get(ip));
	}

	public void removePeer(GnpHost peer) {
		hosts.remove(peer);
	}

	public void clearPeer() {
		hosts.clear();
	}

	public boolean containsPeer(GnpHost peer) {
		return hosts.contains(peer);
	}

	public int numberOfPeers() {
		return hosts.size();
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String toString() {
		return groupName;
	}

	public String getIpString() {

		Object[] peerArray = hosts.toArray();
		String result = String.valueOf(((GnpHost) peerArray[0]).getIpAddress());

		for (int c = 1; c < peerArray.length; c++) {
			result = result + "," + String.valueOf(((GnpHost) peerArray[c]).getIpAddress());
		}

		return result;

	}

	public HashSet<GnpHost> getPeers() {
		return hosts;
	}

	public void updateGroup() {

		HashSet<GnpHost> delete = new HashSet<GnpHost>();

		for (GnpHost host : hosts) {
			if (!mapRef.getPeerIndex().containsKey(host.getIpAddress()))
				delete.add(host);
		}

		for (GnpHost host : delete) {
			hosts.remove(host);
		}

	}
}
