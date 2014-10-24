package net.powermatcher.core.agent.objective.service;


import net.powermatcher.core.adapter.service.Connectable;

/**
 * @author IBM
 * @version 0.9.0
 */
public interface ObjectiveConnectorService extends Connectable {

	/**
	 * Each subtype of ConnectorService declares a final static String field
	 * with the identifier ADAPTER_FACTORY_PROPERTY_NAME to define the name of the 
	 * configuration property to configure the adapter factory ids for this connector.
	 * @see #getAdapterFactory(Class)
	 */
	final static String ADAPTER_FACTORY_PROPERTY_NAME = "objective.adapter.factory";

	/**
	 * Bind the objective adapter to the objective agent.
	 * 
	 * @param notificationService
	 *            The notification service (<code>ObjectiveNotificationService</code>) the
	 *            objective agent should use.
	 */
	public void bind(final ObjectiveNotificationService notificationService);

	/**
	 * Gets the objective agent's control interface (ObjectiveControlService).
	 * 
	 * @return The objective agent's control interface (
	 *         <code>ObjectiveControlService</code>).
	 */
	public ObjectiveControlService getObjectiveControlService();

	/**
	 * Unbind the objective adapter from the objective agent.
	 * This also causes the objective agent to revert to sending out its configured
	 * default bid.
	 * 
	 * @param notificationService
	 *            The notification service (<code>ObjectiveNotificationService</code>) to be unbound.
	 */
	public void unbind(final ObjectiveNotificationService notificationService);

}
