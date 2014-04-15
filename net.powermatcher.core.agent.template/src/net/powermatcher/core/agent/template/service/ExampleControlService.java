package net.powermatcher.core.agent.template.service;



/**
 * This interface defines an example control interface that is provided by an agent to an adapter
 * via the agent's connector interface.
 * 
 * @see ExampleConnectorService
 * @see ExampleNotificationService
 * 
 * @author IBM
 * @version 0.9.0
 */
public interface ExampleControlService {

	/**
	 * Control message from adapter to agent.
	 * Adapter will invoke this method when it receives the test message 
	 * it sends to itself.
	 * 
	 * @see ExampleNotificationService#somethingChanged()
	 */
	public void doSomething();

}
