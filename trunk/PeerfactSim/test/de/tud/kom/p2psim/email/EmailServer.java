package de.tud.kom.p2psim.email;

import java.util.Collection;

import de.tud.kom.p2psim.api.transport.TransInfo;
import de.tud.kom.p2psim.api.transport.TransLayer;
import de.tud.kom.p2psim.api.transport.TransMessageListener;

/**
 * Email server ccan store emails. All communication with the server
 * should take place over the network.
 * 
 * @author Konstantin Pussep
 * @author Sebastian Kaune
 * @version 3.0, 30.11.2007
 *
 */
public interface EmailServer extends TransMessageListener{
	/**
	 * By default the server will listen at this port for incoming connection requests.
	 */
	public final static short DEFAULT_PORT = 25; 
	/**
	 * Set the link to the network layer.
	 * @param transLayer - network layer
	 */
	public void setTransLayer(TransLayer transLayer);

	/**
	 * List all e-mails which were stored on the server but not fetched by the users yet. 
	 * @return list of e-mails (e-mail text only)
	 */
	public Collection<String> listEmails();

	/**
	 * @return the address at which the server waits for incoming requests
	 */
	public TransInfo getAddress();
	
}
