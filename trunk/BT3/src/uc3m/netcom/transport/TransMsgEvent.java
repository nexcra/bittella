/**
 * 
 */
package uc3m.netcom.transport;

import java.util.EventObject;

import uc3m.netcom.overlay.bt.message.BTMessage;
import de.tud.kom.p2psim.impl.transport.AbstractTransMessage;

/**
 * TransMsgEvent comprises data necessary to implement the virtual communication
 * between higher layers which are located above the TransLayer in the procotol
 * stack (such as overlays or applications). That is, the message decapsulation
 * process is done using TransMsgEvent as all necessary data is passed from the
 * TransLayer to the above registered layers which implement the
 * {@link de.tud.kom.p2psim.api.transport#TransMessageListener} interface.
 * 
 * @author Sebastian Kaune
 * @author Konstantin Pussep
 * @version 3.0, 11/29/2007
 * 
 */
public class TransMsgEvent extends EventObject {

	private TransInfo sender;

	private TransProtocol protocol;

	private int commId;

	private BTMessage payload;

	/**
	 * Constructs a TransMsgEvent
	 * 
	 * @param msg
	 *            the received transport message
	 * @param sender
	 *            the TransInfo of the sender
	 * @param source
	 *            the source of this event
	 */
	public TransMsgEvent(AbstractTransMessage msg, TransInfo sender, TransLayer source) {
		super(source);
		this.protocol = (TransProtocol) msg.getProtocol();
		this.commId = msg.getCommId();
		this.sender = sender;
		this.payload = (BTMessage) msg.getPayload();
	}

	/**
	 * Returns the TransInfo of the sender
	 * 
	 * @return the TransInfo of the sender
	 */
	public TransInfo getSenderTransInfo() {
		return sender;
	}

	/**
	 * Returns the used transport protocol
	 * 
	 * @return the used transport protocol
	 */
	public TransProtocol getProtocol() {
		return protocol;
	}

	/**
	 * Returns the unique communication identifier. This method should be
	 * invoked by TransMessageListener which send replies to specific request by
	 * using the
	 * {@link TransLayer#sendReply(Message, TransMsgEvent, short, TransProtocol)}
	 * method.
	 * 
	 * @return the unique communication identifier of this message
	 */
	public int getCommId() {
		return commId;
	}

	/**
	 * Returns the data which was encapsulated in the transport message
	 * 
	 * @return the data which was encapsulated in the transport message
	 */
	public BTMessage getPayload() {
		return payload;
	}

}
