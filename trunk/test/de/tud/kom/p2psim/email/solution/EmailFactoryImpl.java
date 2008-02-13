package de.tud.kom.p2psim.email.solution;

import de.tud.kom.p2psim.email.EmailClient;
import de.tud.kom.p2psim.email.EmailFactory;
import de.tud.kom.p2psim.email.EmailServer;

/**
 * Concrete implementation of an Email system can be created via this factory.
 * @author Konstantin Pussep
 * @author Sebastian Kaune
 * @version 3.0, 06.12.2007
 *
 */
public class EmailFactoryImpl implements EmailFactory{
	public EmailClient createClient() {
		return new EmailClientImpl();
	}


	public EmailServer createServer() {
		return new EmailServerImpl();
	}

}
