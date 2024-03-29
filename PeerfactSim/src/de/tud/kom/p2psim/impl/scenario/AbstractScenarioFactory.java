package de.tud.kom.p2psim.impl.scenario;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.tud.kom.p2psim.api.common.Host;
import de.tud.kom.p2psim.api.scenario.Composable;
import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.api.scenario.Configurator;
import de.tud.kom.p2psim.api.scenario.HostBuilder;
import de.tud.kom.p2psim.api.scenario.ScenarioFactory;


/**
 * Abstract factory which provides common functionality for XML-based and CSV-based action factories. 
 * 
 * @author Konstantin Pussep
 * @author Sebastian Kaune
 * @version 3.0, 14.12.2007
 *
 */
public abstract class AbstractScenarioFactory implements ScenarioFactory, Composable{

	protected HostBuilder hostBuilder;
//	Map<String, List<Host>> allHosts;
	protected Class defaultComponentClass;
	protected List<Class> additionalClasses = new LinkedList<Class>();
	protected List<Parser> paramParsers = new LinkedList<Parser>();
	ExtendedScenario scenario;

	AbstractScenarioFactory() {
		super();
	}

	public void compose(Configurator config) {
		hostBuilder = (HostBuilder) config.getConfigurable(Configurator.HOST_BUILDER);
	}

	/**
	 * Default target class to execute scenario actions. 
	 * @param componentClass - class of the component that will be used.
	 */
	public void setComponentClass(Class componentClass) {
		this.defaultComponentClass = componentClass;
	}

	/**
	 * Alternative target classes which can be specified for actions via "TargetClass:methodName"
	 * where "TargetClass" will be the name of the class without the package.
	 * @param classes - array of classes (in config file as strings separated by ";"
	 */
	public void setAdditionalClasses(Class[] classes) {
		this.additionalClasses = Arrays.asList(classes);
	}

	/**
	 * Set parser classes which will be used to convert parameter values for action methods. Note that this
	 * is not necessary for primitive types, strings and Classes.
	 * @param parser - parser to parse method parameters
	 */
	public void setParamParser(Parser parser) {
		this.paramParsers.add(parser);
	}

	/**
	 * Create and pre-initialize a new scenario instance.
	 * @return
	 */
	protected ExtendedScenario newScenario() {
	//		if (scenario != null)
	//			return scenario;
			assert scenario == null : "Scenario already exists!";
			scenario = new ExtendedScenario(defaultComponentClass, additionalClasses, paramParsers);
			
			if (hostBuilder == null)
				throw new ConfigurationException("HostBuilder must be specified for actions to be created");
			
			Map<String, List<Host>> allHosts = hostBuilder.getAllHostsWithGroupIDs();
			scenario.setHosts(allHosts);
			return scenario;
		}

}