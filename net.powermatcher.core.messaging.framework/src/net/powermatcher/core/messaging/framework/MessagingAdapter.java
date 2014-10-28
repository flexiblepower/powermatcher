package net.powermatcher.core.messaging.framework;


import net.powermatcher.core.adapter.Adapter;
import net.powermatcher.core.configurable.service.ConfigurationService;
import net.powermatcher.core.messaging.service.MessagingConnectionService;
import net.powermatcher.core.messaging.service.MessagingConnectorService;


/**
 * <p>
 * Abstract class providing messaging features for publishing and receiving messages.
 * </p>
 * <p>
 * MessagingAdapter is an abstract class providing generic implementation of the
 * MessagingConnectorService interface. The class partially implements the interface.
 * </p>
 * <p>
 * Additionally the class provides methods to publish to a topic, for which a MessagingConnectionService
 * object is required. Such an object can be provided during creation or added later using
 * the binding(MessagingConnectionService) method.
 * </p>
 * @author IBM
 * @version 0.9.0
 * 
 * @see MessagingConnectionService
 */
public abstract class MessagingAdapter extends Adapter implements MessagingConnectorService {
	private MessagingConnectionService connection;

	/**
	 * Constructs an instance of this class.
	 */
	protected MessagingAdapter() {
		/* do nothing */
	}

	/**
	 * Constructs an instance of this class from the specified properties
	 * parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 */
	protected MessagingAdapter(final ConfigurationService configuration) {
		super(configuration);
	}

	/**
	 * Bind.
	 * 
	 * @throws Exception
	 */
	@Override
	public void bind() throws Exception {
		super.bind();
	}

	/**
	 * Binding.
	 * 
	 * @throws Exception
	 */
	protected void binding() throws Exception {
		/* do nothing */
	}

	/**
	 * Binding callback event for the messaging connection.
	 * 
	 * @param connection
	 *            The messaging connection that has now become active.
	 * @throws Exception
	 */
	@Override
	public void binding(final MessagingConnectionService connection) throws Exception {
		this.connection = connection;
		binding();
	}

	/**
	 * Gets the subscriptions (Topic[]) value.
	 * 
	 * @return The subscriptions (<code>String[]</code>) value.
	 */
	@Override
	public abstract Topic[] getSubscriptions();

	/**
	 * Handle message arrived with the specified topic parameter.
	 * 
	 * @param topic
	 *            The topic (<code>Topic</code>) parameter.
	 * @return True if the message topic matched an expected pattern and has been handled.
	 */
	@Override
	public boolean handleMessageArrived(final Topic topic) {
		return false;
	}

	/**
	 * Handle message arrived with the specified topic and data parameters.
	 * 
	 * @param topic
	 *            The topic (<code>Topic</code>) parameter.
	 * @param data
	 *            The data (<code>byte[]</code>) parameter.
	 * @return True if the message topic matched an expected pattern and has been handled.
	 */
	@Override
	public boolean handleMessageArrived(final Topic topic, final byte[] data) {
		return handleMessageArrived(topic, new String(data));
	}

	/**
	 * Handle message arrived with the specified topic and data parameters.
	 * 
	 * @param topic
	 *            The topic (<code>Topic</code>) parameter.
	 * @param data
	 *            The data (<code>String</code>) parameter.
	 * @return True if the message topic matched an expected pattern and has been handled.
	 */
	@Override
	public boolean handleMessageArrived(final Topic topic, final String data) {
		return false;
	}

	/**
	 * Gets the started (boolean) value.
	 * 
	 * @return The started (<code>boolean</code>) value.
	 */
	protected boolean isStarted() {
		return this.connection != null;
	}

	/**
	 * Publish with the specified topic parameter.
	 * 
	 * @param topic
	 *            The topic (<code>Topic</code>) parameter.
	 */
	public void publish(final Topic topic) {
		if (isStarted()) {
			this.connection.publish(topic);
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
	public void publish(final Topic topic, final byte[] data) {
		if (isStarted()) {
			this.connection.publish(topic, data);
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
	public void publish(final Topic topic, final String data) {
		if (isStarted()) {
			this.connection.publish(topic, data);
		}
	}

	/**
	 * Unbind.
	 */
	@Override
	public void unbind() {
		super.unbind();
	}

	/**
	 * Unbinding.
	 */
	protected void unbinding() {
		/* do nothing */
	}

	/**
	 * Unbinding callback event for the messaging connection.
	 * 
	 * @param connection
	 *            The messaging connection that has now become inactive.
	 */
	@Override
	public void unbinding(final MessagingConnectionService connection) {
		unbinding();
		this.connection = null;

	}

}
