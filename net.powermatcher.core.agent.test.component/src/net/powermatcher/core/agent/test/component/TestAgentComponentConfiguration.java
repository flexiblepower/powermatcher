package net.powermatcher.core.agent.test.component;


import aQute.bnd.annotation.metatype.Meta;
import aQute.bnd.annotation.metatype.Meta.OCD;

/**
 * @author IBM
 * @version 0.9.0
 */
@OCD(name = "PowerMatcher Test Agent")
public interface TestAgentComponentConfiguration extends net.powermatcher.core.agent.test.config.TestAgentConfiguration {

	@Override
	@Meta.AD(required = false, deflt = CLUSTER_ID_DEFAULT, description = CLUSTER_ID_DESCRIPTION)
	public String cluster_id();

	@Override
	@Meta.AD(required = true, description = ID_DESCRIPTION)
	public String id();

	@Override
	@Meta.AD(required = false, deflt = ENABLED_DEFAULT_STR, description = ENABLED_DESCRIPTION)
	public boolean enabled();

	@Override
	@Meta.AD(required = false, deflt = MAXIMUM_POWER_DEFAULT_STR)
	public double maximum_power();

	@Override
	@Meta.AD(required = false, deflt = MINIMUM_PRICE_DEFAULT_STR)
	public int minimum_price();

	@Override
	@Meta.AD(required = false, deflt = MINIMUM_POWER_DEFAULT_STR)
	public double minimum_power();

	@Override
	@Meta.AD(required = false, deflt = MAXIMUM_PRICE_DEFAULT_STR)
	public int maximum_price();

	@Override
	@Meta.AD(required = false, deflt = STEPS_DEFAULT_STR)
	public int steps();

	@Override
	@Meta.AD(required = false, deflt = UPDATE_INTERVAL_DEFAULT_STR, description = UPDATE_INTERVAL_DESCRIPTION)
	public int update_interval();

	@Override
	@Meta.AD(required = false, deflt = PARENT_MATCHER_ID_DEFAULT_STR, description = PARENT_MATCHER_ID_DESCRIPTION)
	public String matcher_id();

	@Override
	@Meta.AD(required = false, deflt = TIME_ADAPTER_FACTORY_DEFAULT, description = TIME_ADAPTER_FACTORY_DESCRIPTION)
	public String time_adapter_factory();
	
	@Override
	@Meta.AD(required = false, deflt = SCHEDULER_ADAPTER_FACTORY_DEFAULT, description = SCHEDULER_ADAPTER_FACTORY_DESCRIPTION)
	public String scheduler_adapter_factory();
	
	@Override
	@Meta.AD(required = false, deflt = AGENT_ADAPTER_FACTORY_DEFAULT, description = AGENT_ADAPTER_FACTORY_DESCRIPTION)
	public String agent_adapter_factory();
	
	@Override
	@Meta.AD(required = false, deflt = LOGGING_ADAPTER_FACTORY_DEFAULT, description = LOGGING_ADAPTER_FACTORY_DESCRIPTION)
	public String logging_adapter_factory();
	
	@Override
	@Meta.AD(required = false, deflt = LOG_LISTENER_ID_DEFAULT_STR, description = LOG_LISTENER_ID_DESCRIPTION)
	public String log_listener_id();

	@Override
	@Meta.AD(required = false, deflt = AGENT_BID_LOG_LEVEL_DEFAULT, description = AGENT_BID_LOG_LEVEL_DESCRIPTION,
			optionValues = { NO_LOGGING, PARTIAL_LOGGING, FULL_LOGGING }, optionLabels = {
			NO_LOGGING_LABEL,
			PARTIAL_LOGGING_LABEL,
			FULL_LOGGING_LABEL })
	public String agent_bid_log_level();

	@Override
	@Meta.AD(required = false, deflt = AGENT_PRICE_LOG_LEVEL_DEFAULT, description = AGENT_PRICE_LOG_LEVEL_DESCRIPTION, optionValues = { NO_LOGGING, FULL_LOGGING }, optionLabels = {
			NO_LOGGING_LABEL,
			FULL_LOGGING_LABEL })
	public String agent_price_log_level();

}
