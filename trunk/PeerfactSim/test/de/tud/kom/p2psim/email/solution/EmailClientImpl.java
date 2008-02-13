package de.tud.kom.p2psim.email.solution;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import de.tud.kom.p2psim.api.transport.TransInfo;
import de.tud.kom.p2psim.api.transport.TransLayer;
import de.tud.kom.p2psim.api.transport.TransMsgEvent;
import de.tud.kom.p2psim.api.transport.TransProtocol;
import de.tud.kom.p2psim.email.Email;
import de.tud.kom.p2psim.email.EmailClient;
import de.tud.kom.p2psim.impl.util.logging.SimLogger;

/**
 * Example solution.
 * 
 * @author Konstantin Pussep
 * @author Sebastian Kaune
 * @version 3.0, 30.11.2007
 * 
 */
public class EmailClientImpl implements EmailClient {
	private static final Logger log = SimLogger.getLogger(EmailClientImpl.class);

	TransLayer transLayer;

	private TransInfo serverAddress;

	static short port = 100;

	Set<String> emails = new HashSet<String>();

	public void sendEmail(String from, String to, String text) {
		transLayer.send(new Email(from, to, text), serverAddress, port, TransProtocol.UDP);
	}

	public void setServerAddress(TransInfo serverAddress) {
		this.serverAddress = serverAddress;
	}

	public void setTransLayer(TransLayer transLayer) {
		this.transLayer = transLayer;
		transLayer.addTransMsgListener(this, port);
	}

	public void fetchEmail(String username) {
		transLayer.send(new EmailRequestImpl(username), serverAddress, port, TransProtocol.UDP);
	}

	public Collection<String> listEmails() {
		return Collections.unmodifiableSet(emails);
	}

	public void messageArrived(TransMsgEvent receivingEvent) {
		log.info("Client received " + receivingEvent.getPayload());
		Object payload = receivingEvent.getPayload();
		if (payload instanceof EmailReplyImpl) {
			EmailReplyImpl reply = (EmailReplyImpl) payload;
			for (Email email : reply.getEmails()) {
				emails.add(email.getText());
			}
		}
	}

}
