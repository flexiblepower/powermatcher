package net.powermatcher.core.messaging.mqttv3;


import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.powermatcher.core.adapter.Adapter;
import net.powermatcher.core.configurable.service.Configurable;
import net.powermatcher.core.messaging.framework.Topic;
import net.powermatcher.core.messaging.service.MessagingConnectionService;
import net.powermatcher.core.messaging.service.MessagingConnectorService;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.MqttTopic;


/**
 * @author IBM
 * @version 0.9.0
 */
public class Mqttv3Connection extends Adapter implements MessagingConnectionService {
	/**
	 * @author IBM
	 * @version 0.9.0
	 */
	private class CallbackHandler implements MqttCallback {
		/**
		 * Connection lost with the specified cause parameter.
		 * 
		 * @param cause
		 *            The cause (<code>Throwable</code>) parameter.
		 */
		@Override
		public void connectionLost(final Throwable cause) {
			logWarning("MQTT client connection lost: " + cause);
			asyncConnectMqttClient(getConnectionDefinition().getReconnectInterval());
		}

		/**
		 * Delivery complete with the specified token parameter.
		 * 
		 * @param token
		 *            The token (<code>MqttDeliveryToken</code>) parameter.
		 */
		@Override
		public void deliveryComplete(final MqttDeliveryToken token) {
			/* do nothing */
		}

		/**
		 * Message arrived with the specified destination and message
		 * parameters.
		 * 
		 * @param destination
		 *            The destination (<code>MqttTopic</code>) parameter.
		 * @param message
		 *            The message (<code>MqttMessage</code>) parameter.
		 * @throws Exception
		 *             Exception.
		 */
		@Override
		public void messageArrived(final MqttTopic destination, final MqttMessage message) throws Exception {
			Topic topic = Topic.create(destination.getName());
			try {
				handleMessageArrived(topic, message.getPayload());
			} catch (final Exception e) {
				logError("Error handling message", e);
			}
		}

	}

	/**
	 * @author IBM
	 * @version 0.9.0
	 */
	private class ConnectTask implements Runnable {
		/**
		 * Attempt to connect to the MQ client.
		 * For some reason each MQTT client must be connected on its own, virgin, thread. If this is not done,
		 * an MqttException.REASON_CODE_CLIENT_TIMEOUT is thrown for the subscribe immediately.
		 * As the run method of ConnectTask is called on one of the threads of the scheduler's thread pool,
		 * a temporary thread must be forked for the execution.
		 * This thread terminates immediately after the call to connectMqttClient.
		 */
		@Override
		public void run() {
			Runnable async = new Runnable() {

				@Override
				public void run() {
					try {
						connectMqttClient();
					} catch (final MqttException me) {
						logDebug("Retrying MQTT client connection: ", me);
					} catch (final Exception e) {
						logError("Error connecting MQTT client: ", e);
					}
				}
				
			};
			new Thread(async).start();
		}

	}

	/**
	 * Define the UTF8 (Charset) field.
	 */
	private static final Charset UTF8 = Charset.forName("UTF8");

	/**
	 * Define the MQTTv3 connection definition (Mqttv3ConnectionDefinition)
	 * field.
	 */
	private Mqttv3ConnectionDefinition connectionDefinition;
	/**
	 * Define the update task (ConnectTask) field.
	 */
	private ConnectTask connectTask;
	/**
	 * Define the connectors (Set) field.
	 */
	private Set<MessagingConnectorService> connectors = new HashSet<MessagingConnectorService>();
	/**
	 * Define the client (MqttClient) field.
	 */
	private MqttClient client;
	/**
	 * Define the connect task future (ScheduledFuture) field.
	 */
	private ScheduledFuture<?> connectTaskFuture;
	/**
	 * Define the bound (boolean) field.
	 */
	private boolean bound;
	/**
	 * Define the started (boolean) field.
	 */
	private boolean started;

	/**
	 * Constructs an instance of this class.
	 */
	public Mqttv3Connection() {
		super();
	}

	/**
	 * Constructs an instance of this class from the specified connection definition
	 * parameter.
	 * 
	 * @param connectionDefinition
	 *            The connectionDefinition (<code>Mqttv3ConnectionDefinition</code>)
	 *            parameter.
	 */
	public Mqttv3Connection(final Mqttv3ConnectionDefinition connectionDefinition) {
		super();
		setConnectionDefinition(connectionDefinition);
	}

	/**
	 * Constructs an instance of this class from the specified properties
	 * parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 */
	public Mqttv3Connection(final Configurable configuration) {
		super(configuration);
	}

	/**
	 * Bind a client to a (shared) messaging connection.
	 * 
	 * @param connector
	 *            The connector (<code>MessagingConnectorService</code>)
	 *            parameter.
	 */
	@Override
	public synchronized void addConnector(final MessagingConnectorService connector) {
		this.connectors.add(connector);
		if (isStarted()) {
			try {
				subscribeMqttClient(this.client, connector.getSubscriptions());
				connector.binding(this);
			} catch (final Exception e) {
				logError("MQTT connection failed to bind to " + connector.getConnectorId(), e);
			}
		} else {
			asyncConnectMqttClient(0);
		}
	}

	/**
	 * Async connect mqtt client.
	 * 
	 * @param initialDelay
	 *            The initial delay (<code>int</code>) parameter.
	 */
	private void asyncConnectMqttClient(final int initialDelay) {
		ScheduledExecutorService scheduler = getScheduler();
		if (this.connectTask == null && this.client != null && scheduler != null) {
			logInfo("Attempting to (re)connect MQTT client to " + getConnectionDefinition().getBrokerURI());
			this.connectTask = new Mqttv3Connection.ConnectTask();
			this.connectTaskFuture = scheduler.scheduleWithFixedDelay(this.connectTask, 
					initialDelay, getConnectionDefinition().getReconnectInterval(), TimeUnit.SECONDS);
		}
	}

	/**
	 * Create the MQTT client and start the task to connect asynchronously.
	 * The client is created only if not already created when the scheduler was bound.
	 * The asynchronous connect is started if one or more connectors have been registered.
	 * @see #startPeriodicTasks()
	 */
	@Override
	public synchronized void bind() {
		if (!this.bound) {
			this.bound = true;
			startPeriodicTasks();
		}
	}

	/**
	 * Binding.
	 * 
	 * @throws Exception
	 */
	protected void binding() throws Exception {
		MqttClient client = this.client;
		if (client != null) {
			this.started = true;
			cancelConnectTask();
			for (MessagingConnectorService connector : this.connectors) {
				try {
					subscribeMqttClient(client, connector.getSubscriptions());
					connector.binding(this);
				} catch (Exception e) {
					logError("MQTT connection failed to bind to " + connector.getConnectorId(), e);
				}
			}
		}
	}

	/**
	 * Cancel the running connect task, if it is running.
	 */
	private synchronized void cancelConnectTask() {
		if (this.connectTask != null) {
			this.connectTaskFuture.cancel(true);
			this.connectTask = null;
			this.connectTaskFuture = null;
		}
	}

	/**
	 * Connect MQTT client.
	 * 
	 * @throws Exception
	 *             Exception.
	 */
	private synchronized void connectMqttClient() throws Exception {
		if (this.client != null && !this.client.isConnected()) {
			MqttConnectOptions options = new MqttConnectOptions();
			options.setUserName(getConnectionDefinition().getUserName());
			options.setPassword(getConnectionDefinition().getPassword());
			MqttTopic notificationTopic = this.client.getTopic(getConnectionDefinition().getNotificationTopic().toString());
			if (getConnectionDefinition().isNotificationEnabled()) {
				options.setWill(notificationTopic, getConnectionDefinition().getNotificationDisconnectedMessage().getBytes(),
						0, false);
			}
			this.client.connect(options);
			logInfo("MQTT client connected");
			if (getConnectionDefinition().isNotificationEnabled()) {
				MqttMessage message = new MqttMessage(getConnectionDefinition().getNotificationConnectedMessage().getBytes());
				notificationTopic.publish(message);
			}
			binding();
		}
	}

	/**
	 * Creates the unconnected MQTT client with the callback handler.
	 * The MQTT client is created only if it has not been created yet.
	 * 
	 * @throws MqttException
	 */
	private void createMqttClient() throws MqttException {
		if (this.client == null) {
			String mqttClientName = getConnectionDefinition().getClientId();
			this.client = new MqttClient(getConnectionDefinition().getBrokerURI(), mqttClientName);
			this.client.setCallback(new Mqttv3Connection.CallbackHandler());
		}
	}

	/**
	 * Delete MQTT client.
	 */
	private void deleteMqttClient() {
		if (this.client != null) {
			try {
				this.client.setCallback(null);
			} catch (final MqttException e) {
				logWarning("Error deleting MQTT client", e);
			}
			this.client = null;
		}
	}

	/**
	 * Disconnect the MQTT client, if connected
	 */
	private synchronized void disconnectMqttClient() {
		unbinding();
		try {
			cancelConnectTask();
			if (this.client != null) {
				if (this.client.isConnected()) {
					logInfo("Disconnecting MQTT client");
					if (getConnectionDefinition().isNotificationEnabled()) {
						MqttTopic notificationTopic = this.client.getTopic(getConnectionDefinition().getNotificationTopic()
								.toString());
						MqttMessage message = new MqttMessage(getConnectionDefinition()
								.getNotificationCleanDisconnectedMessage().getBytes());
						MqttDeliveryToken token = notificationTopic.publish(message);
						token.waitForCompletion(getConnectionDefinition().getReconnectInterval() * 500l);
					}
					this.client.disconnect();
				}
			}
		} catch (final MqttException e) {
			logWarning("Error disconnecting MQTT client", e);
		}
	}

	/**
	 * Get the connection definition from which this connection was created.
	 * 
	 * @return the connection definition for this connection.
	 */
	@Override
	public Mqttv3ConnectionDefinition getConnectionDefinition() {
		if (this.connectionDefinition == null) {
			this.connectionDefinition = new Mqttv3ConnectionDefinition(getConfiguration());
		}
		return this.connectionDefinition;
	}

	/**
	 * Handle message arrived with the specified topic and data parameters.
	 * 
	 * @param topic
	 *            The topic (<code>Topic</code>) parameter.
	 * @param data
	 *            The data (<code>byte[]</code>) parameter.
	 */
	protected synchronized void handleMessageArrived(final Topic topic, final byte[] data) {
		boolean handled = false;
		for (MessagingConnectorService connector : this.connectors) {
			if (connector.handleMessageArrived(topic, data)) {
				handled = true;
			}
		}
		if (!handled) {
			logUnhandledMessage(topic, data);
		}
	}

	/**
	 * Gets the bound (boolean) value.
	 * 
	 * @return The bound (<code>boolean</code>) value.
	 */
	@Override
	public synchronized boolean isBound() {
		return !this.connectors.isEmpty();
	}

	/**
	 * Gets the started (boolean) value.
	 * 
	 * @return The started (<code>boolean</code>) value.
	 */
	protected boolean isStarted() {
		return this.started;
	}

	/**
	 * Log unhandled message with the specified topic and data parameters.
	 * 
	 * @param topic
	 *            The topic (<code>Topic</code>) parameter.
	 * @param data
	 *            The data (<code>Object</code>) parameter.
	 */
	protected void logUnhandledMessage(final Topic topic, final Object data) {
		final String data_string = String.valueOf(data);
		final StringBuffer buffer = new StringBuffer();
		buffer.append("Unhandled message arrived: ");
		buffer.append(topic);
		buffer.append(" = ");
		buffer.append(data_string);
		logError(buffer.toString());
	}

	/**
	 * Publish with the specified topic parameter.
	 * 
	 * @param topic
	 *            The topic (<code>Topic</code>) parameter.
	 */
	@Override
	public void publish(final Topic topic) {
		MqttTopic mqttTopic = this.client.getTopic(topic.toString());
		MqttMessage message = new MqttMessage();
		try {
			mqttTopic.publish(message);
		} catch (final MqttException e) {
			logWarning("Error publishing MQTT message: ", e);
		}
	}

	/**
	 * Publish with the specified topic and data parameters.
	 * 
	 * @param topic
	 *            The topic (<code>Topic</code>) parameter.
	 * @param data
	 *            The data (<code>byte[]</code>) parameter.
	 */
	@Override
	public void publish(final Topic topic, final byte[] data) {
		MqttTopic mqttTopic = this.client.getTopic(topic.toString());
		MqttMessage message = new MqttMessage(data);
		try {
			mqttTopic.publish(message);
		} catch (final MqttException e) {
			logWarning("Error publishing MQTT message: ", e);
		}
	}

	/**
	 * Publish with the specified topic and data parameters.
	 * 
	 * @param topic
	 *            The topic (<code>Topic</code>) parameter.
	 * @param data
	 *            The data (<code>String</code>) parameter.
	 */
	@Override
	public void publish(final Topic topic, final String data) {
		MqttTopic mqttTopic = this.client.getTopic(topic.toString());
		MqttMessage message = new MqttMessage(data.getBytes(UTF8));
		try {
			mqttTopic.publish(message);
		} catch (final MqttException e) {
			logWarning("Error publishing MQTT message: ", e);
		}
	}

	/**
	 * Unbind a client from a (shared) messaging connection.
	 * 
	 * @param connector
	 *            The connector (<code>MessagingConnectorService</code>)
	 *            parameter.
	 */
	@Override
	public synchronized void removeConnector(final MessagingConnectorService connector) {
		if (isStarted()) {
			unsubscribeMqttClient(this.client, connector.getSubscriptions());
			connector.unbinding(this);
		}
		this.connectors.remove(connector);
		if (this.connectors.isEmpty()) {
			disconnectMqttClient();
		}
	}

	/**
	 * Sets the connection definition value.
	 * The configuration of the connection definition will be used.
	 * 
	 * @param connectionDefinition
	 *            The configuration (<code>Mqttv3ConnectionDefinition</code>)
	 *            parameter.
	 * @see #setConfiguration(Configurable)
	 */
	public void setConnectionDefinition(Mqttv3ConnectionDefinition connectionDefinition) {
		this.connectionDefinition = connectionDefinition;
		super.setConfiguration(connectionDefinition.getConfiguration());
	}

	/**
	 * Create the MQTT client and start the task to connect asynchronously.
	 * The client is created only if not already created at bind time.
	 * The asynchronous connect is started only if bound and not already running.
	 * @see #bind()
	 */
	@Override
	protected synchronized void startPeriodicTasks() {
		try {
			createMqttClient();
		} catch (final Exception e) {
			logError("MQTT connection failed to start", e);
		}
		if (this.bound && !this.connectors.isEmpty()) {
			asyncConnectMqttClient(0);
		}
	}

	/**
	 * Stop the task to connect asynchronously, disconnect and delete the MQTT client. 
	 * The asynchronous connect is stopped only if it is running.
	 * The client is disconnected and deleted only if not already disconnected and deleted during unbind.
	 * @see #unbind()
	 */
	@Override
	protected synchronized void stopPeriodicTasks() {
		disconnectMqttClient();
		deleteMqttClient();
	}

	/**
	 * Subscribe mqtt client.
	 * 
	 * @param topics
	 *            The topics (<code>Topic[]</code>) parameter.
	 * @param client
	 *            The client (<code>MqttClient</code>) parameter.
	 * @throws MqttSecurityException
	 * @throws MqttException
	 */
	private void subscribeMqttClient(final MqttClient client, final Topic[] topics) throws MqttSecurityException, MqttException {
		int length = topics.length;
		if (length > 0) {
			String mbTopics[] = new String[length];
			for (int i = 0; i < length; i++) {
				mbTopics[i] = topics[i].toString();
			}
			int options[] = new int[length];
			for (int i = 0; i < length; i++) {
				options[i] = 0;
			}
			client.subscribe(mbTopics, options);
		}
	}

	/**
	 * Stop the task to connect asynchronously, disconnect and delete the MQTT client. 
	 * The asynchronous connect is stopped only if it is running.
	 * The client is disconnected and deleted only if not already disconnected and deleted during unbind.
	 * @see #stopPeriodicTasks()
	 */
	@Override
	public synchronized void unbind() {
		if (this.bound && this.connectors.isEmpty()) {
			stopPeriodicTasks();
			this.bound = false;
			/*
			 * Remove the reference to the connection definition so that cached connections are removed.
			 */
			this.connectionDefinition = null;
			super.unbind();
		}
	}

	/**
	 * Unbinding.
	 */
	protected void unbinding() {
		if (isStarted()) {
			for (MessagingConnectorService connector : this.connectors) {
				unsubscribeMqttClient(this.client, connector.getSubscriptions());
				connector.unbinding(this);
			}
			this.started = false;
		}
	}

	/**
	 * Unsubscribe mqtt client.
	 * 
	 * @param client
	 *            The client (<code>MqttClient</code>) parameter.
	 * @param topics
	 *            The topics (<code>Topic[]</code>) parameter.
	 */
	private void unsubscribeMqttClient(final MqttClient client, final Topic[] topics) {
		String mbTopics[] = new String[topics.length];
		for (int i = 0; i < mbTopics.length; i++) {
			mbTopics[i] = topics[i].toString();
		}
		try {
			client.unsubscribe(mbTopics);
		} catch (final MqttException e) {
			logWarning("Error unsubscribing MQTT client: " + e);
		}
	}

}
