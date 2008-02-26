package de.tud.kom.p2psim.api.common;

import de.tud.kom.p2psim.api.network.NetLayer;

/**
* A <code>ConnectivityListener</code> must be implemented to support
* notification that the connectivity of a host changed,
* i.e. that the host went online or offline. Components interested in
* the online status should register themselves at host's <code>HostProperty</code>
* as <code>ConnectivityListener</code>s to receive <code>ConnectivityEvent</code>s. 
* 
* @author Konstantin Pussep
* @author Sebastian Kaune
* @version 3.0, 03.12.2007
*
* @see ConnectivityEvent
* @see HostProperties#addConnectivityListener(ConnectivityListener)
* @see NetLayer#addConnectivityListener(ConnectivityListener)
*/
public interface ConnectivityListener extends ComponentEventListener {
	/**
	 * Called when the connectivity of the host change changed.
	 * @param ce - connectivity event
	 */
	public void connectivityChanged(ConnectivityEvent ce);
}
