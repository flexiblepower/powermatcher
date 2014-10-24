package net.powermatcher.core.agent.auctioneer.service;


import net.powermatcher.core.adapter.service.Connectable;

/**
 * @author IBM
 * @version 0.9.0
 */
public interface PricingConnectorService extends Connectable {

	/**
	 * Each subtype of ConnectorService declares a final static String field
	 * with the identifier ADAPTER_FACTORY_PROPERTY_NAME to define the name of the 
	 * configuration property to configure the adapter factory ids for this connector.
	 * @see #getAdapterFactory(Class)
	 */
	final static String ADAPTER_FACTORY_PROPERTY_NAME = "pricing.adapter.factory";

	/**
	 * Bind the pricing service to the auctioneer.
	 * 
	 * @param pricingService
	 *            The pricing service (<code>PricingService</code>) the
	 *            auctioneer should use.
	 */
	public void bind(final PricingService pricingService);

	/**
	 * Gets the auctioneer's pricing control interface (PricingControlService).
	 * 
	 * @return The auctioneer's pricing control interface (
	 *         <code>PricingControlService</code>).
	 */
	public PricingControlService getPricingControlService();

	/**
	 * Unbind the specified pricing service from the auctioneer. If no pricing
	 * service is bound to the auctioneer, the auctioneer will use its default
	 * pricing algortihm.
	 * 
	 * @param pricingService
	 *            The pricing service (<code>PricingService</code>) the
	 *            auctioneer should no longer use.
	 */
	public void unbind(final PricingService pricingService);

}
