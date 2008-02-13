package de.tud.kom.p2psim.email;

import java.util.Collection;

import de.tud.kom.p2psim.api.transport.TransInfo;
import de.tud.kom.p2psim.api.transport.TransLayer;
import de.tud.kom.p2psim.api.transport.TransMessageListener;

/**
 * Email client for sending and fetching emails.
 * @author Konstantin Pussep
 * @author Sebastian Kaune
 * @version 3.0, 30.11.2007
 *
 */
public interface EmailClient extends TransMessageListener {
	/**
	 * Link the client with the lower layer, which will be used to send messages
	 * throug the network.
	 * @param transLayer network layer
	 */
	public void setTransLayer(TransLayer transLayer);
	
	/**
	 * Set the address of the server, which will be used to send and fetch emails.
	 * @param serverAddress network address of the server
	 */
	public void setServerAddress(TransInfo serverAddress);

	/**
	 * Send an email on behalf of the user.
	 * @param from - the name of the sending user 
	 * @param to - the name of the user who will receive the message
	 * @param text - the content of the email
	 */
	public void sendEmail(String from, String to, String text);

	/**
	 * Fetch all emails from the server and store them in the client.
	 * @param username - users name (the receiver of emails) 
	 */
	public void fetchEmail(String username);

	/**
	 * List all clients fetched from server and stored locally.
	 * @return list of fetched emails.
	 */
	public Collection<String> listEmails();

}
