package net.powermatcher.core.agent.concentrator.component;


import net.powermatcher.core.agent.concentrator.config.ConcentratorConfiguration;
import aQute.bnd.annotation.metatype.Meta;
import aQute.bnd.annotation.metatype.Meta.OCD;

/**
 * @author IBM
 * @version 0.9.0
 */
@OCD(name = "PowerMatcher Concentrator")
public interface ConcentratorComponentConfiguration extends ConcentratorConfiguration {

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
	@Meta.AD(required = false, description = MATCHER_ADAPTER_FACTORY_DESCRIPTION)
	public String matcher_adapter_factory();
	
	@Override
	@Meta.AD(required = false, deflt = LOG_LISTENER_ID_DEFAULT_STR, description = LOG_LISTENER_ID_DESCRIPTION)
	public String log_listener_id();

	@Override
	@Meta.AD(required = false, deflt = BID_EXPIRATION_TIME_DEFAULT_STR, description = BID_EXPIRATION_TIME_DESCRIPTION)
	public int bid_expiration_time();

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

	@Override
	@Meta.AD(required = false, deflt = MATCHER_AGENT_BID_LOG_LEVEL_DEFAULT, description = MATCHER_AGENT_BID_LOG_LEVEL_DESCRIPTION, optionValues = { NO_LOGGING, PARTIAL_LOGGING, FULL_LOGGING }, optionLabels = {
			NO_LOGGING_LABEL,
			PARTIAL_LOGGING_LABEL,
			FULL_LOGGING_LABEL })
	public String matcher_agent_bid_log_level();

	@Override
	@Meta.AD(required = false, deflt = MATCHER_AGGREGATED_BID_LOG_LEVEL_DEFAULT, description = MATCHER_AGGREGATED_BID_LOG_LEVEL_DESCRIPTION, optionValues = { NO_LOGGING, PARTIAL_LOGGING, FULL_LOGGING }, optionLabels = {
			NO_LOGGING_LABEL,
			PARTIAL_LOGGING_LABEL,
			FULL_LOGGING_LABEL })
	public String matcher_aggregated_bid_log_level();

	@Override
	@Meta.AD(required = false, deflt = MATCHER_PRICE_LOG_LEVEL_DEFAULT, description = MATCHER_PRICE_LOG_LEVEL_DESCRIPTION, optionValues = { NO_LOGGING, FULL_LOGGING }, optionLabels = {
			NO_LOGGING_LABEL,
			FULL_LOGGING_LABEL })
	public String matcher_price_log_level();

}
