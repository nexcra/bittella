/**
 * 
 */
package de.tud.kom.p2psim.email;

import de.tud.kom.p2psim.api.common.Message;

public class Email implements Message{
	private String from, to, text;

	public Email(String from, String to, String text) {
		super();
		this.from = from;
		this.to = to;
		this.text = text;
	}

	public String getFrom() {
		return from;
	}

	public String getText() {
		return text;
	}

	public String getTo() {
		return to;
	}

	@Override
	public String toString() {
		return "Email("+from+" -> "+to+": "+text+")";
	}

	public Message getPayload() {
		return null;
	}

	public long getSize() {
		return 0;
	}
	
}