package de.tud.kom.p2psim.email.solution;

import de.tud.kom.p2psim.api.common.Message;

class EmailRequestImpl implements Message{
	private final String username;

	EmailRequestImpl(String username) {
		super();
		this.username = username;
	}

	String getUsername() {
		return username;
	}

	@Override
	public String toString() {
		return "Fetch request from user "+username;
	}

	public Message getPayload() {
		return null;
	}

	public long getSize() {
		return username.length();
	}
	
	
}
