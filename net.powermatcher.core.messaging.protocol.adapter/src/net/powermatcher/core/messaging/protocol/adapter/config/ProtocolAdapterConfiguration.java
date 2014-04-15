package net.powermatcher.core.messaging.protocol.adapter.config;

import net.powermatcher.core.messaging.protocol.adapter.ProtocolAdapter;



/**
 * Defines the configuration properties, default values and constants for a ProtocolAdapter object.
 * 
 * <p>
 * The interface defines the Protocol type, comprising the following protocols versions supported by this adapter:
 * <ul>
 * <li>Internal V1</li>
 * <li>HAN revision 6</li>
 * </ul>
 * It also defines the logging logging level type, containing the levels:
 * <ul>
 * <li>No logging</li>
 * <li>Partial logging</li>
 * <li>Full logging</li>
 * </ul>
 * 
 * 
 * </p>
 * @author IBM
 * @version 0.9.0
 * 
 * @see ProtocolAdapter
 * @see BaseAdapterConfiguration
 */
public interface ProtocolAdapterConfiguration extends BaseAdapterConfiguration {
	/**
	 * Enumeration of the messaging protocol versions supported by this adapter.
	 */
	public enum Protocol {
		/**
		 * Ordinal 0
		 */
		INTERNAL_v1,
		/**
		 * Ordinal 1
		 */
		HAN_rev6
	}

	/**
	 * Define the protocol property (String) constant.
	 */
	public static final String PROTOCOL_PROPERTY = "messaging.protocol";
	/**
	 * Define the protocol property default (String) constant.
	 */
	public static final String PROTOCOL_PROPERTY_DEFAULT = "INTERNAL_v1";
	/**
	 * Define the protocol description (String) constant.
	 */
	public static final String PROTOCOL_DESCRIPTION = "PowerMatcher messaging protocol";

	/**
	 * Protocol and return the String result.
	 * 
	 * @return Results of the protocol (<code>String</code>) value.
	 */
	public String messaging_protocol();

}
