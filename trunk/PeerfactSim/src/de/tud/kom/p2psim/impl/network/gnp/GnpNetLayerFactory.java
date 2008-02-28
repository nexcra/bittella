package de.tud.kom.p2psim.impl.network.gnp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import de.tud.kom.p2psim.api.common.ComponentFactory;
import de.tud.kom.p2psim.api.common.Host;
import de.tud.kom.p2psim.impl.network.gnp.topology.CountryLookup;
import de.tud.kom.p2psim.impl.network.gnp.topology.GeographicPosition;
import de.tud.kom.p2psim.impl.network.gnp.topology.GnpPosition;
import de.tud.kom.p2psim.impl.network.gnp.topology.PingErLookup;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.util.logging.SimLogger;

/**
 * Implementation of the factory design pattern for ComplexNetworkWrappers
 * 
 * @author Andreas Glaser, Sebastian Kaune
 */
public class GnpNetLayerFactory implements ComponentFactory {

	private static Logger log = SimLogger.getLogger(GnpNetLayerFactory.class);

	private final GnpSubnet subnet;

	private final static double DEFAULT_DOWN_BANDWIDTH = 500l;
	private final static double DEFAULT_UP_BANDWIDTH = 100l;

	private double downBandwidth;
	private double upBandwidth;

	private HashMap<IPv4NetID, TopologyProperty> hostPool;
	private HashMap<String, ArrayList<IPv4NetID>> namedGroups;

	private PingErLookup pingErLockup;
	private CountryLookup countryLockup;

	public GnpNetLayerFactory() {
		subnet = new GnpSubnet();
		this.downBandwidth = DEFAULT_DOWN_BANDWIDTH;
		this.upBandwidth = DEFAULT_UP_BANDWIDTH;
	}

	public GnpNetLayer createComponent(Host host) {
		GnpNetLayer netLayer = newNetLayer(host.getProperties().getGroupID());
		netLayer.setHost(host);
		return netLayer;
	}

	/**
	 * random node form group
	 * 
	 * @param id
	 * @return
	 */
	public GnpNetLayer newNetLayer(String id) {
		if (this.namedGroups.containsKey(id) && !this.namedGroups.get(id).isEmpty()) {
			int size = namedGroups.get(id).size();
			IPv4NetID netId = namedGroups.get(id).get(Simulator.getRandom().nextInt(size));
			namedGroups.get(id).remove(netId);
			return newNetLayer(netId);
		} else {
			throw new IllegalStateException(
					"No (more) Hosts are assigned to \"" + id + "\"");
		}
	}

	private GnpNetLayer newNetLayer(IPv4NetID netID) {
		GnpPosition gnpPos = this.hostPool.get(netID).getGnpPosition();
		GeographicPosition geoPos = this.hostPool.get(netID).getGeographicPosition();
		GnpNetLayer nw = new GnpNetLayer(this.subnet, netID, gnpPos, geoPos,
				this.downBandwidth, this.upBandwidth, hostPool.get(netID)
						.getCountryCode());
		hostPool.remove(netID);
		return nw;
	}

	public void setGnpFile(String gnpFileName) {
		File gnpFile = new File(gnpFileName);

		hostPool = new HashMap<IPv4NetID, TopologyProperty>();
		namedGroups = new HashMap<String, ArrayList<IPv4NetID>>();

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
		assert root.getName().equals("gnp");

		for (Object obj : root.elements()) {
			Element elem = (Element) obj;
			if (elem.getName().equals("GroupLockup")) {
				for (Iterator iter = elem.elementIterator("Group"); iter
						.hasNext();) {
					Element variable = (Element) iter.next();
					String id = variable.attributeValue("id");

					ArrayList<IPv4NetID> group = new ArrayList<IPv4NetID>();
					for (Iterator ipIter = variable.elementIterator("IPs"); ipIter
							.hasNext();) {
						Element ipElement = (Element) ipIter.next();
						String[] ips = ipElement.attributeValue("value").split(
								",");
						for (int c = 0; c < ips.length; c++)
							group.add(new IPv4NetID(Long.parseLong(ips[c])));
					}
					if (namedGroups.containsKey(id)) {
						throw new IllegalStateException("Multiple Group Definition in " + gnpFileName + " ( Group: " + id + " )");
					} else {
						namedGroups.put(id, group);
					}
				}
			} else if (elem.getName().equals("Hosts")) {
				for (Iterator iter = elem.elementIterator("Host"); iter
						.hasNext();) {
					Element variable = (Element) iter.next();

					IPv4NetID hostID = new IPv4NetID(Long.parseLong(variable
							.attributeValue("ip")));
					String country = variable.attributeValue("country");
					double longitude = Double.parseDouble(variable
							.attributeValue("longitude"));
					double latitude = Double.parseDouble(variable
							.attributeValue("latitude"));
					String[] coordinatesS = variable.attributeValue(
							"coordinates").split(",");
					double[] coordinatesD = new double[coordinatesS.length];
					for (int c = 0; c < coordinatesD.length; c++)
						coordinatesD[c] = Double.parseDouble(coordinatesS[c]);

					GeographicPosition geoPos = new GeographicPosition(longitude, latitude);
					GnpPosition gnpPos = new GnpPosition(coordinatesD);

					TopologyProperty hostProp = new TopologyProperty(geoPos, gnpPos, country);
					hostPool.put(hostID, hostProp);
				}
			} else if (elem.getName().equals("PingErLockup")) {
				pingErLockup = new PingErLookup();
				pingErLockup.loadFromXML(elem);

			} else if (elem.getName().equals("CountryLockup")) {
				countryLockup = new CountryLookup();
				countryLockup.importFromXML(elem);
			}
		}
	}

	public void setDownBandwidth(double downBandwidth) {
		this.downBandwidth = downBandwidth;
	}

	public void setUpBandwidth(double upBandwidth) {
		this.upBandwidth = upBandwidth;
	}

	public void setLatencyModel(GnpLatencyModel model) {
		model.init(pingErLockup, countryLockup);
		subnet.setLatencyModel(model);
	}

	public void setBandwidthManager(AbstractGnpNetBandwidthManager bm) {
		subnet.setBandwidthManager(bm);
	}

	public void setPbaPeriod(double seconds) {
		subnet.setPbaPeriod(Math.round(seconds * Simulator.SECOND_UNIT));
	}

	private class TopologyProperty {

		private GnpPosition gnpPosition;
		private GeographicPosition geoPosition;
		private String countryCode;

		public TopologyProperty(GeographicPosition geoPos, GnpPosition gnpPos,
				String countryCode) {
			this.gnpPosition = gnpPos;
			this.geoPosition = geoPos;
			this.countryCode = countryCode;
		}

		public GnpPosition getGnpPosition() {
			return gnpPosition;
		}

		public GeographicPosition getGeographicPosition() {
			return geoPosition;
		}

		public String getCountryCode() {
			return countryCode;
		}
	}

	private class CountryProperty {

		private String countryCode; // GeoIP Country Code
		private String countryName; // PingER Country Name
		private String regionName; // PingER Region Name

		public CountryProperty(String countryCode, String countryName,
				String regionName) {
			this.countryCode = countryCode;
			this.countryName = countryName;
			this.regionName = regionName;
		}

		public String getCountryCode() {
			return countryCode;
		}

		public String getCountryName() {
			return countryName;
		}

		public String getRegionName() {
			return regionName;
		}
	}

}
