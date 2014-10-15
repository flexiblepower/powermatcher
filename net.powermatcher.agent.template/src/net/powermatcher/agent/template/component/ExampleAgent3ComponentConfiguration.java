package net.powermatcher.agent.template.component;



import net.powermatcher.agent.template.ExampleAgent3;
import net.powermatcher.agent.template.config.ExampleAgent3Configuration;
import aQute.bnd.annotation.metatype.Meta;
import aQute.bnd.annotation.metatype.Meta.OCD;

/**
 * This interface defines the OSGi metatype Object Class Definition that specifies the
 * configuration properties for <code>ExampleAgent3</code>. The interface defines a
 * method for each configuration property that must be included in the generated metatype 
 * information. The name of the method must be the same as the configuration property, where
 * a '.' in the property name is replaced by a '_' to conform to Java naming constraints.
 * 
 * @see ExampleAgent3
 * @see ExampleAgent3Component
 * 
 * @author IBM
 * @version 0.9.0
 */
@OCD(name = "PowerMatcher Example Agent 3")
public interface ExampleAgent3ComponentConfiguration extends ExampleAgent3Configuration {

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
	@Meta.AD(required = false, deflt = BID_PRICE_DEFAULT_STR, description = BID_PRICE_DESCRIPTION)
	public double bid_price();

	@Override
	@Meta.AD(required = false, deflt = BID_POWER_DEFAULT_STR, description = BID_POWER_DESCRIPTION)
	public double bid_power();

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
	@Meta.AD(required = false, deflt = TELEMETRY_ADAPTER_FACTORY_DEFAULT, description = TELEMETRY_ADAPTER_FACTORY_DESCRIPTION)
	public String telemetry_adapter_factory();
	
	@Override
	@Meta.AD(required = false, deflt = LOG_LISTENER_ID_DEFAULT_STR, description = LOG_LISTENER_ID_DESCRIPTION)
	public String log_listener_id();

	@Override
	@Meta.AD(required = false, deflt = TELEMETRY_LISTENER_ID_DEFAULT_STR, description = TELEMETRY_LISTENER_ID_DESCRIPTION)
	public String telemetry_listener_id();

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
