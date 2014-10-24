package net.powermatcher.core.messaging.adapter.template;


import net.powermatcher.core.agent.template.config.ExampleAgent2Configuration;
import net.powermatcher.core.agent.template.service.ExampleConnectorService;
import net.powermatcher.core.agent.template.service.ExampleControlService;
import net.powermatcher.core.agent.template.service.ExampleNotificationService;
import net.powermatcher.core.configurable.service.Configurable;
import net.powermatcher.core.messaging.adapter.template.config.ExampleAdapterConfiguration;
import net.powermatcher.core.messaging.adapter.template.constants.ExampleAdapterConstants;
import net.powermatcher.core.messaging.framework.MessagingAdapter;
import net.powermatcher.core.messaging.framework.Topic;


/**
 * Th<code>ExampleAdapter</code> class implements an adapter for the <code>ExampleConnectorService</code> of
 * agent <code>ExampleAgent2</code>.
 * <p>
 * The adapter sends a message to itself when the agent notifies the adapter that something has changed, and when
 * it receives the loopback message, it invokes a method on the control interface of the agent to trigger an action.
 * </p>  
 * <p>
 * This class serves as the template for implementing, for example, device adapters. Instead of sending loopback
 * messages, control messages can be sent to the device. Asynchronous notifications received from the device can 
 * result in requests to the agent via the control interface.
 * </p>  
 * <p>
 * If the device does not have a messaging interface, or if one does not want to encapsulate the
 * transport protocol into a separate messaging adapter, the transport interface can be implemented
 * directly in this adapter. In this case the superclass should be <code>Adapter</code> instead of
 * <code>MessagingAdapter</code>.  
 * </p>  
 * 
 * @see ExampleConnectorService
 * @see ExampleNotificationService
 * @see ExampleControlService
 * 
 * @author IBM
 * @version 0.9.0
 */
public class ExampleAdapter extends MessagingAdapter {

	/**
	 * @author IBM
	 * @version 0.9.0
	 */
	private class NotificationListener implements ExampleNotificationService {

		/**
		 * Notification message from agent to adapter.
		 * This adapter will send a example message to itself for this event.
		 * @see net.powermatcher.core.agent.example.service.ExampleNotificationService#somethingChanged()
		 */
		@Override
		public void somethingChanged() {
			handleSomethingChanged();
		}

	}

	/**
	 * Define the notification service (ExampleNotificationService) field.
	 * The notification service interface that the adapter implements 
	 * is bound to the agent via the agent's connector.
	 */
	private ExampleNotificationService notificationService;
	/**
	 * Define the example setting (String) field.
	 * The example setting is a configuration property for the adapter. 
	 * 
	 * @see ExampleAdapterConfiguration#EXAMPLE_PROPERTY
	 */
	private String exampleSetting;
	/**
	 * Define the example topic (Topic) field.
	 * This is the example topic that the adapter uses to send a message to itself.
	 */
	private Topic exampleTopic;
	/**
	 * Define the example pattern (Topic) field.
	 * This is the topic pattern that the adapter uses to receive the example message it sends to itself.
	 */
	private Topic examplePattern;

	/**
	 * Define the control service (ExampleControlService) field.
	 * The control service interface that the agent implements 
	 * is received via the agent's connector when the adapter binds to the agent.
	 */
	private ExampleControlService controlService;

	/**
	 * Define the example connector (ExampleConnectorService) field.
	 * This is the connector of the agent that the adapter will bind to and unbind from.
	 */
	private ExampleConnectorService connectorService;

	/**
	 * Constructs an unconfigured instance of this class.
	 */
	public ExampleAdapter() {
		super();
	}

	/**
	 * Constructs an instance of this class with the specified configuration.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 */
	public ExampleAdapter(final Configurable configuration) {
		super(configuration);
	}

	/**
	 * Bind the adapter to the agent.
	 * Note that the adapter can be bound to an agent, but not yet bound to by a connection,
	 * and vice versa.
	 * 
	 * @see #unbind()
	 * @throws Exception
	 */
	@Override
	public void bind() throws Exception {
		super.bind();
		this.connectorService.bind(this.notificationService);
	}

	/**
	 * A messaging connection is binding to this adapter.
	 * When binding the adapter can initialize its external interface, if required. 
	 * Note that the adapter can be bound to an agent, but not yet bound to by a connection,
	 * and vice versa.
	 * 
	 * @throws Exception
	 *             Exception.
	 * @see #unbinding()
	 */
	@Override
	protected void binding() throws Exception {
		super.binding();
	}

	/**
	 * Gets the connector (ExampleConnectorService) value.
	 * 
	 * @return The connector (ExampleConnectorService) value.
	 */
	public ExampleConnectorService getConnector() {
		return this.connectorService;
	}

	/**
	 * Gets the subscriptions (Topic[]) value.
	 * This returns the message topic patterns that the adapter wishes to receive,
	 * which is just the example topic in this example.
	 * 
	 * @return The subscriptions (<code>String[]</code>) value.
	 */
	@Override
	public Topic[] getSubscriptions() {
		return new Topic[] { this.examplePattern };
	}

	/**
	 * Handle an external message with the specified topic parameter.
	 * This callback method is invoked if a message without a payload is received.
	 * <p>
	 * Messages with empty payload are supported in some messaging connections only.
	 * If a message is sent with an empty payload, and the messaging connection does not
	 * support an empty payload, it is received as a binary payload of length null.  
	 * </p>
	 * <p>
	 * As multiple adapters may share the same messaging connection, the implementation of this method
	 * must check whether the topic is one of the topics it wishes to receive.
	 * In this case, if the topic matches the example topic, the adapter invokes the <code>doSomething</code>`
	 * method on the agent.
	 * </p>
	 * 
	 * @param topic
	 *            The topic (<code>Topic</code>) parameter.
	 * @return True if the message topic matched an expected pattern and has been handled.
	 * @see #handleMessageArrived(Topic, byte[])
	 * @see #handleMessageArrived(Topic, String)       
	 */
	@Override
	public boolean handleMessageArrived(Topic topic) {
		if (topic.matches(this.examplePattern)) {
			ExampleControlService controlService = this.controlService;
			if (controlService != null) {
				controlService.doSomething();
			}
		} else {
			return super.handleMessageArrived(topic);
		}
		return true;
	}

	/**
	 * Handle an external message with the specified topic and data parameters.
	 * This callback method is invoked if a message with a binary payload is received.
	 * <p>
	 * Some messaging connections support only binary payloads, others differentiates between 
	 * string and binary payloads.
	 * </p>
	 * <p>
	 * For topics that carry a string that has been encoded as a binary payload, this method must 
	 * call its super method. The super method converts the binary payload to a string and 
	 * calls the string variant of <code>handleMessageArrived</code>.
	 * </p>
	 * <p>
	 * As multiple adapters may share the same messaging connection, the implementation of this method
	 * must check whether the topic is one of the topics it wishes to receive.
	 * </p>
	 * 
	 * @param topic
	 *            The topic (<code>Topic</code>) parameter.
	 * @param data
	 *            The data (<code>byte[]</code>) parameter.
	 * @return True if the message topic matched an expected pattern and has
	 *         been handled.
	 * @see #handleMessageArrived(Topic)
	 * @see #handleMessageArrived(Topic, String)       
	 */
	@Override
	public boolean handleMessageArrived(final Topic topic, final byte[] data) {
		return super.handleMessageArrived(topic, data);
	}

	/**
	 * Handle an external message with the specified topic and data parameters.
	 * This callback method is invoked if an message with a string payload is received,
	 * or if a message with a string that was encoded as a binary payload has been received.
	 * <p>
	 * As multiple adapters may share the same messaging connection, the implementation of this method
	 * must check whether the topic is one of the topics it wishes to receive.
	 * In this case, if the topic matches the example topic, the adapter invokes the <code>doSomething</code>`
	 * method on the agent.
	 * </p>
	 * 
	 * @param topic
	 *            The topic (<code>Topic</code>) parameter.
	 * @param data
	 *            The data (<code>String</code>) parameter.
	 * @return True if the message topic matched an expected pattern and has been handled.
	 * @see #handleMessageArrived(Topic)
	 * @see #handleMessageArrived(Topic, byte[])   
	 * @see ExampleControlService#doSomething()    
	 */
	@Override
	public boolean handleMessageArrived(Topic topic, String data) {
		if (topic.matches(this.examplePattern)) {
			return handleMessageArrived(topic);
		}
		return super.handleMessageArrived(topic, data);
	}

	/**
	 * Handle an event or a request received from the agent via the notification interface.
	 * Here the example topic is published to the messaging connection, without payload data.
	 * 
	 * @see ExampleAdapter#handleMessageArrived(Topic) 
	 */
	private void handleSomethingChanged() {
		/*
		 * Publish a message to a topic without payload data.
		 * The other variants of the publis method support publishing with string or binary payload
		 * data.
		 */
		this.publish(this.exampleTopic);
	}

	/**
	 * Initialize the adapter by setting the adapter's fields with the configured
	 * properties or their default values, if applicable.
	 */
	private void initialize() {
		this.exampleSetting = getProperty(ExampleAgent2Configuration.EXAMPLE_PROPERTY,
				ExampleAgent2Configuration.EXAMPLE_DEFAULT);
		logInfo("The example setting is initialized to: " + this.exampleSetting);
		this.exampleTopic = Topic.create(ExampleAdapterConstants.EXAMPLE_TOPIC_PREFIX).addLevel(getClusterId())
				.addLevel(getId());
		this.examplePattern = this.exampleTopic;
	}

	/**
	 * Returns whether the adapter is enabled (boolean) or not.
	 * The adapter is enabled only if both the adapter itself is enabled (a property) as
	 * well as the agent it connects to.
	 * 
	 * @return The enabled (<code>boolean</code>) value.
	 */
	@Override
	public boolean isEnabled() {
		return super.isEnabled() && this.connectorService.isEnabled();
	}

	/**
	 * Configures the adapter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 */
	@Override
	public void setConfiguration(final Configurable configuration) {
		super.setConfiguration(configuration);
		initialize();
	}

	/**
	 * Sets the connector the adapter will bind to and unbind from.
	 * 
	 * @param connectorService
	 *            The connector (<code>ExampleConnectorService</code>)
	 *            parameter.
	 */
	public void setConnector(final ExampleConnectorService connectorService) {
		this.connectorService = connectorService;
		if (connectorService == null) {
			this.controlService = null;
			this.notificationService = null;
		} else {
			this.controlService = connectorService.getExampleService();
			this.notificationService = new ExampleAdapter.NotificationListener();
		}
	}

	/**
	 * Unbind the adapter from the agent.
	 * Note that the adapter can be bound to an agent, but not yet bound to by a connection,
	 * and vice versa.
	 * 
	 * @see #bind()
	 */
	@Override
	public void unbind() {
		this.connectorService.unbind(this.notificationService);
		super.unbinding();
	}

	/**
	 * A messaging connection is unbinding from this adapter.
	 * @see #binding()
	 */
	@Override
	protected void unbinding() {
		super.unbinding();
	}

}
