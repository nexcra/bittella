package de.tud.kom.p2psim.impl.network.gnp.topology;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

public class GnpHost implements Serializable {

	private static final long serialVersionUID = 5135831087205329917L;

	public static int MONITOR = 1;

	public static int PEER = 2;

	// public static int PEER_INTERPOLATE_OLD = 3; // Monitore für interpolierte
	// Knoten können auch Peers sein

	private int type;

	private int status;

	private Long ipAddress = new Long(0);

	private double latitude; // Breite / Y

	private double longitude; // Länge / X

	private boolean locatable = false;

	private String country = "--";

	private String name;

	// private Hashtable<Long,RttDistribution> rttToMonitors;
	private ArrayList<RttDistribution> rttToMonitors2;

	// private GnpCoordinate coordinateRef ;
	// private double[] measuredRtt;
	// private Long[] measuredRttMonitorIP;
	// private int[] measuredRttCounter;
	// ToDo private double[] varianceDistanceToMonitor;

	private GnpHostMap mapRef;

	private GnpPosition coordinateRef;

	// private int monitorID = -1;
	// public int monitorConnections = 0;
	private double distance;

	public String getName() {
		if (name == null)
			return super.toString();
		else
			return name;
	}

	public void setHostName(String name) {
		this.name = name;
	}

	public GnpHost(Long ipAddress, GnpHostMap mapRef) {
		super();
		this.ipAddress = ipAddress;
		this.mapRef = mapRef;
		this.rttToMonitors2 = new ArrayList<RttDistribution>(20);
	}

	public int getGridPosLongitude(int numberOfGridParts) {
		double step = 360 / (double) numberOfGridParts;
		return (int) Math.floor((180 + this.getLongitude()) / step);
	}

	public int getGridPosLatitude(int numberOfGridParts) {
		double step = 180 / (double) numberOfGridParts;
		return (int) Math.floor((90 + this.getLatitude()) / step);
	}

	// ////////////////
	// Get-Methods

	public void setRttDistribution(Long monitorIP, RttDistribution distribution) {
		Integer monitorID = mapRef.getMonitorID(monitorIP);
		while (rttToMonitors2.size() <= monitorID)
			rttToMonitors2.add(new RttDistribution());
		rttToMonitors2.set(monitorID, distribution);
	}

	RttDistribution getRttDistribution(Long monitorIP) {
		Integer monitorID = mapRef.getMonitorID(monitorIP);
		while (rttToMonitors2.size() <= monitorID)
			rttToMonitors2.add(new RttDistribution());
		return rttToMonitors2.get(monitorID);
	}

	public double getRtt(Long monitorIP) {
		return getRttDistribution(monitorIP).getMinRTT();
	}

	public void removeRTT(long monitorIP) {
		Integer monitorID = mapRef.getMonitorID(monitorIP);
		if (rttToMonitors2.size() > monitorID)
			this.rttToMonitors2.set(monitorID, null);
	}

	public boolean isConnectedToMonitor(long monitorIP) {
		Integer monitorID = mapRef.getMonitorID(monitorIP);
		if (rttToMonitors2.size() <= monitorID)
			return false;
		else if (rttToMonitors2.get(monitorID) == null)
			return false;
		else if (Double.compare(rttToMonitors2.get(monitorID).getMinRTT(), Double.NaN) == 0)
			return false;
		else
			return true;
	}

	public ArrayList<GnpHost> getConnectedMonitors() {
		ArrayList<GnpHost> monitors = new ArrayList<GnpHost>();
		Hashtable<Long, GnpHost> monitorIndex = mapRef.getMonitorIndex();
		Set<Long> ips = monitorIndex.keySet();
		for (Long ip : ips) {
			if (this.isConnectedToMonitor(ip)) {
				monitors.add(monitorIndex.get(ip));
			}
		}
		return monitors;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public String getCountry() {
		if (country == null)
			return "";
		return country;
	}

	public GnpPosition getCoordinateReference() {
		return coordinateRef;
	}

	public int getPeerType() {
		return type;
	}

	public Long getIpAddress() {
		return ipAddress;
	}

	public double getDistance() {
		return distance;
	}

	// ////////////////
	// Set-Methods

	/*
	 * public void setMonitorIP(int monitorID, Long monitorIP) {
	 * measuredRttMonitorIP[monitorID] = monitorIP; }
	 * 
	 * public void setMonitorIP(int monitorID, String monitorIP) {
	 * setMonitorIP(monitorID, Peer.ipLong(monitorIP)); }
	 */

	public void setPeerType(int peerType) {
		this.type = peerType;
	}

	public void setGeographicPosition(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public void setCoordinateReference(GnpPosition coordinateRef) {
		this.coordinateRef = coordinateRef;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public void resetDistance() {
		this.distance = -1.0;
	}

	/*
	 * public void mergeRtt(Long monitorIP, Peer peer) { Integer monitorID =
	 * mapRef.getMonitorID(monitorIP); while (rttToMonitors2.size() <=
	 * monitorID) rttToMonitors2.add(new RttDistribution()); RttDistribution
	 * rttDistri = rttToMonitors2.get(monitorID);
	 * rttDistri.mergeRtt(peer.getRttDistribution(monitorIP)); }
	 */

	public void addRtt(Long monitorIP, double rtt) {

		Integer monitorID = mapRef.getMonitorID(monitorIP);

		while (rttToMonitors2.size() <= monitorID)
			rttToMonitors2.add(new RttDistribution());

		RttDistribution rttDistri = rttToMonitors2.get(monitorID);
		rttDistri.addRtt(rtt);
		// System.out.println(rtt + " / " + rttDistri.getRtt());
	}

	/*
	 * public void setMonitorId(int id){ monitorID = id; }
	 * 
	 * public boolean isMonitor() { if (monitorID > -1) return true; else return
	 * false; }
	 * 
	 * public int getMonitorId(){ return monitorID; }
	 */

	/*
	 * public getTypeOfAddress() { // Brodcast // Multicast // Localhost
	 * 127.0.0.0 ... }
	 */

	public boolean equals(Object obj) {
		return (this.ipAddress == ((GnpHost) obj).getIpAddress());
	}

	public int hashCode() {
		return ipAddress.hashCode();
	}

	public String toString() {
		String returnString = "";
		returnString += GnpHost.ipString(ipAddress);
		returnString += "\t(" + this.getCountry() + ")";
		return returnString;
	}

	/*
	 * public GnpCoordinate getGnpCoordinate() { return coordinateRef; }
	 * 
	 * public void setGnpCoordinate(GnpCoordinate coordinateRef) {
	 * this.coordinateRef = coordinateRef; }
	 * 
	 */

	public static String ipString(Long ip) {
		String returnString = "";
		returnString += Long.toString((ip << 32) >>> 56) + ".";
		returnString += Long.toString((ip << 40) >>> 56) + ".";
		returnString += Long.toString((ip << 48) >>> 56) + ".";
		returnString += Long.toString((ip << 56) >>> 56);
		return returnString;
	}

	public static Long ipLong(String ip) {
		String[] ipBytes = ip.split("\\.");
		Long ipLong = new Long(0);
		try {
			ipLong += (Long.valueOf(ipBytes[0])) << 24;
			ipLong += (Long.valueOf(ipBytes[1])) << 16;
			ipLong += (Long.valueOf(ipBytes[2])) << 8;
			ipLong += Long.valueOf(ipBytes[3]);
		} catch (Exception e) {
			return null;
		}

		return ipLong;
	}

	public void finalizeRttDistribution() {
		for (RttDistribution rtt : this.rttToMonitors2) {
			rtt.finalize();
		}
	}

	/*
	 * public boolean isComplete() { for (int c=0; c<this.getNumberOfMonitors();
	 * c++) if (this.measuredRttCounter[c] == 0) return false; return true; }
	 */

	public void setLocation(Geolocator locator) {

		if (locator.search(ipAddress)) {
			this.setGeographicPosition(locator.getLatitude(), locator.getLongitude());
			this.locatable = true;
			this.country = locator.getCountryCode();
		}

		if (this.country.equals("--") || this.country.equals("A1") || this.country.equals("A2") || this.country.equals("01"))
			this.locatable = false;
		else
			this.locatable = true;

	}

	public boolean isLocatable() {
		return locatable;
	}

	public double calculateMinimumMinRTT() {
		double min = Double.NaN;
		for (RttDistribution rtt : this.rttToMonitors2) {
			if (rtt.getMinRTT() < min)
				min = rtt.getMinRTT();
		}
		return min;
	}

	public double calculateMaximumMinRTT() {
		double max = Double.NaN;
		for (RttDistribution rtt : this.rttToMonitors2) {
			if (rtt.getMinRTT() > max)
				max = rtt.getMinRTT();
		}
		return max;
	}

	public double calculateMinimumMeanRTT() {
		double min = Double.NaN;
		for (RttDistribution rtt : this.rttToMonitors2) {
			if (rtt.getMeanRTT() < min)
				min = rtt.getMeanRTT();
		}
		return min;
	}

	public double calculateMaximumMeanRTT() {
		double max = Double.NaN;
		for (RttDistribution rtt : this.rttToMonitors2) {
			if (rtt.getMeanRTT() > max)
				max = rtt.getMeanRTT();
		}
		return max;
	}

	public double calculateMinimumStDeviationRTT() {
		double min = Double.NaN;
		for (RttDistribution rtt : this.rttToMonitors2) {
			if (rtt.getMeanRTT() < min)
				min = rtt.getMeanRTT();
		}
		return min;
	}

	public double calculateMaximumStDeviationRTT() {
		double max = Double.NaN;
		for (RttDistribution rtt : this.rttToMonitors2) {
			if (rtt.getSdDeviationRTT() > max)
				max = rtt.getSdDeviationRTT();
		}
		return max;
	}

}
