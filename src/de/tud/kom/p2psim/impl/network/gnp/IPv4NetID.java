package de.tud.kom.p2psim.impl.network.gnp;

import de.tud.kom.p2psim.api.network.NetID;

/**
 * Implementation of the NetID-Interface for IPv4-Addresses 
 * @author Gerald Klunker
 **/
public class IPv4NetID implements NetID {
	
 /**
  * 32bit IP Address (with respect to the algebraic sign of 32bit Integers
  * in a 64bit Long Value).
  */	
 private Long id;

 /**
  * Creates an Instance of IPv4NetID.
  * @param id The Long-ID.
  */
 public IPv4NetID(Long id) {
  this.id = id;
 }
 
 public IPv4NetID(String id) {
	 try {
		 long ip = Long.parseLong(id);
		 this.id = ip;
	 } catch (Exception e) {
		 long ip = IPv4NetID.ipToLong(id);
		 this.id = ip;
	 }
 }

 /**
  * @return The Long-ID.
  */
 public Long getID() {
  return this.id;	 
 }
 
 /**
  * @param obj An object.
  * @return Whether the parameter is equal to this IPv4NetID or not.
  */
 public boolean equals(Object obj) {
  if(obj instanceof IPv4NetID) {
   return getID().equals(((IPv4NetID)obj).getID());
  }
  else {
   return false;	  
  }
 }

 /**
  * @return The hashcode of this IPv4NetID.
  */
 public int hashCode() {
  return this.id.hashCode();
 }

 /**
  * @return A string representing this InternetProtocolNetID.
  */
 public String toString() {
  return IPv4NetID.ipToString(this.id);
 }
 
 	// ToDo Exceptions bei ungültigen Parameterwerten schmeißen
 
	/**
	 * @param ip 32bit IP-Address
	 * @return A readable IP-String like "192.168.0.1"
	 */
	 public static String ipToString(Long ip) {
		String returnString = "";
		returnString += Long.toString( ( ip << 32 ) >>> 56 ) + ".";
		returnString += Long.toString( ( ip << 40 ) >>> 56 ) + ".";
		returnString += Long.toString( ( ip << 48 ) >>> 56 ) + ".";
		returnString += Long.toString( ( ip << 56 ) >>> 56 );
		return returnString;
	}
	
 	/**
 	 * 
 	 * @param ip readable IP-String like "192.168.0.1"
 	 * @return A 32bit IP-Address
 	 */
	public static Long ipToLong(String ip) {
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
}