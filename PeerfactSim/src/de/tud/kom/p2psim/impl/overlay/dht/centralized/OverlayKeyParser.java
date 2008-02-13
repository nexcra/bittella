package de.tud.kom.p2psim.impl.overlay.dht.centralized;

import de.tud.kom.p2psim.api.overlay.OverlayKey;
import de.tud.kom.p2psim.impl.scenario.Parser;

public class OverlayKeyParser implements Parser {

	public Class getType() {
		return OverlayKey.class;
	}

	public Object parse(String stringValue) {
		return new OverlayKeyImpl(stringValue);
	}

}
