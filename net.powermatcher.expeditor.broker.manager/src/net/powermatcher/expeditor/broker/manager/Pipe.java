package net.powermatcher.expeditor.broker.manager;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.powermatcher.core.configurable.BaseConfiguration;
import net.powermatcher.core.configurable.ConfigurableObject;
import net.powermatcher.core.configurable.service.ConfigurationService;
import net.powermatcher.core.object.config.ConnectableObjectConfiguration;
import net.powermatcher.expeditor.broker.manager.config.BridgeManagerConfiguration;

import com.ibm.micro.admin.AdminException;
import com.ibm.micro.admin.bridge.Bridge;
import com.ibm.micro.admin.bridge.FlowDefinition;
import com.ibm.micro.admin.bridge.MQTTConnectionDefinition;
import com.ibm.micro.admin.bridge.NotificationDefinition;
import com.ibm.micro.admin.bridge.PipeDefinition;
import com.ibm.micro.admin.bridge.TopicDefinition;

/**
 * @author IBM
 * @version 0.9.0
 */
public class Pipe extends ConfigurableObject {
	/**
	 * Define the logger (Logger) field.
	 */
	protected final static Logger logger = LoggerFactory.getLogger(Pipe.class);
	/**
	 * Define the bridge (Bridge) field.
	 */
	private Bridge bridge;
	/**
	 * Define the pd (PipeDefinition) field.
	 */
	private PipeDefinition pd;

	/**
	 * Gets the cluster ID (String) value.
	 * 
	 * @return The Cluster ID (<code>String</code>) value.
	 */
	public String getClusterId() {
		return getProperty(ConnectableObjectConfiguration.CLUSTER_ID_PROPERTY, ConnectableObjectConfiguration.CLUSTER_ID_DEFAULT);
	}

	/**
	 * Gets the ID (String) value.
	 * 
	 * @return The id (<code>String</code>) value.
	 * @see #getClusterId()
	 */
	public String getId() {
		return getStringProperty(ConnectableObjectConfiguration.ID_PROPERTY);
	}

	/**
	 * Gets the in topics (String[]) value.
	 * 
	 * @return The in topics (<code>String[]</code>) value.
	 */
	public String[] getInTopics() {
		return getProperty(BridgeManagerConfiguration.IN_TOPICS_PROPERTY, BridgeManagerConfiguration.IN_TOPICS_DEFAULT);
	}

	/**
	 * Gets the in target topic (String) value.
	 * 
	 * @return The in target topics (<code>String</code>) value.
	 */
	public String getInTargetTopic() {
		return getProperty(BridgeManagerConfiguration.IN_TARGET_TOPIC_PROPERTY, (String)null);
	}

	/**
	 * Gets the keep alive secs (short) value.
	 * 
	 * @return The keep alive secs (<code>short</code>) value.
	 */
	private short getKeepAliveSecs() {
		return getProperty(BridgeManagerConfiguration.KEEP_ALIVE_SECS_PROPERTY,
				BridgeManagerConfiguration.KEEP_ALIVE_SECS_DEFAULT);
	}

	/**
	 * Gets the name (String) value.
	 * 
	 * @return The name (<code>String</code>) value.
	 */
	public String getName() {
		String defaultName = getId() + '.' + getClusterId();
		String name = getProperty(BridgeManagerConfiguration.PIPE_NAME_PROPERTY, defaultName);
		name = name.replace('-', '_');
		if (name.length() > 23) {
			name = name.substring(0, 23);
		}
		return name;
	}

	/**
	 * Gets the notification clean disconnected message (String) value.
	 * 
	 * @return The notification clean disconnected message (<code>String</code>)
	 *         value.
	 */
	private String getNotificationCleanDisconnectedMessage() {
		return getProperty(BridgeManagerConfiguration.NOTIFICATION_CLEANDISCONNECTED_MESSAGE_PROPERTY,
				BridgeManagerConfiguration.NOTIFICATION_CLEANDISCONNECTED_MESSAGE_DEFAULT);
	}

	/**
	 * Gets the notification connected message (String) value.
	 * 
	 * @return The notification connected message (<code>String</code>) value.
	 */
	private String getNotificationConnectedMessage() {
		return getProperty(BridgeManagerConfiguration.NOTIFICATION_CONNECTED_MESSAGE_PROPERTY,
				BridgeManagerConfiguration.NOTIFICATION_CONNECTED_MESSAGE_DEFAULT);
	}

	/**
	 * Gets the notification disconnected message (String) value.
	 * 
	 * @return The notification disconnected message (<code>String</code>)
	 *         value.
	 */
	private String getNotificationDisconnectedMessage() {
		return getProperty(BridgeManagerConfiguration.NOTIFICATION_DISCONNECTED_MESSAGE_PROPERTY,
				BridgeManagerConfiguration.NOTIFICATION_DISCONNECTED_MESSAGE_DEFAULT);
	}

	/**
	 * Gets the notification topic (String) value.
	 * 
	 * @return The notification topic (<code>String</code>) value.
	 */
	private String getNotificationTopic() {
		String prefix = getProperty(BridgeManagerConfiguration.NOTIFICATION_TOPIC_PREFIX_PROPERTY,
				BridgeManagerConfiguration.NOTIFICATION_TOPIC_PREFIX_DEFAULT);
		return prefix + '/' + getClusterId() + '/' + getId();
	}

	/**
	 * Gets the out topics (String[]) value.
	 * 
	 * @return The out topics (<code>String[]</code>) value.
	 */
	public String[] getOutTopics() {
		return getProperty(BridgeManagerConfiguration.OUT_TOPICS_PROPERTY, BridgeManagerConfiguration.OUT_TOPICS_DEFAULT);
	}

	/**
	 * Gets the out target topic (String) value.
	 * 
	 * @return The out target topic (<code>String</code>) value.
	 */
	public String getOutTargetTopic() {
		return getProperty(BridgeManagerConfiguration.OUT_TARGET_TOPIC_PROPERTY, (String)null);
	}

	/**
	 * Gets the password (String) value.
	 * 
	 * @return The password (<code>String</code>) value.
	 */
	private String getPassword() {
		return getProperty(BridgeManagerConfiguration.PASSWORD_PROPERTY, (String) null);
	}

	/**
	 * Gets the remote host (String) value.
	 * 
	 * @return The remote host (<code>String</code>) value.
	 */
	public String getRemoteHost() {
		return getStringProperty(BridgeManagerConfiguration.HOST_PROPERTY);
	}

	/**
	 * Gets the remote port (int) value.
	 * 
	 * @return The remote port (<code>int</code>) value.
	 */
	private int getRemotePort() {
		return getProperty(BridgeManagerConfiguration.PORT_PROPERTY, BridgeManagerConfiguration.PORT_DEFAULT);
	}

	/**
	 * Gets the secure (boolean) value.
	 * 
	 * @return The secure (<code>boolean</code>) value.
	 */
	private boolean getSecure() {
		return getProperty(BridgeManagerConfiguration.SECURE_PROPERTY, BridgeManagerConfiguration.SECURE_DEFAULT);
	}

	/**
	 * Gets the user name (String) value.
	 * 
	 * @return The user name (<code>String</code>) value.
	 */
	private String getUserName() {
		return getProperty(BridgeManagerConfiguration.USERNAME_PROPERTY, (String) null);
	}

	/**
	 * Gets the notification enabled (boolean) value.
	 * 
	 * @return The notification enabled (<code>boolean</code>) value.
	 */
	private boolean isNotificationEnabled() {
		return getProperty(BridgeManagerConfiguration.NOTIFICATION_ENABLED_PROPERTY,
				BridgeManagerConfiguration.NOTIFICATION_ENABLED_DEFAULT);
	}

	/**
	 * Sets the bridge value.
	 * 
	 * @param bridge
	 *            The bridge (<code>Bridge</code>) parameter.
	 */
	public void setBridge(final Bridge bridge) {
		this.bridge = bridge;
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
		String hostname;
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (final UnknownHostException e) {
			hostname = "unknown";
		}
		Map<String, Object> defaultProperties = new HashMap<String, Object>();
		defaultProperties.put(BridgeManagerConfiguration.PIPE_NAME_PROPERTY,
				configuration.getStringProperty(BridgeManagerConfiguration.ID_PROPERTY));
		defaultProperties.put(BridgeManagerConfiguration.NOTIFICATION_COMPONENT_NAME_PROPERTY,
				configuration.getProperty(BridgeManagerConfiguration.NOTIFICATION_COMPONENT_NAME_PROPERTY, hostname));
		defaultProperties.put(BridgeManagerConfiguration.NOTIFICATION_HOST_NAME_PROPERTY,
				configuration.getProperty(BridgeManagerConfiguration.NOTIFICATION_HOST_NAME_PROPERTY, hostname));
		ConfigurationService augmentedConfiguration = new BaseConfiguration(configuration, defaultProperties);
		super.setConfiguration(augmentedConfiguration);
	}

	/**
	 * Start pipe.
	 * 
	 * @throws AdminException
	 *             Admin Exception.
	 * @see #stopPipe()
	 */
	protected void startPipe() throws AdminException {
		String name = getName();
		try {
			this.pd = this.bridge.createPipeDefinition(name);
			String host = getRemoteHost();
			MQTTConnectionDefinition cd = this.bridge.createMQTTConnectionDefinition(host);
			cd.setHost(host);
			cd.setPort(getRemotePort());
			String userName = getUserName();
			if (userName != null) {
				cd.setUserName(userName);
			}
			String password = getPassword();
			if (password != null) {
				cd.setPassword(password);
			}
			cd.setSecure(getSecure());
			cd.setKeepAliveSecs(getKeepAliveSecs());
			this.pd.setConnection(cd);
			FlowDefinition flowOut = this.bridge.createFlowDefinition(name + "-out");
			String outTopics[] = getOutTopics();
			TopicDefinition[] outTopicDefs = new TopicDefinition[outTopics.length];
			for (int i = 0; i < outTopicDefs.length; i++) {
				outTopicDefs[i] = this.bridge.createTopicDefinition(outTopics[i]);
			}
			flowOut.setSources(outTopicDefs);
			if (getOutTargetTopic() != null) {
				flowOut.setTarget(this.bridge.createTopicDefinition(getOutTargetTopic()));
			}
			flowOut.setQos(0);
			FlowDefinition flowIn = this.bridge.createFlowDefinition(name + "-in");
			String inTopics[] = getInTopics();
			TopicDefinition[] inTopicDefs = new TopicDefinition[inTopics.length];
			for (int i = 0; i < inTopicDefs.length; i++) {
				inTopicDefs[i] = this.bridge.createTopicDefinition(inTopics[i]);
			}
			flowIn.setSources(inTopicDefs);
			if (getInTargetTopic() != null) {
				flowOut.setTarget(this.bridge.createTopicDefinition(getInTargetTopic()));
			}
			flowIn.setQos(0);
			this.pd.addOutboundFlow(flowOut);
			this.pd.addInboundFlow(flowIn);
			if (isNotificationEnabled()) {
				NotificationDefinition lwatNotification = this.bridge.createNotificationDefinition("lwat");
				lwatNotification.setTopic(getNotificationTopic());
				lwatNotification.setConnectedMessage(getNotificationConnectedMessage());
				lwatNotification.setDisconnectedMessage(getNotificationDisconnectedMessage());
				lwatNotification.setCleanDisconnectedMessage(getNotificationCleanDisconnectedMessage());
				this.pd.setLocalNotification(lwatNotification);
				this.pd.setRemoteNotification(lwatNotification);
			}
			this.bridge.addPipe(this.pd);
			this.bridge.startAllPipes();
		} catch (final AdminException e) {
			logger.error("Failed to create pipe " + name, e);
			throw e;
		}
	}

	/**
	 * Stop pipe.
	 * 
	 * @see #startPipe()
	 */
	protected void stopPipe() {
		String name = getName();
		try {
			this.bridge.deletePipe(name, true);
		} catch (final AdminException e) {
			logger.warn("Error to deleting pipe " + name + " :" + e.getMessage());
		}
	}

}
