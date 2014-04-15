package net.powermatcher.core.messaging.framework.config;

import net.powermatcher.core.adapter.config.AdapterConfiguration;
import net.powermatcher.core.messaging.service.MessagingConnectorService;


/**
 * Defines the interface of a messaging adapter.
 * 
 * <p>
 * A MessagingAdapterConfiguration defines configuration settings for a MessagingAdapter
 * instance. The interface currently only extends the AdapterConfiguration and does not
 * any new members. 
 * </p>
 * @author IBM
 * @version 0.9.0
 */
public interface MessagingAdapterConfiguration extends AdapterConfiguration {

	/**
	 * Define the messaging adapter factory default (String) constant.
	 */
	public static final String MESSAGING_ADAPTER_FACTORY_PROPERTY = MessagingConnectorService.ADAPTER_FACTORY_PROPERTY_NAME;
	/**
	 * Define the messaging adapter factory description (String) constant.
	 */
	public static final String MESSAGING_ADAPTER_FACTORY_DEFAULT = "mqttv3ConnectionFactory";
	/**
	 * Define the messaging adapter factory description (String) constant.
	 */
	public static final String MESSAGING_ADAPTER_FACTORY_DESCRIPTION = "The adapter factory for creating the messaging connection";

	/**
	 * Messaging_adapter_factory and return the String result.
	 * 
	 * @return Results of the messaging_adapter_factory (<code>String</code>) value.
	 */
	public String messaging_adapter_factory();
	
}
