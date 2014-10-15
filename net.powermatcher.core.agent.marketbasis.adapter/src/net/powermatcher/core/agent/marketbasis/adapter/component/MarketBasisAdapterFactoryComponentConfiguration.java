package net.powermatcher.core.agent.marketbasis.adapter.component;


import net.powermatcher.core.agent.marketbasis.adapter.config.MarketBasisAdapterConfiguration;
import aQute.bnd.annotation.metatype.Meta;
import aQute.bnd.annotation.metatype.Meta.OCD;


/**
 * Defines the configuration interface of a MatcherProtocolAdapterComponent instance (OSGi component).
 * <p>
 * The interface defines the OSGi configuration object properties and property default values
 * for a MatcherProtocolAdapterComponent instance.
 * </p>
 * @author IBM
 * @version 0.9.0
 */
@OCD(name = MarketBasisAdapterFactoryComponentConfiguration.CONFIGURATION_NAME, description = MarketBasisAdapterFactoryComponentConfiguration.CONFIGURATION_DESCRIPTION)
public interface MarketBasisAdapterFactoryComponentConfiguration extends MarketBasisAdapterConfiguration {

	/**
	 * 
	 */
	public final static String CONFIGURATION_NAME = "PowerMatcher Market Basis Adapter Factory";
	/**
	 * 
	 */
	public final static String CONFIGURATION_DESCRIPTION = "Adapter to configure a market basis via the Agent interface of a PowerMatcher component";

	@Override
	@Meta.AD(required = false, deflt = CLUSTER_ID_DEFAULT, description = CLUSTER_ID_DESCRIPTION)
	public String cluster_id();

	@Override
	@Meta.AD(required = true, deflt = AGENT_ADAPTER_FACTORY_DEFAULT, description = ID_DESCRIPTION)
	public String id();

	@Override
	@Meta.AD(required = false, deflt = COMMODITY_DEFAULT, description = COMMODITY_DESCRIPTION)
	public String commodity();

	@Override
	@Meta.AD(required = false, deflt = CURRENCY_DEFAULT, description = CURRENCY_DESCRIPTION)
	public String currency();

	@Override
	@Meta.AD(required = false, deflt = MARKET_REF_DEFAULT_STR, description = MARKET_REF_DESCRIPTION)
	public int market_ref();

	@Override
	@Meta.AD(required = false, deflt = MINIMUM_PRICE_DEFAULT_STR, description = MINIMUM_PRICE_DESCRIPTION)
	public double minimum_price();

	@Override
	@Meta.AD(required = false, deflt = MAXIMUM_PRICE_DEFAULT_STR, description = MAXIMUM_PRICE_DESCRIPTION)
	public double maximum_price();

	@Override
	@Meta.AD(required = false, deflt = PRICE_STEPS_DEFAULT_STR, description = PRICE_STEPS_DESCRIPTION)
	public int price_steps();

	@Override
	@Meta.AD(required = false, deflt = SIGNIFICANCE_DEFAULT_STR, description = SIGNIFICANCE_DESCRIPTION)
	public int significance();

}
