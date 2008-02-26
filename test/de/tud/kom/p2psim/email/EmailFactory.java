package de.tud.kom.p2psim.email;

public interface EmailFactory {

	public EmailClient createClient();

	public EmailServer createServer();

}
