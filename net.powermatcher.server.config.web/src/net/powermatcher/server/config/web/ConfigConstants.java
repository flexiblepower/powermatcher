package net.powermatcher.server.config.web;


public interface ConfigConstants {
	
	/**
	 * Configuration service parameter node id.
	 */
	public static final String CONFIG_PARAMETER_NODE_ID = "nodeid";
	
	/**
	 * Topic name suffix of the configuration update trigger.
	 */
	public static final String CONFIG_UPDATE_TOPIC_SUFFIX = "Update";
	
	/**
	 * MIME type of the Configuration Manager service (servlet) response.
	 */
	public static final String CONFIG_SERVICE_MIME_TYPE = "application/xml"; // ("text/xml"  is deprecated)

}
