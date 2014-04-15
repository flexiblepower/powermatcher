package net.powermatcher.core.agent.template.config;


import net.powermatcher.core.agent.framework.config.AgentConfiguration;
import net.powermatcher.core.agent.template.ExampleAgent1;
import net.powermatcher.core.object.config.ActiveObjectConfiguration;

/**
 * This interface defines the configuration properties and their default values (if any) for
 * PowerMatcher agent <code>ExampleAgent1</code>.
 * <p>
 * <code>ExampleAgent1</code> supports the configuration of the price and demand of the fixed
 * bid that the agent publishes. Each PowerMatcher agent also supports the properties defined by
 * <code>ConnectableObjectConfiguration</code> and <code>ActiveObjectConfiguration</code>,
 * as inherited from <code>AgentConfiguration</code>.
 * </p><p>
 * Unless configuring a value for a property is mandatory, a default value is defined here.<br>
 * </p><p>
 * An access method with the name of the property is defined to allow the property
 * to be exported as OSGi configuration metatype via annotations. The rule is that
 * the name of the access method must be the same as the key property, with a '.' replaced
 * by a '_' due to restrictions for method names in Java.
 * </p><p>
 * A default value is always also defined as a string literal to allow the default
 * value to be referenced in OSGi configuration meta type annotations.
 * </p>
 * 
 * @see ExampleAgent1
 * @see AgentConfiguration
 * 
 * @author IBM
 * @version 0.9.0
 */
public interface ExampleAgent1Configuration extends AgentConfiguration {
	/**
	 * Define the bid price property (String) constant.
	 */
	public static final String BID_PRICE_PROPERTY = "bid.price";
	/**
	 * Define the bid price default (double) constant.
	 */
	public static final double BID_PRICE_DEFAULT = 0.50;
	/**
	 * Define the bid price default (String) constant.
	 * The default value is also defined as a string literal to allow the default
	 * value to be referenced in OSGi configuration meta type annotations.
	 */
	public static final String BID_PRICE_DEFAULT_STR = "0.50";
	/**
	 * Define the bid price description (String) constant.
	 */
	public static final String BID_PRICE_DESCRIPTION = "Constant bid price (floating point)";
	/**
	 * Define the bid power property (String) constant.
	 */
	public static final String BID_POWER_PROPERTY = "bid.power";
	/**
	 * Define the bid power default (double) constant.
	 */
	public static final double BID_POWER_DEFAULT = 100.0;
	/**
	 * Define the bid power default str (String) constant.
	 */
	public static final String BID_POWER_DEFAULT_STR = "100";
	/**
	 * Define the bid power description (String) constant.
	 */
	public static final String BID_POWER_DESCRIPTION = "Constant bid demand (floating point)";

	/**
	 * Access method for the <code>bid.price</code> property.
	 * This method is not implemented anywhere, but provides the signature for OSGi metatype annotations.
	 * 
	 * @return The value configured for the <code>bid.price</code> property.
	 */
	public double bid_price();

	/**
	 * Access method for the <code>bid.power</code> property.
	 * This method is not implemented anywhere, but provides the signature for OSGi metatype annotations.
	 * 
	 * @return The value configured for the <code>bid.power</code> property.
	 */
	public double bid_power();

}
