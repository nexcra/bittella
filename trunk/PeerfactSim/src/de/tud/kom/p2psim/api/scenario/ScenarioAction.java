package de.tud.kom.p2psim.api.scenario;

/**
 * Represents an action which can be specified in the action file and will
 * be used to pre-initialize the simulator queue. 
 * 
 * @author Konstantin Pussep
 * @author Sebastian Kaune
 * @version 3.0, 14.12.2007
 *
 */
public interface ScenarioAction {

	/**
	 * Schedule this action in the simulator queue. 
	 */
	public void schedule();

}