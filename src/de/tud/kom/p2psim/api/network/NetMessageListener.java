package de.tud.kom.p2psim.api.network;

import de.tud.kom.p2psim.api.common.ComponentEventListener;

/**
 * NetMessage listeners acts as event handlers for incoming NetMsgEvents
 * triggered by the NetLayer. In particular, NetMsgEvents comprises among other
 * things a payload message which in turn comprises data necessary for the
 * transport layer. In other words, the network layers strips off the header
 * information of the network message and passes all relevant data to transport
 * layer in terms of NetMsgEvents.
 * 
 * @author Sebastian Kaune
 * @author Konstantin Pussep
 * @version 3.0, 11/29/2007
 * 
 */
public interface NetMessageListener extends ComponentEventListener {
	/**
	 * Upon receiving a NetMessage, the NetLayer strips off the header
	 * information of this message and passes all relevant data to a given
	 * NetMessageListener in terms of NetMsgEvents.
	 * 
	 * @param nme
	 *            the NetMsgEvent passed from the NetLayer
	 * 
	 */
	public void messageArrived(NetMsgEvent nme);
}
