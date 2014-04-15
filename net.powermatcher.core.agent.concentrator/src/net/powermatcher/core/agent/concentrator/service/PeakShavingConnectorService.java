package net.powermatcher.core.agent.concentrator.service;


import net.powermatcher.core.adapter.service.ConnectorService;

/**
 * @author IBM
 * @version 0.9.0
 */
public interface PeakShavingConnectorService extends ConnectorService {

	/**
	 * Each subtype of ConnectorService declares a final static String field
	 * with the identifier ADAPTER_FACTORY_PROPERTY_NAME to define the name of the 
	 * configuration property to configure the adapter factory ids for this connector.
	 * @see #getAdapterFactory(Class)
	 */
	final static String ADAPTER_FACTORY_PROPERTY_NAME = "peak.shaving.adapter.factory";

	/**
	 * Bind with the specified peak shaving adapter parameter.
	 * @param peakShavingAdapter
	 *		The peak shaving adapter (<code>PeakShavingNotificationService</code>) parameter.
	 */
	public void bind(final PeakShavingNotificationService peakShavingAdapter);

	/**
	 * Gets the peak shaving agent (PeakShavingService) value.
	 * @return The peak shaving agent (<code>PeakShavingService</code>) value.
	 */
	public PeakShavingService getPeakShavingAgent();

	/**
	 * Unbind with the specified peak shaving adapter parameter.
	 * @param peakShavingAdapter
	 *		The peak shaving adapter (<code>PeakShavingNotificationService</code>) parameter.
	 */
	public void unbind(final PeakShavingNotificationService peakShavingAdapter);

}
