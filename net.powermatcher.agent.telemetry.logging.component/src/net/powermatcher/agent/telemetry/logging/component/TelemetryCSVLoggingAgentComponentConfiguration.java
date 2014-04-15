package net.powermatcher.agent.telemetry.logging.component;



import net.powermatcher.agent.telemetry.logging.config.TelemetryCSVLoggingAgentConfiguration;
import aQute.bnd.annotation.metatype.Meta;
import aQute.bnd.annotation.metatype.Meta.OCD;

/**
 * @author IBM
 * @version 0.9.0
 */
@OCD(name = "PowerMatcher Telemetry CSV Logging Agent")
public interface TelemetryCSVLoggingAgentComponentConfiguration extends
		TelemetryCSVLoggingAgentConfiguration {

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
	@Meta.AD(required = false, description = TELEMETRY_LISTENER_ADAPTER_FACTORY_DESCRIPTION)
	public String telemetry_listener_adapter_factory();
	
	@Override
	@Meta.AD(required = false, deflt = MEASUREMENT_LOGGING_PATTERN_DEFAULT, description = LOGGING_PATTERN_DESCRIPTION)
	public String measurement_logging_pattern();

	@Override
	@Meta.AD(required = false, deflt = STATUS_LOGGING_PATTERN_DEFAULT, description = LOGGING_PATTERN_DESCRIPTION)
	public String status_logging_pattern();

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
