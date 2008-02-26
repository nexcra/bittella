package de.tud.kom.p2psim.impl.storage;

import de.tud.kom.p2psim.api.common.ComponentFactory;
import de.tud.kom.p2psim.api.common.Host;

public class StorageFactory implements ComponentFactory {

	public DefaultContentStorage createComponent(Host host) {
		return new DefaultContentStorage();
	}

}
