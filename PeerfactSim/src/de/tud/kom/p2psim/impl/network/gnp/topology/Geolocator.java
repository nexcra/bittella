package de.tud.kom.p2psim.impl.network.gnp.topology;

public interface Geolocator {

	public boolean search(String ip);

	public boolean search(Long ip);

	public double getLatitude();

	public double getLongitude();

	public String getCountryCode();

	public String getCountryName();

	public String getRegion();

	public String getCity();

	public String getPostalCode();

}
