/**
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.arch;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * The <code>EventsRegistry</code> class is not in use currently. It is
 * however aimed to incorporate it as a singleton class which will keep all
 * Events possible in the system specified through a config file.
 * 
 * @author Ozair Kafray
 * @version $Id: EventsRegistry.java 258 2006-11-28 13:05:40Z cosmin $
 */
public class EventsRegistry {

	/**
	 * An instance of Logger
	 */
	private static Logger log = Logger.getLogger(EventsRegistry.class);

	/**
	 * The constructor is private so as to ensure that it is not instantiated.
	 * This class is only to be used statically.
	 */
	private EventsRegistry() {
	}

	private static Map<String, EventTopic> eventTopics = null;

	/**
	 * Initializes the Map of Event Topics to EventTypes. The DEPLOYMENT topic
	 * is added by default, other events can be added through eventType file
	 * specified in the startupfile
	 */
	public static void initialize() {

		eventTopics = new HashMap<String, EventTopic>();
		eventTopics.put("DEPLOYMENT", new EventTopic("DEPLOYMENT",
				"Events for deployment of gods agent and controlcenter"));
	}

	public static void populate(String eventsFileName) {

		try {
			InputStream eventsFile = new FileInputStream(eventsFileName);
			Properties events = new Properties();
			events.loadFromXML(eventsFile);
			eventsFile.close();

			Iterator i = events.keySet().iterator();
			String topic = "";
			String description = "";
			while (i.hasNext()) {
				topic = (String) i.next();
				description = events.getProperty(topic);
				log.info(topic + ": " + description);
				eventTopics.put(topic, new EventTopic(topic, description));
			}
		}

		catch (IOException ioe) {
			log.error("EXCEPTION: " + ioe.getMessage());
		}

	}

	/**
	 * @param topic
	 * @return
	 */
	public static EventTopic getEventTopic(String topic) {
		return eventTopics.get(topic);
	}
}
