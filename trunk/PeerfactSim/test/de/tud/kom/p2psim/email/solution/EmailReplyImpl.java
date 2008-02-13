package de.tud.kom.p2psim.email.solution;

import java.util.Set;

import de.tud.kom.p2psim.api.common.Message;
import de.tud.kom.p2psim.email.Email;

class EmailReplyImpl implements Message{
	public String toString() {
		return "Reply ["+emails+"]";
	}
	private final Set<Email> emails;
	EmailReplyImpl(Set<Email> emails) {
		super();
		this.emails = emails;
	}
	Set<Email> getEmails() {
		return emails;
	}
	public Message getPayload() {
		return null;
	}
	public long getSize() {
		return 0;
	}
	
}
