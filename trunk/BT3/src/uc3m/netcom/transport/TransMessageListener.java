package uc3m.netcom.transport;

import de.tud.kom.p2psim.api.common.ComponentEventListener;

/**
 * TransMessageListeners acts as event handlers for incoming TransMsgEvents
 * triggered by the TransLayer. In particular, TransMsgEvents comprises among
 * other things a payload message which in turn comprises data necessary to
 * implement the (virtual) communication between higher layers such as the
 * overlay or application. For instance, the payload field might represent an
 * overlay or application messages.
 * 
 * In other words, the transport layers strips off the header information of the
 * transport message and passes all relevant data to the receiving highler layer
 * in terms of TransMsgEvents.
 * 
 * @author Sebastian Kaune
 * @author Konstantin Pussep
 * @version 3.0, 03.12.2007
 * 
 */
public interface TransMessageListener extends ComponentEventListener {

	/**
	 * Upon receiving a transport message, the transport layer strips off the
	 * header information of this message and passes all relevant data to a
	 * given TransMessageListener in terms of TransMsgEvent.
	 * 
	 * @param receivingEvent
	 *            the TransMsgEvent containing all relevant data
	 */
	public void messageArrived(TransMsgEvent receivingEvent);
}
