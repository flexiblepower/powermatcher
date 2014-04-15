package net.powermatcher.simulation.testagents;

import net.powermatcher.core.agent.framework.config.AgentConfiguration;
import aQute.bnd.annotation.metatype.Meta;
import aQute.bnd.annotation.metatype.Meta.OCD;

@OCD(name = "PowerMatcher VariableDeviceAgent")
public interface VariableDeviceAgentComponentConfiguration extends AgentConfiguration {
	@Override
	@Meta.AD(required = false, deflt = CLUSTER_ID_DEFAULT)
	public String cluster_id();

	@Override
	public String id();

	@Override
	@Meta.AD(required = false, deflt = AgentConfiguration.ENABLED_DEFAULT_STR)
	public boolean enabled();

	@Override
	@Meta.AD(required = false, deflt = UPDATE_INTERVAL_DEFAULT_STR)
	public int update_interval();

	@Override
	@Meta.AD(required = false, deflt = AGENT_BID_LOG_LEVEL_DEFAULT, optionValues = { NO_LOGGING, PARTIAL_LOGGING,
			FULL_LOGGING }, optionLabels = { NO_LOGGING_LABEL, PARTIAL_LOGGING_LABEL, FULL_LOGGING_LABEL })
	public String agent_bid_log_level();

	@Override
	@Meta.AD(required = false, deflt = AGENT_PRICE_LOG_LEVEL_DEFAULT, optionValues = { NO_LOGGING, FULL_LOGGING }, optionLabels = {
			NO_LOGGING_LABEL, FULL_LOGGING_LABEL })
	public String agent_price_log_level();
}
