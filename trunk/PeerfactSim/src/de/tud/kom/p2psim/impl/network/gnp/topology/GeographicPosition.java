package de.tud.kom.p2psim.impl.network.gnp.topology;

import de.tud.kom.p2psim.api.network.NetPosition;

public class GeographicPosition implements NetPosition {

	double EARTH_DIAMETER = 2 * 6378.2;

	double PI = 3.14159265;

	double RAD_CONVERT = PI / 180;

	private double latitude; // Breite / Y

	private double longitude; // LŠnge / X

	public GeographicPosition(double longitude, double latitude) {
		this.longitude = longitude;
		this.latitude = latitude;
	}

	public double getDistance(NetPosition point) {

		double lat1 = latitude * RAD_CONVERT;
		double lat2 = ((GeographicPosition) point).getLatitude() * RAD_CONVERT;

		double delta_lat = lat2 - lat1;
		double delta_lon = (((GeographicPosition) point).getLongitude() - longitude) * RAD_CONVERT;

		double temp = Math.pow(Math.sin(delta_lat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(delta_lon / 2), 2);
		return EARTH_DIAMETER * Math.atan2(Math.sqrt(temp), Math.sqrt(1 - temp));

	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

}
