package de.tud.kom.p2psim.impl.network.gnp;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import de.tud.kom.p2psim.api.common.ComponentFactory;
import de.tud.kom.p2psim.api.common.Host;
import de.tud.kom.p2psim.impl.network.gnp.topology.GeographicPosition;
import de.tud.kom.p2psim.impl.network.gnp.topology.GnpPosition;
import de.tud.kom.p2psim.impl.network.gnp.topology.LinkProperty;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.util.logging.SimLogger;

/**
 * Implementation of the factory design pattern for ComplexNetworkWrappers
 * 
 * @author Andreas Glaser, Sebastian Kaune
 */
public class GnpNetLayerFactory implements ComponentFactory {

	private static Logger log = SimLogger.getLogger(GnpNetLayerFactory.class);

	/**
	 * In Kbytes per second
	 */
	private final static double DEFAULT_DOWN_BANDWIDTH = 500l;

	/**
	 * In KBytes per second
	 */
	private final static double DEFAULT_UP_BANDWIDTH = 100l;

	/**
	 * The Subnet of all generated NetworkWrappers.
	 */
	private final GnpSubnet subnet;

	/**
	 * Configures the GnpNetLayers by parsing the corresponding XML-Element of
	 * the config-file.
	 * 
	 * @param elem
	 *            The NetworkLayer-XML-Element.
	 */

	private double downBandwidth;

	private double upBandwidth;

	protected static final String ROOT_TAG = "gnp";

	protected static final String HOSTS_TAG = "Hosts";

	protected static final String HOST_TAG = "Host";

	protected static final String GROUPS_TAG = "Groups";

	protected static final String GROUP_TAG = "Group";

	protected static final String PEERS_TAG = "Peers";

	protected static final String PEER_TAG = "Peer";

	protected HashMap<IPv4NetID, HostGnpInfo> hostPool;

	protected HashMap<String, IPv4NetID[]> namedGroups;

	protected HashSet<IPv4NetID> blacklist;

	protected HashMap<String, HashMap<String, LinkProperty>> regionLinkProperty = new HashMap<String, HashMap<String, LinkProperty>>();

	protected HashMap<String, String[]> countryLockup = new HashMap<String, String[]>();

	public GnpNetLayerFactory() {
		subnet = new GnpSubnet();
		this.downBandwidth = DEFAULT_DOWN_BANDWIDTH;
		this.upBandwidth = DEFAULT_UP_BANDWIDTH;
	}

	public void setLatencyModel(GnpLatencyModel model) {
		model.initLinkProperty(regionLinkProperty, countryLockup);
		subnet.setLatencyModel(model);
	}

	public void setGnpFile(String gnpFileName) throws Exception {

		File gnpFile = new File(gnpFileName);

		hostPool = new HashMap<IPv4NetID, HostGnpInfo>();
		blacklist = new HashSet<IPv4NetID>();

		namedGroups = new HashMap<String, IPv4NetID[]>();

		log.info("Read hosts from file " + gnpFile);
		SAXReader reader = new SAXReader(false);

		Document configuration = null;
		try {
			configuration = reader.read(gnpFile);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Element root = configuration.getRootElement();
		assert root.getName().equals(ROOT_TAG);

		for (Object obj : root.elements()) {
			Element elem = (Element) obj;
			if (elem.getName().equals(HOSTS_TAG)) {
				for (Iterator iter = elem.elementIterator(HOST_TAG); iter.hasNext();) {
					Element variable = (Element) iter.next();
					String id = variable.attributeValue("id");
					IPv4NetID hostNetID = new IPv4NetID(Long.parseLong(variable.attributeValue("ip")));
					if (namedGroups.containsKey(id))
						throw new Exception();
					else {
						IPv4NetID[] group = { hostNetID };
						namedGroups.put(id, group);
					}
				}
			} else if (elem.getName().equals(GROUPS_TAG)) {
				for (Iterator iter = elem.elementIterator(GROUP_TAG); iter.hasNext();) {
					Element variable = (Element) iter.next();
					String id = variable.attributeValue("id");
					String[] ips = variable.attributeValue("ips").split(",");
					IPv4NetID[] group = new IPv4NetID[ips.length];
					for (int c = 0; c < group.length; c++)
						group[c] = new IPv4NetID(Long.parseLong(ips[c]));
					namedGroups.put(id, group);

				}
			} else if (elem.getName().equals(PEERS_TAG)) {
				for (Iterator iter = elem.elementIterator(PEER_TAG); iter.hasNext();) {
					Element variable = (Element) iter.next();

					IPv4NetID hostNetID = new IPv4NetID(Long.parseLong(variable.attributeValue("ip")));
					String country = variable.attributeValue("country");
					double longitude = Double.parseDouble(variable.attributeValue("longitude"));
					double latitude = Double.parseDouble(variable.attributeValue("latitude"));
					String[] coordinatesS = variable.attributeValue("coordinates").split(",");
					double[] coordinatesD = new double[coordinatesS.length];
					for (int c = 0; c < coordinatesD.length; c++)
						coordinatesD[c] = Double.parseDouble(coordinatesS[c]);

					GeographicPosition geoPos = new GeographicPosition(longitude, latitude);
					GnpPosition gnpPos = new GnpPosition(coordinatesD);

					HostGnpInfo hostProp = new HostGnpInfo();
					hostProp.setCountryCode(country);
					hostProp.setGeographicPosition(geoPos);
					hostProp.setGnpPosition(gnpPos);
					hostPool.put(hostNetID, hostProp);
				}
			} else if (elem.getName().equals("pingER")) {
				for (Iterator iter = elem.elementIterator("SummaryReport"); iter.hasNext();) {
					Element variable = (Element) iter.next();
					String regionFrom = variable.attributeValue("from");
					String regionTo = variable.attributeValue("to");
					double minRtt = 0.0;
					double averageRtt = 0.0;
					double delayVariation = 0.0;
					double packetLoss = 0.0;

					minRtt = Double.parseDouble(variable.attributeValue("minimumRtt"));
					averageRtt = Double.parseDouble(variable.attributeValue("averageRtt"));
					delayVariation = Double.parseDouble(variable.attributeValue("delayVariation"));
					packetLoss = Double.parseDouble(variable.attributeValue("packetLoss"));

					if (!regionLinkProperty.containsKey(regionFrom))
						regionLinkProperty.put(regionFrom, new HashMap<String, LinkProperty>());
					regionLinkProperty.get(regionFrom).put(regionTo, new LinkProperty(minRtt, averageRtt, delayVariation, packetLoss));

				}
			} else if (elem.getName().equals("Countries")) {
				for (Iterator iter = elem.elementIterator("CountryKey"); iter.hasNext();) {
					Element variable = (Element) iter.next();
					String code = variable.attributeValue("Code");
					String[] name = new String[2];
					name[0] = variable.attributeValue("Country");
					name[1] = variable.attributeValue("Region");
					countryLockup.put(code, name);
				}
			}
		}
	}

	/**
	 * Generation of a new NetWrapper.
	 * 
	 * @param netID
	 *            The NetworkWrappers NetID.
	 * @return A new NetWrapper.
	 */
	private GnpNetLayer newNetworkWrapper(IPv4NetID netID) {
		blacklist.add(netID);
		GnpPosition pos = this.hostPool.get(netID).getEuclidianPoint();

		GnpNetLayer nw = new GnpNetLayer(this.subnet, netID, pos, this.downBandwidth, this.upBandwidth, hostPool.get(netID).getCountryCode());
		return nw;
	}

	/**
	 * Generation of a new NetWrapper that Wraps a random selected Host from the
	 * Gnp-Model
	 * 
	 * @return A new NetWrapper.
	 */
	public GnpNetLayer newNetworkWrapper() {
		Set<IPv4NetID> hostSet = hostPool.keySet();
		int randomHostPos = (int) Math.floor(Simulator.getRandom().nextDouble() * hostSet.size()); // ToDo
		// Verteilung mit seed initialisieren
		Object randomHost = hostSet.toArray()[randomHostPos];
		while (blacklist.contains(randomHost))
			randomHost = hostSet.toArray()[randomHostPos];
		return newNetworkWrapper((IPv4NetID) randomHost);
	}

	/**
	 * Generation of a new NetWrapper.
	 * 
	 * @param id
	 * 
	 * 1. id ist benannter Host / Gruppe id als Long IP
	 */
	public GnpNetLayer newNetworkWrapper(String id) {

		// Fall 1: id ist ein benannter Host / Gruppe
		if (this.namedGroups.containsKey(id)) {
			IPv4NetID[] group = namedGroups.get(id);
			for (int c = 0; c < group.length; c++) {
				if (!blacklist.contains(group[c])) {
					return newNetworkWrapper(group[c]);
				}
			}
		}

		// Fall 2
		return newNetworkWrapper(this.parseID(id));
	}

	public GnpNetLayer createComponent(Host host) {
		return newNetworkWrapper(host.getProperties().getGroupID());
	}

	/**
	 * Creates a NetID out of a string.
	 * 
	 * @param s
	 *            The String.
	 * @return The NetID.
	 */
	private IPv4NetID parseID(String s) {
		return new IPv4NetID(s);
	}

	public void setDownBandwidth(double downBandwidth) {
		this.downBandwidth = downBandwidth;
	}

	public void setUpBandwidth(double upBandwidth) {
		this.upBandwidth = upBandwidth;
	}

	private class HostGnpInfo {

		public String groupId;

		private GnpPosition gnpPosition;

		private GeographicPosition geoPosition;

		private String countryCode;

		GnpPosition getEuclidianPoint() {
			return gnpPosition;
		}

	    void setGnpPosition(GnpPosition position) {
			this.gnpPosition = position;
		}

	    void setGeographicPosition(GeographicPosition position) {
			this.geoPosition = position;
		}

		void setCountryCode(String countryCode) {
			this.countryCode = countryCode;
		}

		String getCountryCode() {
			return countryCode;
		}

		String getGroupId() {
			return groupId;
		}

		void setGroupId(String groupId) {
			this.groupId = groupId;
		}

	}

}
