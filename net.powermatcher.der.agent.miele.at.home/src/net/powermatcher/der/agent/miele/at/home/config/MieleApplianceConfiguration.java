package net.powermatcher.der.agent.miele.at.home.config;


import net.powermatcher.core.agent.framework.config.AgentConfiguration;
import net.powermatcher.telemetry.config.TelemetryConfiguration;

/**
 * @author IBM
 * @version 0.9.0
 */
public interface MieleApplianceConfiguration extends AgentConfiguration, TelemetryConfiguration {

	/**
	 * 
	 */
	public static final String MIELE_APPLIANCE_ID = "appliance.id";
	/**
	 * 
	 */
	public static final String MIELE_APPLIANCE_TYPE = "appliance.type";
	/**
	 * 
	 */
	public static final String MIELE_AGENT_LANGUAGE_CODE = "language.code";
	/**
	 * 
	 */
	public static final String MIELE_APPLIANCE_POWER_CONSUMPTION = "appliance.power";

	/** Configuration property: Miele gateway protocol property */
	public static final String MIELE_GATEWAY_PROTOCOL_PROPERTY = "gateway.protocol";

	/** Configuration property: Miele gateway hostname property */
	public static final String MIELE_GATEWAY_HOSTNAME_PROPERTY = "gateway.hostname";

	/** Configuration property: Miele gateway port property */
	public static final String MIELE_GATEWAY_PORT_PROPERTY = "gateway.port";

	// Default values
	/**
	 * 
	 */
	public static final String MIELE_AGENT_LANGUAGE_CODE_DEFAULT = "de_DE";

	/** Default Miele appliance power consumption when not configured */
	public static final int MIELE_APPLIANCE_POWER_CONSUMPTION_DEFAULT = 200;
	/**
	 * 
	 */
	public static final String MIELE_APPLIANCE_POWER_CONSUMPTION_DEFAULT_STR = "200";

	/** Miele gateway protocol default value */
	public static final String MIELE_GATEWAY_PROTOCOL_DEFAULT = "http";

	/** Miele gateway hostname default value */
	public static final String MIELE_GATEWAY_HOSTNAME_DEFAULT = "localhost";

	/** Miele gateway port default value */
	public static final String MIELE_GATEWAY_PORT_DEFAULT = "8080";

	/**
	 * @return TODO
	 */
	public String appliance_id();

	/**
	 * @return TODO
	 */
	public double appliance_power();

	/**
	 * @return TODO
	 */
	public String appliance_type();

	/**
	 * @return TODO
	 */
	public String hostname();

	/**
	 * @return TODO
	 */
	public String language_code();

	/**
	 * @return TODO
	 */
	public String port();

	/**
	 * @return TODO
	 */
	public String protocol();

}
