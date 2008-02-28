package de.tud.kom.p2psim.impl.overlay.cd;

import de.tud.kom.p2psim.api.storage.Document;
import de.tud.kom.p2psim.impl.scenario.Parser;

public class DocumentParser implements Parser {

	public Class getType() {
		return Document.class;
	}

	public Object parse(String stringValue) {
		return new DocumentImpl(stringValue);
	}

}
