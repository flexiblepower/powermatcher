package net.powermatcher.core.agent.template.service;


import net.powermatcher.core.adapter.service.Connectable;

/**
 * This interface defines a custom connector service interface that can be used by
 * an adapter to connect to and interface with this agent.
 * <p>
 * In this example the adapter binds a notification interface via the
 * connector to the agent, and obtains a control interface from the connector
 * to control the agent. The agent uses the notification interface to notify the adapter. 
 * </p>
 * <p>
 * An agent may support binding of multiple adapters to a single connector, but the example
 * agent in this project supports only 0 or 1 adapter. Although the agent in this example
 * provides only one custom connector, an agent may define multiple connectors. Note that for
 * example the PowerMatcher protocol has also been separated from the agent via a connector. 
 * </p>
 * 
 * @see ExampleControlService
 * @see ExampleNotificationService
 * 
 * @author IBM
 * @version 0.9.0
 */
public interface ExampleConnectorService extends Connectable {

	/**
	 * Each subtype of ConnectorService declares a final static String field
	 * with the identifier ADAPTER_FACTORY_PROPERTY_NAME to define the name of the 
	 * configuration property to configure the adapter factory ids for this connector.
	 * @see #getAdapterFactory(Class)
	 */
	final static String ADAPTER_FACTORY_PROPERTY_NAME = "example.adapter.factory";

	/**
	 * Bind an adapter's notification interface to the agent.
	 * @param exampleAdapter
	 *		The example adapter (<code>ExampleNotificationService</code>) implementing the notification service.
	 *
	 * @see #bind(ExampleNotificationService)
	 */
	public void bind(final ExampleNotificationService exampleAdapter);

	/**
	 * Gets the agent's control interface (ExampleControlService).
	 * @return The control service interface (<code>ExampleControlService</code>) of the agent.
	 */
	public ExampleControlService getExampleService();

	/**
	 * Unbind an adapter's notification interface from the agent.
	 * @param exampleAdapter
	 *		The example adapter (<code>ExampleNotificationService</code>) implementing the notification service.
	 *
	 * @see #unbind(ExampleNotificationService)
	 */
	public void unbind(final ExampleNotificationService exampleAdapter);

}
