package de.tud.kom.p2psim.impl.network.gnp.topology;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamTokenizer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.apache.commons.math.stat.StatUtils;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultDocument;
import org.dom4j.tree.DefaultElement;

import de.tud.kom.p2psim.impl.simengine.Simulator;

public class GnpHostMap implements Serializable {

	private static final long serialVersionUID = -4133566173471138424L;

	private Hashtable<Long, GnpHost> monitorIndex;

	private Hashtable<Long, GnpHost> peerIndex;

	private Hashtable<Long, Integer> monitorTranslator; // IP -> intern Monitor

	// ID

	private ArrayList<GnpHost>[][] peerDistribution;

	private boolean isSymmetric = false;

	private GnpSpace gnpRef;

	private HashMap<String, HashMap<String, Double>> pingErMinimumRtt = new HashMap<String, HashMap<String, Double>>();

	private HashMap<String, HashMap<String, Double>> pingErAverageRtt = new HashMap<String, HashMap<String, Double>>();

	private HashMap<String, HashMap<String, Double>> pingErDelayVariation = new HashMap<String, HashMap<String, Double>>();

	private HashMap<String, HashMap<String, Double>> pingErPacketLoss = new HashMap<String, HashMap<String, Double>>();

	private HashMap<String, String[]> countries;

	private ArrayList<GnpHost>[][][] quickLockup;

	private ArrayList<GnpHostGroup> simGroups;

	private ArrayList<GnpHost> simHosts;

	public GnpHostMap() {
		super();

		monitorIndex = new Hashtable<Long, GnpHost>();
		monitorTranslator = new Hashtable<Long, Integer>();
		peerIndex = new Hashtable<Long, GnpHost>();

		simGroups = new ArrayList<GnpHostGroup>();
		simHosts = new ArrayList<GnpHost>();

		try {
			this.importContryStrings(new File("measuredData/countries.txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void builtQuickLockup() {

		quickLockup = new ArrayList[21][21][21];

		int index0 = 0;
		int index1 = 0;
		int index2 = 0;

		Set<Long> ips = this.peerIndex.keySet();
		for (Long ip : ips) {
			index0 = (int) Math.floor(GnpHostMap.getGeographicalDistance(90, 0, peerIndex.get(ip).getLatitude(), peerIndex.get(ip).getLongitude()) / 1000.0);
			index1 = (int) Math.floor(GnpHostMap.getGeographicalDistance(0, 0, peerIndex.get(ip).getLatitude(), peerIndex.get(ip).getLongitude()) / 1000.0);
			index2 = (int) Math.floor(GnpHostMap.getGeographicalDistance(0, 90, peerIndex.get(ip).getLatitude(), peerIndex.get(ip).getLongitude()) / 1000.0);
			if (quickLockup[index0][index1][index2] == null)
				quickLockup[index0][index1][index2] = new ArrayList<GnpHost>();
			quickLockup[index0][index1][index2].add(peerIndex.get(ip));
		}

	}

	public void importContryStrings(File countryFile) throws IOException {
		FileReader inputFile = new FileReader(countryFile);
		BufferedReader input = new BufferedReader(inputFile);

		countries = new HashMap<String, String[]>();
		String line = input.readLine();
		while (line != null) {
			String[] countryNames = line.split(";");
			String code = countryNames[0];
			countries.put(code, countryNames);
			line = input.readLine();
		}
	}

	public GnpSpace getGnpRef() {
		return gnpRef;
	}

	public void setGnpRef(GnpSpace gnp) {
		gnpRef = gnp;
	}

	public ArrayList<GnpHostGroup> getSimGroups() {
		return simGroups;
	}

	public ArrayList<GnpHost> getSimHosts() {
		return simHosts;
	}

	public Integer getMonitorID(Long monitorIP) {
		return monitorTranslator.get(monitorIP);
	}

	public ArrayList<GnpHost> getMonitors() {

		ArrayList<GnpHost> monitors = new ArrayList<GnpHost>();

		Set<Long> ips = monitorIndex.keySet();
		for (Long ip : ips) {
			monitors.add(monitorIndex.get(ip));
		}

		Collections.sort(monitors, new PeerComparatorNoOfConnections());

		return monitors;
	}

	public void importSkitterFile(File skitterFile) throws NumberFormatException, IOException {

		FileReader inputFile = new FileReader(skitterFile);
		BufferedReader input = new BufferedReader(inputFile);
		StreamTokenizer tokenizer = new StreamTokenizer(input);

		System.out.println("Importing: " + skitterFile.getAbsolutePath());

		tokenizer.resetSyntax();
		tokenizer.whitespaceChars(' ', ' ');
		tokenizer.wordChars('0', '9');
		tokenizer.wordChars('.', '.');
		tokenizer.wordChars('-', '-');
		tokenizer.wordChars('A', 'Z');
		tokenizer.wordChars('a', 'z');

		Long monitorIP;
		Long peerIP;
		double rtt;
		GnpHost currentMonitor;
		GnpHost currentPeer;

		int x = 0;

		while ((tokenizer.nextToken()) != StreamTokenizer.TT_EOF) {

			if (tokenizer.sval.equals("C") || tokenizer.sval.equals("I")) {

				x++;

				tokenizer.nextToken();
				String mo = tokenizer.sval;
				tokenizer.nextToken();
				String pe = tokenizer.sval;
				tokenizer.nextToken();
				tokenizer.nextToken();
				String rt = tokenizer.sval;

				monitorIP = GnpHost.ipLong(mo);
				peerIP = GnpHost.ipLong(pe);
				rtt = Double.valueOf(rt);

				currentMonitor = peerIndex.get(monitorIP);
				if (currentMonitor == null) {
					currentMonitor = new GnpHost(monitorIP, this);
					monitorIndex.put(monitorIP, currentMonitor);
					peerIndex.put(monitorIP, currentMonitor);
				}
				if (!monitorIndex.contains(currentMonitor)) {
					monitorIndex.put(monitorIP, currentMonitor);
				}
				if (!monitorTranslator.containsKey(monitorIP)) {
					monitorTranslator.put(monitorIP, monitorTranslator.size());
				}

				currentMonitor.setPeerType(GnpHost.MONITOR);

				currentPeer = peerIndex.get(peerIP);
				if (currentPeer == null) {
					currentPeer = new GnpHost(peerIP, this);
					currentPeer.setPeerType(GnpHost.PEER);
					peerIndex.put(peerIP, currentPeer);
				}
				currentPeer.addRtt(monitorIP, rtt);
			}

			input.readLine();
		}

		/*
		 * Set<Long> ips = this.peerIndex.keySet(); for (Long ip : ips) {
		 * peerIndex.get(ip).finalizeRttDistribution(); } System.gc();
		 */
	}

	public ArrayList<GnpHost>[][] getPeerGrid(int resLon, int resLat) {

		ArrayList<GnpHost>[][] peerGrid = new ArrayList[resLon][resLat];

		int posLon;
		int posLat;

		double stepLon = 360 / (double) resLon;
		double stepLat = 180 / (double) resLat;

		Set<Long> ips = this.peerIndex.keySet();
		for (Long ip : ips) {

			posLon = (int) Math.floor((180 + peerIndex.get(ip).getLongitude()) / stepLon);
			posLat = (int) Math.floor((90 + peerIndex.get(ip).getLatitude()) / stepLat);

			if (peerGrid[posLon][posLat] == null)
				peerGrid[posLon][posLat] = new ArrayList<GnpHost>();
			peerGrid[posLon][posLat].add(peerIndex.get(ip));

		}

		return peerGrid;

	}

	public void initGrid(int resLon, int resLat) {

		peerDistribution = getPeerGrid(resLon, resLat);

	}

	public void builtCountryGroups(String countryFile) throws IOException {
		FileReader inputFile = new FileReader(countryFile);
		BufferedReader input = new BufferedReader(inputFile);

		HashMap<String, GnpHostGroup> countryGroups = new HashMap<String, GnpHostGroup>();
		String line = input.readLine();
		while (line != null) {
			String[] countryNames = line.split(";");
			if (countryNames.length == 4) {
				countryGroups.put(countryNames[0], new GnpHostGroup(this, countryNames[2]));
			}
			line = input.readLine();
		}

		Set<Long> ips = this.peerIndex.keySet();
		for (Long ip : ips) {
			GnpHostGroup group = countryGroups.get(peerIndex.get(ip).getCountry());
			if (group != null) {
				group.addPeer(peerIndex.get(ip));
			}
		}

		Set<String> groupIds = countryGroups.keySet();
		for (String groupId : groupIds) {
			if (countryGroups.get(groupId).getPeers().size() > 0)
				this.simGroups.add(countryGroups.get(groupId));
		}

	}

	public void builtRegionGroups(String countryFile) throws IOException {
		FileReader inputFile = new FileReader(countryFile);
		BufferedReader input = new BufferedReader(inputFile);

		HashMap<String, String> countryToRegion = new HashMap<String, String>();
		String line = input.readLine();
		while (line != null) {
			String[] countryNames = line.split(";");
			if (countryNames.length == 4) {
				countryToRegion.put(countryNames[0], countryNames[3]);
			}
			line = input.readLine();
		}

		HashMap<String, GnpHostGroup> regionGroups = new HashMap<String, GnpHostGroup>();
		Set<Long> ips = this.peerIndex.keySet();
		for (Long ip : ips) {

			String region = countryToRegion.get(peerIndex.get(ip).getCountry());

			if (region != null) {

				if (!regionGroups.containsKey(region)) {
					regionGroups.put(region, new GnpHostGroup(this, region));
				}
				GnpHostGroup group = regionGroups.get(region);
				group.addPeer(peerIndex.get(ip));
			}
		}

		Set<String> groupIds = regionGroups.keySet();
		for (String groupId : groupIds) {
			this.simGroups.add(regionGroups.get(groupId));
		}

	}

	public int getNoOfPeersInGrid(int lon, int lat) {
		if (peerDistribution[lon][lat] != null)
			return peerDistribution[lon][lat].size();
		else
			return 0;
	}

	public double[] getGridStepDimension() {
		double[] dimension = new double[2];
		dimension[0] = 360 / (double) peerDistribution.length;
		dimension[1] = 180 / (double) peerDistribution[0].length;
		return dimension;
	}

	public int getMaxPeersPerGrid() {
		int max = 0;
		for (int x = 0; x < peerDistribution.length; x++) {
			for (int y = 0; y < peerDistribution[x].length; y++) {
				if (peerDistribution[x][y] != null)
					if (peerDistribution[x][y].size() > max)
						max = peerDistribution[x][y].size();
			}
		}
		return max;
	}

	public int getMinPeersPerGrid() {
		int min = peerIndex.size();
		for (int x = 0; x < peerDistribution.length; x++) {
			for (int y = 0; y < peerDistribution[x].length; y++) {
				if (peerDistribution[x][y] != null) {
					if (peerDistribution[x][y].size() < min)
						min = peerDistribution[x][y].size();
				} else
					return 0;
			}
		}
		return min;
	}

	public int getNoOfPeers() {
		return peerIndex.size();
	}

	public int getNoOfMonitors() {
		return monitorIndex.size();
	}

	public void setLocationOfPeers() {

		Geolocator locator;
		locator = new GeolocatorGeoIP();

		Set<Long> ips = peerIndex.keySet();
		for (Long ip : ips) {
			peerIndex.get(ip).setLocation(locator);
		}
		this.removeUnlocatablePeers();
	}

	public Hashtable<Long, GnpHost> getMonitorIndex() {
		return monitorIndex;
	}

	public Hashtable<Long, GnpHost> getPeerIndex() {
		return peerIndex;
	}

	public void printMonitorAdjacencyMatrix() {

		DecimalFormat myDF = new DecimalFormat("0.0");

		System.out.print("\n");

		ArrayList<GnpHost> monitorList = getMonitors();

		for (GnpHost p1 : monitorList) {
			System.out.print(p1 + "\t");
			for (GnpHost p2 : monitorList) {
				System.out.print(myDF.format(getRTT(p1.getIpAddress(), p2.getIpAddress())) + "\t");
			}
			System.out.print("\n");
		}

	}

	public void saveToFile(String filename) throws IOException {
		FileOutputStream fos = new FileOutputStream(filename);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(this);
	}

	public static GnpHostMap loadFromFile(File file) throws IOException, ClassNotFoundException {
		FileInputStream fis = new FileInputStream(file);
		ObjectInputStream ois = new ObjectInputStream(fis);
		return (GnpHostMap) ois.readObject();
	}

	public double getRTT(Long peerIP, Long monitorIP) {
		return peerIndex.get(peerIP).getRtt(monitorIP);
	}

	public void makeSymmetry() {

		if (isSymmetric)
			return;

		GnpHost monitorA;
		GnpHost monitorB;

		Set<Long> ips1 = monitorIndex.keySet();
		for (Long ip1 : ips1) {
			monitorA = monitorIndex.get(ip1);
			Set<Long> ips2 = monitorIndex.keySet();
			for (Long ip2 : ips2) {
				monitorB = monitorIndex.get(ip2);
				if (monitorA == monitorB) {
					monitorA.getRttDistribution(ip2).clear();
				} else {
					monitorA.getRttDistribution(ip2).mergeRtt(monitorB.getRttDistribution(ip1));
					monitorB.setRttDistribution(ip1, monitorA.getRttDistribution(ip2));
				}
			}

		}
		isSymmetric = true;
	}

	public static double getGeographicalDistance(double lat1, double lon1, double lat2, double lon2) {

		double EARTH_DIAMETER = 2 * 6378.2;
		double PI = 3.14159265;
		double RAD_CONVERT = PI / 180;

		double delta_lat, delta_lon;
		double temp;

		// convert degrees to radians
		lat1 *= RAD_CONVERT;
		lat2 *= RAD_CONVERT;

		// find the deltas
		delta_lat = lat2 - lat1;
		delta_lon = (lon2 - lon1) * RAD_CONVERT;

		// Find the great circle distance
		temp = Math.pow(Math.sin(delta_lat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(delta_lon / 2), 2);
		return EARTH_DIAMETER * Math.atan2(Math.sqrt(temp), Math.sqrt(1 - temp));
	}

	public static double getGeographicalDistance(GnpHost p1, GnpHost p2) {
		return GnpHostMap.getGeographicalDistance(p1.getLatitude(), p1.getLongitude(), p2.getLatitude(), p2.getLongitude());
	}

	public GnpHost getNearestPeer(double longitude, double latitude) {

		int index0 = (int) Math.floor(GnpHostMap.getGeographicalDistance(90, 0, latitude, longitude) / 1000.0);
		int index1 = (int) Math.floor(GnpHostMap.getGeographicalDistance(0, 0, latitude, longitude) / 1000.0);
		int index2 = (int) Math.floor(GnpHostMap.getGeographicalDistance(0, 90, latitude, longitude) / 1000.0);

		GnpHost peer = null;

		PeerComparatorDistance comparator = new PeerComparatorDistance();
		comparator.setPosition(latitude, longitude);

		if (quickLockup != null && quickLockup[index0][index1][index2] != null) {
			for (GnpHost p : quickLockup[index0][index1][index2]) {
				if (peer == null) {
					peer = p;
				} else if (comparator.compare(peer, p) == 1) {
					peer = p;
				}
			}
		}
		return peer;
	}

	private int[] calculateNumberOfUnlocatablePeers() {
		int unlocatedPeers = 0;
		int unlocatedMonitors = 0;
		Set<Long> ips = this.peerIndex.keySet();
		for (Long ip : ips) {
			unlocatedPeers += (peerIndex.get(ip).isLocatable()) ? 0 : 1;
			unlocatedMonitors += (peerIndex.get(ip).isLocatable() || peerIndex.get(ip).getPeerType() == GnpHost.PEER) ? 0 : 1;
		}
		int[] returnValue = { unlocatedPeers, unlocatedMonitors };
		return returnValue;
	}

	public int getNoOfUnlocatablePeers() {
		int[] no = calculateNumberOfUnlocatablePeers();
		return no[0] - no[1];
	}

	class PeerComparatorDistance implements Comparator<GnpHost> {

		double latitude;

		double longitude;

		public void setPosition(double latitude, double longitude) {
			this.latitude = latitude;
			this.longitude = longitude;
		}

		public int compare(GnpHost peer1, GnpHost peer2) {

			double distance1 = GnpHostMap.getGeographicalDistance(peer1.getLatitude(), peer1.getLongitude(), latitude, longitude);
			double distance2 = GnpHostMap.getGeographicalDistance(peer2.getLatitude(), peer2.getLongitude(), latitude, longitude);

			if (distance1 < distance2)
				return -1;
			else if (distance1 > distance2)
				return 1;
			else
				return 0;
		}

	}

	class PeerComparatorNoOfConnections implements Comparator<GnpHost> {

		public int compare(GnpHost peer1, GnpHost peer2) {

			int coonections1 = peer1.getConnectedMonitors().size();
			int coonections2 = peer2.getConnectedMonitors().size();

			if (coonections1 < coonections2)
				return 1;
			else if (coonections1 > coonections2)
				return -1;
			else
				return 0;
		}

	}

	public void printStatus() {
		System.out.println("Number of Peers / Monitors: " + this.peerIndex.size() + " / " + this.monitorIndex.size());
		System.out.println("Number of unlocatable Peers / Monitors: " + calculateNumberOfUnlocatablePeers()[0] + " / " + calculateNumberOfUnlocatablePeers()[1]);
	}

	public void addSimGroup() {
		simGroups.add(new GnpHostGroup(this));
	}

	public void addPeersToGroup(GnpHostGroup group, boolean[][] grid) {

		ArrayList<GnpHost>[][] peerGrid = getPeerGrid(grid.length, grid[0].length);

		for (int x = 0; x < grid.length; x++) {
			for (int y = 0; y < grid[x].length; y++) {
				if (grid[x][y] && peerGrid[x][y] != null) {
					group.addPeer(peerGrid[x][y]);
				}
			}
		}
	}

	public void addPeerToHosts(GnpHost host) {
		this.simHosts.add(host);
	}

	public double calculateMinimumMinRTT() {
		double min = Double.NaN;
		Set<Long> ips = this.peerIndex.keySet();
		for (Long ip : ips) {
			if (peerIndex.get(ip).calculateMinimumMinRTT() < min)
				min = peerIndex.get(ip).calculateMinimumMinRTT();
		}
		return min;
	}

	public double calculateMaximumMinRTT() {
		double max = Double.NaN;
		Set<Long> ips = this.peerIndex.keySet();
		for (Long ip : ips) {
			if (peerIndex.get(ip).calculateMaximumMinRTT() > max)
				max = peerIndex.get(ip).calculateMaximumMinRTT();
		}
		return max;
	}

	public double calculateMinimumMeanRTT() {
		double min = Double.NaN;
		Set<Long> ips = this.peerIndex.keySet();
		for (Long ip : ips) {
			if (peerIndex.get(ip).calculateMinimumMeanRTT() < min)
				min = peerIndex.get(ip).calculateMinimumMeanRTT();
		}
		return min;
	}

	public double calculateMaximumMeanRTT() {
		double max = Double.NaN;
		Set<Long> ips = this.peerIndex.keySet();
		for (Long ip : ips) {
			if (peerIndex.get(ip).calculateMaximumMeanRTT() > max)
				max = peerIndex.get(ip).calculateMaximumMeanRTT();
		}
		return max;
	}

	public double calculateMinimumStDeviationRTT() {
		double min = Double.NaN;
		Set<Long> ips = this.peerIndex.keySet();
		for (Long ip : ips) {
			if (peerIndex.get(ip).calculateMaximumStDeviationRTT() < min)
				min = peerIndex.get(ip).calculateMaximumStDeviationRTT();
		}
		return min;
	}

	public double calculateMaximumStDeviationRTT() {
		double max = Double.NaN;
		Set<Long> ips = this.peerIndex.keySet();
		for (Long ip : ips) {
			if (peerIndex.get(ip).calculateMaximumStDeviationRTT() > max)
				max = peerIndex.get(ip).calculateMaximumStDeviationRTT();
		}
		return max;
	}

	public void removeRandomPeers(int noOfPeers) {

		if (noOfPeers > getNoOfPeers() - getNoOfMonitors())
			return;

		while (noOfPeers > 0) {
			Set<Long> ips = this.peerIndex.keySet();
			Object[] ipsArray = ips.toArray();

			for (int c = 0; c < noOfPeers; c++) {

				int random = (int) Math.floor(Simulator.getRandom().nextDouble() * ipsArray.length);
				GnpHost peer = peerIndex.get(ipsArray[random]);

				if (peer != null && peer.getPeerType() == GnpHost.PEER && !simHosts.contains(peer)) {
					if (this.getGnpRef() != null)
						this.getGnpRef().removeCoordinate((Long) ipsArray[random]);
					this.peerIndex.remove(ipsArray[random]);
					noOfPeers--;
				}
			}
		}
		this.builtQuickLockup();
		updateGroups();
	}

	public void removeMonitor(long monitorIP) {
		this.monitorIndex.remove(monitorIP);
		this.peerIndex.remove(monitorIP);
		Set<Long> ips = this.peerIndex.keySet();
		for (Long ip : ips) {
			peerIndex.get(ip).removeRTT(monitorIP);
		}
		this.monitorTranslator.remove(monitorIP);
		this.builtQuickLockup();
	}

	private void removePeer(long peerIP) {
		if (this.getGnpRef() != null)
			this.getGnpRef().removeCoordinate(peerIP);
		this.peerIndex.remove(peerIP);
	}

	public void removePeers(int noOfConnections) {

		if (noOfConnections < 1 && noOfConnections > getNoOfMonitors())
			return;

		ArrayList<Long> deletePeers = new ArrayList<Long>();

		Set<Long> ips = this.peerIndex.keySet();
		for (Long ip : ips) {
			if (peerIndex.get(ip).getConnectedMonitors().size() == noOfConnections && peerIndex.get(ip).getPeerType() == GnpHost.PEER)
				deletePeers.add(ip);
		}

		for (Long ip : deletePeers) {
			if (this.getGnpRef() != null)
				this.getGnpRef().removeCoordinate(ip);
			this.peerIndex.remove(ip);
		}
		this.builtQuickLockup();
		updateGroups();

	}

	public void removeMonitorsKeepMaximumSparation(int noOfMonitorsToKeep) {
		ArrayList<GnpHost> maxSeperatedPeers = getMaximumSeparatedMonitors(noOfMonitorsToKeep);
		Set<Long> ips = this.monitorIndex.keySet();
		ArrayList<Long> deleteMonitors = new ArrayList<Long>();
		for (Long ip : ips) {
			if (!maxSeperatedPeers.contains(monitorIndex.get(ip)))
				deleteMonitors.add(ip);
		}
		for (Long ip : deleteMonitors) {
			this.removeMonitor(ip);
		}
		updateGroups();
	}

	public void removeUnlocatablePeers() {

		ArrayList<Long> deletePeers = new ArrayList<Long>();

		Set<Long> ips = this.peerIndex.keySet();
		for (Long ip : ips) {
			if (!peerIndex.get(ip).isLocatable() && peerIndex.get(ip).getPeerType() == GnpHost.PEER)
				deletePeers.add(ip);
		}

		for (Long ip : deletePeers) {
			removePeer(ip);
		}
		this.builtQuickLockup();
	}

	public void removePeersWithRelativError(double threshold) {

		ArrayList<Long> deletePeers = new ArrayList<Long>();
		Set<Long> ips = this.peerIndex.keySet();
		for (Long ip : ips) {
			if (peerIndex.get(ip).getPeerType() != GnpHost.MONITOR && peerIndex.get(ip).getCoordinateReference() != null) {

				double[] errors = peerIndex.get(ip).getCoordinateReference().getDirectionalRelativErrors();
				double maxError = StatUtils.max(errors);

				if (Math.abs(maxError) > threshold) {
					deletePeers.add(ip);
				}
			}
		}

		for (Long ip : deletePeers) {
			removePeer(ip);
		}
		updateGroups();
		this.builtQuickLockup();
	}

	public void updateGroups() {
		for (GnpHostGroup group : simGroups)
			group.updateGroup();

	}

	public int[] getConnectivityOfPeers() {
		int[] counter = new int[getNoOfMonitors() + 1];
		Set<Long> ips = this.peerIndex.keySet();
		for (Long ip : ips) {
			if (peerIndex.get(ip).getPeerType() == GnpHost.PEER)
				counter[peerIndex.get(ip).getConnectedMonitors().size()]++;
		}
		return counter;
	}

	public ArrayList<GnpHost> getMaximumSeparatedMonitors(int noOfMonitors) {

		ArrayList<ArrayList<GnpHost>> allCombinations = getMonitorCombinations(noOfMonitors);

		int posWithMax = 0;
		double valueMax = 0.0;

		for (int c = 0; c < allCombinations.size(); c++) {
			double currentDistance = getInterMonitorDistance(allCombinations.get(c));
			if (currentDistance > valueMax) {
				valueMax = currentDistance;
				posWithMax = c;
			}
		}

		return allCombinations.get(posWithMax);

	}

	public double getInterMonitorDistance(ArrayList<GnpHost> monitors) {
		double result = 0.0;
		for (int c = 0; c < monitors.size() - 1; c++)
			for (int d = c + 1; d < monitors.size(); d++)
				result += monitors.get(c).getRtt(monitors.get(d).getIpAddress());
		return result;

	}

	public ArrayList<ArrayList<GnpHost>> getMonitorCombinations(int size) {
		return builtRecursive(new ArrayList<GnpHost>(), size, this.getMonitors(), 0);
	}

	private ArrayList<ArrayList<GnpHost>> builtRecursive(ArrayList<GnpHost> current, int max, ArrayList<GnpHost> monitors, int posInMonitors) {

		ArrayList<ArrayList<GnpHost>> result = new ArrayList<ArrayList<GnpHost>>();

		if (current.size() == max) {
			result.add(current);
			return result;
		} else {
			for (int c = posInMonitors; c < monitors.size() - (max - current.size()); c++) {
				ArrayList<GnpHost> copy = (ArrayList<GnpHost>) current.clone();
				copy.add(monitors.get(c));
				result.addAll(builtRecursive(copy, max, monitors, c + 1));
			}
			return result;
		}
	}

	public void gnuPlot(String filename) throws IOException {

		FileWriter us = new FileWriter("us" + filename);
		FileWriter de = new FileWriter("de" + filename);
		FileWriter all = new FileWriter(filename);

		Set<Long> ips = this.peerIndex.keySet();

		for (Long ip : ips) {

			GnpHost p = this.peerIndex.get(ip);

			ArrayList<GnpHost> monitors = p.getConnectedMonitors();

			for (GnpHost mon : monitors) {

				double distance = getGeographicalDistance(p, mon);
				double rtt = p.getRtt(mon.getIpAddress());

				if (p.getCountry().equals("DE") || mon.getCountry().equals("DE"))
					de.write(distance + " " + rtt + "\n");
				if (p.getCountry().equals("US") || mon.getCountry().equals("US"))
					us.write(distance + " " + rtt + "\n");

				all.write(distance + " " + rtt + "\n");
			}

		}

		all.close();
		us.close();
		de.close();

	}

	private String[][] parseTsvFile(File file) throws IOException {

		ArrayList<String[]> tempResult = new ArrayList<String[]>();
		String[][] result = new String[1][1];

		FileReader inputFile = new FileReader(file);
		BufferedReader input = new BufferedReader(inputFile);

		String line = input.readLine();
		while (line != null) {
			tempResult.add(line.split("\t"));
			line = input.readLine();
		}

		return tempResult.toArray(result);

	}

	private void importPingErTsvFile(File file, HashMap<String, HashMap<String, Double>> adjacencyList) throws IOException {

		String[][] data = parseTsvFile(file);

		for (int i = 1; i < data.length - 1; i++) {
			for (int j = 1; j < data[i].length; j++) {

				if (data[i][j].equals("."))
					continue;

				if (!adjacencyList.containsKey(data[i][0]))
					adjacencyList.put(data[i][0], new HashMap<String, Double>());
				HashMap<String, Double> current = adjacencyList.get(data[i][0]);
				current.put(data[0][j], Double.parseDouble(data[i][j]));
			}
		}
	}

	public void importPingErMinimumRtt(File file) throws IOException {
		importPingErTsvFile(file, pingErMinimumRtt);
	}

	public void importPingErAverageRtt(File file) throws IOException {
		importPingErTsvFile(file, pingErAverageRtt);
	}

	public void importPingErDelayVariation(File file) throws IOException {
		importPingErTsvFile(file, pingErDelayVariation);
	}

	public void importPingErPacketLoss(File file) throws IOException {
		importPingErTsvFile(file, pingErPacketLoss);
	}

	public void exportToXml(String fileName) throws IOException {

		DefaultDocument document = new DefaultDocument(new DefaultElement("gnp"));

		DefaultElement peers = new DefaultElement("Peers");
		Set<Long> ips = peerIndex.keySet();
		for (Long ip : ips) {
			DefaultElement peer = new DefaultElement("Peer");
			peer.addAttribute("ip", String.valueOf(ip));
			peer.addAttribute("country", peerIndex.get(ip).getCountry());
			peer.addAttribute("longitude", String.valueOf(peerIndex.get(ip).getLongitude()));
			peer.addAttribute("latitude", String.valueOf(peerIndex.get(ip).getLatitude()));
			peer.addAttribute("coordinates", peerIndex.get(ip).getCoordinateReference().getCoordinateString());
			peers.add(peer);
		}

		DefaultElement groups = new DefaultElement("Groups");
		for (GnpHostGroup group : getSimGroups()) {
			DefaultElement groupXml = new DefaultElement("Group");
			groupXml.addAttribute("id", group.getGroupName().replace(' ', '+'));
			groupXml.addAttribute("ips", group.getIpString());
			groups.add(groupXml);
		}

		DefaultElement hosts = new DefaultElement("Hosts");
		for (GnpHost peer : getSimHosts()) {
			DefaultElement peerXml = new DefaultElement("Host");
			peerXml.addAttribute("id", peer.getName().replace(' ', '+'));
			peerXml.addAttribute("ip", String.valueOf(peer.getIpAddress()));
			hosts.add(peerXml);
		}

		DefaultElement pingEr = new DefaultElement("pingER");

		Set<String> keys1 = pingErMinimumRtt.keySet();
		Set<String> keys2 = pingErAverageRtt.keySet();
		Set<String> keys3 = pingErDelayVariation.keySet();
		Set<String> keys4 = pingErPacketLoss.keySet();

		HashSet<String> keys = new HashSet<String>();
		keys.addAll(keys1);
		keys.addAll(keys2);
		keys.addAll(keys3);
		keys.addAll(keys4);

		for (String from : keys) {
			for (String to : keys) {
				DefaultElement pingErXml = new DefaultElement("SummaryReport");
				pingErXml.addAttribute("from", from);
				pingErXml.addAttribute("to", to);

				String minimumRtt;
				if (pingErMinimumRtt.get(from) == null)
					minimumRtt = "-";
				else if (pingErMinimumRtt.get(from).get(to) == null)
					minimumRtt = "-";
				else
					minimumRtt = String.valueOf(pingErMinimumRtt.get(from).get(to));

				String averageRtt;
				if (pingErAverageRtt.get(from) == null)
					averageRtt = "-";
				else if (pingErAverageRtt.get(from).get(to) == null)
					averageRtt = "-";
				else
					averageRtt = String.valueOf(pingErAverageRtt.get(from).get(to));

				String delayVariation;
				if (pingErDelayVariation.get(from) == null)
					delayVariation = "-";
				else if (pingErDelayVariation.get(from).get(to) == null)
					delayVariation = "-";
				else
					delayVariation = String.valueOf(pingErDelayVariation.get(from).get(to));

				String packetLoss;
				if (pingErPacketLoss.get(from) == null)
					packetLoss = "-";
				else if (pingErPacketLoss.get(from).get(to) == null)
					packetLoss = "-";
				else
					packetLoss = String.valueOf(pingErPacketLoss.get(from).get(to));

				if ((minimumRtt.equals("-") && averageRtt.equals("-") && delayVariation.equals("-") && packetLoss.equals("-"))
						|| (minimumRtt.equals("-") && averageRtt.equals("-") && delayVariation.equals("0.0") && packetLoss.equals("-")))
					continue;

				pingErXml.addAttribute("minimumRtt", minimumRtt);
				pingErXml.addAttribute("averageRtt", averageRtt);
				pingErXml.addAttribute("delayVariation", delayVariation);
				pingErXml.addAttribute("packetLoss", packetLoss);
				pingEr.add(pingErXml);
			}
		}

		DefaultElement c = new DefaultElement("Countries");
		Set<String> contrySet = this.countries.keySet();
		for (String code : contrySet) {
			DefaultElement countryKey = new DefaultElement("CountryKey");

			if (countries.get(code).length == 4) {
				countryKey.addAttribute("Code", code);
				countryKey.addAttribute("Country", countries.get(code)[2].replace(' ', '+'));
				countryKey.addAttribute("Region", countries.get(code)[3].replace(' ', '+'));
				c.add(countryKey);
			}

		}

		document.getRootElement().add(hosts);
		document.getRootElement().add(groups);
		document.getRootElement().add(peers);
		document.getRootElement().add(pingEr);
		document.getRootElement().add(c);

		// lets write to a file
		OutputFormat format = OutputFormat.createPrettyPrint();
		XMLWriter writer = new XMLWriter(new FileWriter(fileName), format);
		writer.write(document);
		writer.close();

	}

	/*
	 * private ArrayList<Peer> getProblematicMonitors(ArrayList<Peer>
	 * monitors) {
	 * 
	 * ArrayList<Peer> problem = new ArrayList<Peer>();
	 * 
	 * for (int i=0; i<monitors.size()-1; i++) { for (int j=i+1; j<monitors.size();
	 * j++) { if
	 * (!monitors.get(i).isConnectedToMonitor(monitors.get(j).getIpAddress())) {
	 * if (!problem.contains(monitors.get(i))) problem.add(monitors.get(i)); if
	 * (!problem.contains(monitors.get(j))) problem.add(monitors.get(j)); } } }
	 * 
	 * return problem; }
	 */
}
