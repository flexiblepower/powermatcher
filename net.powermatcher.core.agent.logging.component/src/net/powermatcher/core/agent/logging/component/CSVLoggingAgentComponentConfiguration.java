package net.powermatcher.core.agent.logging.component;


import net.powermatcher.core.agent.logging.config.CSVLoggingAgentConfiguration;
import aQute.bnd.annotation.metatype.Meta;
import aQute.bnd.annotation.metatype.Meta.OCD;

/**
 * @author IBM
 * @version 0.9.0
 */
@OCD(name = "PowerMatcher CSV Logging Agent")
public interface CSVLoggingAgentComponentConfiguration extends
		CSVLoggingAgentConfiguration {

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
	@Meta.AD(required = false, deflt = TIME_ADAPTER_FACTORY_DEFAULT, description = TIME_ADAPTER_FACTORY_DESCRIPTION)
	public String time_adapter_factory();
	
	@Override
	@Meta.AD(required = false, deflt = SCHEDULER_ADAPTER_FACTORY_DEFAULT, description = SCHEDULER_ADAPTER_FACTORY_DESCRIPTION)
	public String scheduler_adapter_factory();
	
	@Override
	@Meta.AD(required = false, description = LOG_LISTENER_ADAPTER_FACTORY_DESCRIPTION)
	public String log_listener_adapter_factory();
	
	@Override
	@Meta.AD(required = false, deflt = POWERMATCHER_BID_LOGGING_PATTERN_DEFAULT, description = LOGGING_PATTERN_DESCRIPTION)
	public String powermatcher_bid_logging_pattern();

	@Override
	@Meta.AD(required = false, deflt = POWERMATCHER_PRICE_LOGGING_PATTERN_DEFAULT, description = LOGGING_PATTERN_DESCRIPTION)
	public String powermatcher_price_logging_pattern();

	@Override
	@Meta.AD(required = false, deflt = DATE_FORMAT_DEFAULT, description = DATE_FORMAT_DESCRIPTION)
	public String date_format();

	@Override
	@Meta.AD(required = false, deflt = LIST_SEPARATOR_DEFAULT, description = LIST_SEPARATOR_DESCRIPTION)
	public String list_separator();

	@Override
	@Meta.AD(required = false, deflt = UPDATE_INTERVAL_DEFAULT_STR, description = UPDATE_INTERVAL_DESCRIPTION)
	public int update_interval();

}
