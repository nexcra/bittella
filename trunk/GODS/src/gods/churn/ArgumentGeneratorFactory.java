/*
 * Global Observatory for Distributed Systems (GODS)
 * An ecosystem for the evaluation and study
 * of large-scale distributed and dynamic systems
 * 
 * Copyright (c) 2006-2007, all rights reserved 
 * 		Royal Institute of Technology (KTH)
 * 		Swedish Institute of Computer Science (SICS)
 */
package gods.churn;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The <code>ArgumentGeneratorsFactory</code> class implements the Factory for
 * ArgumentGenerators as in Factory pattern. The
 * {@link gods.churn.AbstractArgumentGenerator}(s) can only be instantiated
 * through this factory class. If an already instantiated ArgumentGenerator is
 * required again it will be reused after being reinitialized with the new
 * params file. The Factory identifies ArgumentGenerators based on their display
 * names specified in gods.churn.argumentgenerators.file NOT fully qualified
 * Java names.
 * 
 * @author Ozair Kafray
 * @version $Id: org.eclipse.jdt.ui.prefs 258 2006-11-28 13:05:40Z cosmin $
 */
public final class ArgumentGeneratorFactory {

	/**
	 * An instance of logger
	 */
	private static Logger log = Logger
			.getLogger(ArgumentGeneratorFactory.class);

	static {
		log.warn("LOOOOOOO*****OOOADED");
	}

	/**
	 * The parameters required to construct an EventHandler
	 */
	private static Class[] generatorConstructorParameters = { String.class };

	/**
	 * A hashmap of java classNames of ArgumentGenerators against their
	 * displayNames as specified in gods.churn.argumentgenerators.file in gods
	 * config file
	 */
	private static Map<String, String> argGenNames = new HashMap<String, String>();

	/**
	 * A hashmap of displayNames of ArgumentGenerators to their references if
	 * they exist
	 */
	private static Map<String, AbstractArgumentGenerator> argumentGenerators = new HashMap<String, AbstractArgumentGenerator>();

	/**
	 * @return ArgumentGenerator for the specified displayName
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static AbstractArgumentGenerator getArgumentGenerator(
			String displayName, String parametersFileName)
			throws FileNotFoundException, IOException {

		AbstractArgumentGenerator argumentGenerator = null;

		if (argumentGenerators.containsKey(displayName)) {
			argumentGenerator = argumentGenerators.get(displayName);
			argumentGenerator.reinitialize(parametersFileName);
			log.debug(displayName
					+ " generator already there and reinitialized");
		} else {
			log.debug(displayName + " generator not created before");
			try {
				String className = argGenNames.get(displayName);
				if (className == null) {

					log.error("ERROR: Arggen class is NULL");
					log.error("Following should be confirmed");
					log
							.error("1. The argument generator is specified in "
									+ "gods.churn.arggens.xml along with its display name");

					log.error("2. The ArgumentGeneratorFactory has been "
							+ "initialized with this file");
				}
				log.info("ArgGen class is: " + className);
				Class argGenClass = Class.forName(className);

				log.debug(className + " class for " + displayName + " FOUND.");

				Constructor c = argGenClass
						.getConstructor(generatorConstructorParameters);

				log.debug(c.toGenericString());

				argumentGenerator = (AbstractArgumentGenerator) c
						.newInstance(parametersFileName);

				log.debug(" ArgGen reference:" + argumentGenerator);

				log.debug(className + " class for " + displayName
						+ " INSTANTIATED.");

				argumentGenerators.put(displayName, argumentGenerator);

				log.debug(className + " class for " + displayName
						+ " ADDED to ARGGENS.");

			} catch (ClassNotFoundException cnfe) {
				log
						.error("CLASSNOTFOUNDEXCEPTION: "
								+ cnfe.getMessage()
								+ "Please verify that the specified ArgumentGenerator child class' name and that it is in the classpath");
			} catch (IllegalAccessException iae) {
				log
						.error("EXCEPTION: IllegalAccess"
								+ iae.getMessage()
								+ "Please verify that in your ArgumentGenerator a constructor accepting a String specifying 'parametersFileName' exists and has public access specifier");

			} catch (InstantiationException ie) {
				log
						.error("EXCEPTION: Instantiation"
								+ ie.getMessage()
								+ "Please verify that in your ArgumentGenerator a constructor accepting a String specifying 'parametersFileName' exists");

			} catch (NoSuchMethodException nsme) {
				log
						.error("EXCEPTION: NoSuchMethod"
								+ nsme.getMessage()
								+ "Please verify that in your ArgumentGenerator a constructor accepting a String specifying 'parametersFileName' exists");

			} catch (InvocationTargetException ite) {
				log.error("EXCEPTION: InvocationTarget "
						+ ite.getCause().getMessage());

			} catch (ClassCastException cce) {
				log.debug("EXCEPTION: ClassCast" + cce.getMessage());

			}
		}
		return argumentGenerator;
	}

	/**
	 * @param argGensFileName
	 */
	public static void initialize(String argGensFileName) {
		try {
			
			log.debug("Argument generators listed in file: " + argGensFileName);
			
			FileInputStream inputStream = new FileInputStream(argGensFileName);
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder parser = factory.newDocumentBuilder();

			Document d = parser.parse(inputStream);

			Element e = d.getDocumentElement();

			NodeList n = e.getElementsByTagName("argumentgenerator");

			log.warn("INITIALIIIIIIIIIIIIIIZE+" + argGenNames);
			
			// For all argumentgenerators
			for (int i = 0; i < n.getLength(); i++) {
				Element x = (Element) n.item(i);

				// Get ArgumentGenerator displayName
				String displayName = x.getAttribute("displayName");
				// Get ArgumentGenerator className
				String className = x.getTextContent();

				log.debug("displayName: " + displayName + " for ArgGen: "
						+ className);

				argGenNames.put(displayName, className);
				
				log.warn(displayName);
			}
			
			log.warn("INITIALIZED2:"+ argGenNames + " +" +argGenNames.hashCode());
			
			inputStream.close();

		} catch (IOException ioe) {
			log.error("IOException: " + ioe.getMessage());

		} catch (ParserConfigurationException pce) {
			log.error("ParserConfigurationException: " + pce.getMessage());

		} catch (SAXException se) {
			log.error("SAXException: " + se.getMessage());

		}
	}

	public static String[] getAllDisplayNames() {
		String[] displayNames = new String[argGenNames.size()];

		displayNames = argGenNames.keySet().toArray(displayNames);

		log.info("BECALI" + displayNames.length + " +" + argGenNames + " +" +argGenNames.hashCode());
		
		return displayNames;
	}
}
