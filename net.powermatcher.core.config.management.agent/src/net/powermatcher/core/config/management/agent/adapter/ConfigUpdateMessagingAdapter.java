package net.powermatcher.core.config.management.agent.adapter;


import net.powermatcher.core.config.management.agent.ConfigManager;
import net.powermatcher.core.config.management.agent.ConfigManagerConfiguration;
import net.powermatcher.core.configurable.service.ConfigurationService;
import net.powermatcher.core.messaging.framework.MessagingAdapter;
import net.powermatcher.core.messaging.framework.Topic;


/**
 * @author IBM
 * @version 0.9.0
 */
public class ConfigUpdateMessagingAdapter extends MessagingAdapter {
	/**
	 * Define the topic prefix (Topic) constant.
	 */
	private static final Topic TOPIC_PREFIX = Topic.create("Configuration/Update");
	/**
	 * Define the request topic (Topic) constant.
	 */
	private static Topic REQUEST_TOPIC;
	/**
	 * The configuration manager.
	 */
	private ConfigManager configManager;

	/**
	 * Constructs an instance of this class.
	 * 
	 * @see #ConfigUpdateMessagingAdapter(ConfigurationService)
	 */
	public ConfigUpdateMessagingAdapter() {
		super();
	}

	/**
	 * Constructs an instance of this class from the specified properties
	 * parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @see #ConfigUpdateMessagingAdapter()
	 */
	public ConfigUpdateMessagingAdapter(final ConfigurationService configuration) {
		super(configuration);
	}

	/**
	 * Gets the subscriptions (Topic[]) value.
	 * 
	 * @return The subscriptions (<code>Topic[]</code>) value.
	 */
	@Override
	public Topic[] getSubscriptions() {
		logInfo("Config update messaging adapter subscribed to " + REQUEST_TOPIC);
		return new Topic[] { REQUEST_TOPIC };
	}

	/**
	 * Handle message arrived with the specified topic and data parameters.
	 * 
	 * @param topic
	 *            The topic (<code>Topic</code>) parameter.
	 * @param data
	 *            The data (<code>String</code>) parameter.
	 * @return True if the message topic matched an expected pattern and has
	 *         been handled.
	 */
	@Override
	public boolean handleMessageArrived(final Topic topic, final String data) {
		if (topic.matches(REQUEST_TOPIC)) {
			handleRequest(topic, data);
		} else {
			return super.handleMessageArrived(topic, data);
		}
		return true;
	}

	/**
	 * Handle request with the specified topic and data parameters.
	 * 
	 * @param topic
	 *            The topic (<code>Topic</code>) parameter.
	 * @param data
	 *            The data (<code>String</code>) parameter.
	 */
	private void handleRequest(final Topic topic, final String data) {
		if (isDebugEnabled()) {
			logDebug("handleRequest topic=" + topic + " data=" + data);
		}
		logInfo("On-demand update received.");
		this.configManager.doUpdate();
	}

	/**
	 * Initialize.
	 */
	protected void initialize() {
		logInfo("Initializing config update messaging adapter.");
		REQUEST_TOPIC = TOPIC_PREFIX
		.addLevel(
				getProperty(
						ConfigManagerConfiguration.CONFIGURATION_NODE_ID_PROPERTY,
						ConfigManagerConfiguration.CONFIGURATION_NODE_ID_DEFAULT));
	}

	/**
	 * Sets the configuration manager value.
	 * 
	 * @param configManager
	 *            The configuration manager (<code>ConfigManager</code>)
	 *            parameter.
	 */
	public void setConfigManager(final ConfigManager configManager) {
		this.configManager = configManager;
	}

	/**
	 * Sets the configuration value.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 */
	@Override
	public void setConfiguration(final ConfigurationService configuration) {
		super.setConfiguration(configuration);
		initialize();
	}

}
