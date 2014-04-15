package net.powermatcher.der.agent.miele.at.home.component;


import net.powermatcher.der.agent.miele.at.home.config.MieleFridgeFreezerDeviceAgentConfiguration;
import aQute.bnd.annotation.metatype.Meta;
import aQute.bnd.annotation.metatype.Meta.OCD;

/**
 * @author IBM
 * @version 0.9.0
 */
@OCD(name = "PowerMatcher Miele Fridge Freezer Device Agent")
public interface MieleFridgeFreezerDeviceAgentComponentConfiguration extends
	MieleFridgeFreezerDeviceAgentConfiguration {

	/**
	 * 
	 */
	public static final String EQUIPMENT_TYPE_DEFAULT = "Refrigerator";

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
	@Meta.AD(required = true, description = "Appliance type as known by the Miele gateway")
	public String appliance_type();

	@Override
	@Meta.AD(required = true, description = "Appliance ID as known by the Miele gateway")
	public String appliance_id();

	@Override
	@Meta.AD(required = false, deflt = MIELE_APPLIANCE_POWER_CONSUMPTION_DEFAULT_STR)
	public double appliance_power();

	@Override
	@Meta.AD(required = false, deflt = MIELE_FREEZER_TARGET_TEMPERATURE_DEFAULT_STR)
	public float freezer_temperature();

	@Override
	@Meta.AD(required = false, deflt = MIELE_MIN_DURATION_SUPERFROST_DEFAULT_STR)
	public int min_duration_supercool();

	@Override
	@Meta.AD(required = false, deflt = MIELE_FRIDGE_TARGET_TEMPERATURE_DEFAULT_STR)
	public float fridge_temperature();

	@Override
	@Meta.AD(required = false, deflt = MIELE_MIN_DURATION_SUPERCOOL_DEFAULT_STR)
	public int min_duration_superfrost();

	@Override
	@Meta.AD(required = false, deflt = MIELE_GATEWAY_PROTOCOL_DEFAULT)
	public String protocol();

	@Override
	@Meta.AD(required = false, deflt = MIELE_GATEWAY_HOSTNAME_DEFAULT)
	public String hostname();

	@Override
	@Meta.AD(required = false, deflt = MIELE_GATEWAY_PORT_DEFAULT)
	public String port();

	@Override
	@Meta.AD(required = false, deflt = MIELE_AGENT_LANGUAGE_CODE_DEFAULT)
	public String language_code();

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
