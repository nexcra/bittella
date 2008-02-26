package de.tud.kom.p2psim.impl.network.gnp.topology;

import java.io.IOException;

import de.tud.kom.p2psim.impl.network.gnp.geoip.Location;
import de.tud.kom.p2psim.impl.network.gnp.geoip.LookupService;

class GeolocatorGeoIP implements Geolocator {

	LookupService cl;

	Location l1;

	public GeolocatorGeoIP() {
		super();
		try {
			cl = new LookupService("measuredData/GeoIP/GeoLiteCity20070901.dat", LookupService.GEOIP_MEMORY_CACHE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getCity() {
		return l1.city;
	}

	public String getCountryCode() {
		return l1.countryCode;
	}

	public String getCountryName() {
		return l1.countryName;
	}

	public double getLatitude() {
		return l1.latitude;
	}

	public double getLongitude() {
		return l1.longitude;
	}

	public String getPostalCode() {
		return l1.postalCode;
	}

	public String getRegion() {
		return l1.region;
	}

	public boolean search(String ip) {
		l1 = cl.getLocation(ip);
		if (l1 != null)
			return true;
		else
			return false;
	}

	public boolean search(Long ip) {
		l1 = cl.getLocation(ip);
		if (l1 != null)
			return true;
		else
			return false;
	}
}
