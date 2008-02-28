package de.tud.kom.p2psim.api.common;

import de.tud.kom.p2psim.api.network.NetLayer;

/**
 * Event which notifies that the connectivity of a host changed,
 * i.e. that the host went online or offline. Components interested in
 * the online status should register themselves at host's <code>HostProperty</code>
 * as <code>ConnectivityListener</code>s. 
 * 
 * @author Konstantin Pussep
 * @author Sebastian Kaune
 * @version 3.0, 03.12.2007
 *
 * @see ConnectivityListener
 * @see HostProperties#addConnectivityListener(ConnectivityListener)
 * @see NetLayer#addConnectivityListener(ConnectivityListener)
 */
public class ConnectivityEvent extends ComponentEvent {

	private boolean isOnline;
	
	/**
	 * Creates new immutable connectivity event. 
	 * @param source - source of the connectivity event
	 * @param isOnline - what happened: true => went online, false => went offline
	 */
	public ConnectivityEvent(Object source, boolean isOnline) {
		super(source);
		this.isOnline=isOnline;
	}

	/**
	 * @return whether this peer went online
	 */
	public boolean isOnline(){
		return isOnline;
	}
	
	/**
	 * @return whether this peer went offline
	 */
	public boolean isOffline(){
		return !isOnline;
	}
}
