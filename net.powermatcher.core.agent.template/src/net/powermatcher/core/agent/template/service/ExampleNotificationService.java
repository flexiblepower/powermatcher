package net.powermatcher.core.agent.template.service;


/**
 * This interface defines an example notification interface that an adapter can bind to an agent 
 * via the agent's connector interface.
 * 
 * @see ExampleConnectorService
 * @see ExampleControlService
 * 
 * @author IBM
 * @version 0.9.0
 */
public interface ExampleNotificationService {

	/**
	 * Notification message from agent to adapter.
	 * Adapter will send test message to itself for this event.
	 * 
	 * @see ExampleControlService#doSomething()
	 */
	public void somethingChanged();

}
