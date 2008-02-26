package de.tud.kom.p2psim.email.solution;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import de.tud.kom.p2psim.api.transport.TransInfo;
import de.tud.kom.p2psim.api.transport.TransLayer;
import de.tud.kom.p2psim.api.transport.TransMsgEvent;
import de.tud.kom.p2psim.api.transport.TransProtocol;
import de.tud.kom.p2psim.email.Email;
import de.tud.kom.p2psim.email.EmailServer;
import de.tud.kom.p2psim.impl.util.logging.SimLogger;

class EmailServerImpl implements EmailServer {
	private static final Logger log = SimLogger.getLogger(EmailClientImpl.class);
	private static final short PORT = 143;
	TransLayer transLayer;
	List<Email> emails = new LinkedList<Email>();
	
	public Collection<String> listEmails() {
		Set<String> texts = new LinkedHashSet<String>();
		for (Email email : emails) {
			texts.add(email.getText());
		}
		return texts;
	}

	public void setTransLayer(TransLayer transLayer) {
		this.transLayer=transLayer;
		transLayer.addTransMsgListener(this, PORT);
	}

	public TransInfo getAddress() {
		return transLayer.getLocalTransInfo(PORT);
	}

	public void messageArrived(TransMsgEvent receivingEvent) {
		Object payload = receivingEvent.getPayload();
		System.err.println("Received: "+payload);
		if (payload instanceof Email) {
			Email email = (Email) payload;
			emails.add(email);
		} if (payload instanceof EmailRequestImpl) {
			EmailRequestImpl req = (EmailRequestImpl) payload;
			String user = req.getUsername();
			Set<Email> reply = new HashSet<Email>();
			for (Email email : emails) {
				if(email.getTo().equals(user)){
					log.debug("email "+email+" match user "+user);
					reply.add(email);
				}
			}
			transLayer.send(new EmailReplyImpl(reply), receivingEvent.getSenderTransInfo(), PORT, TransProtocol.UDP);
		}

		
	}

}
