package net.powermatcher.core.messaging.framework.test;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import net.powermatcher.core.configurable.BaseConfiguration;
import net.powermatcher.core.configurable.service.Configurable;
import net.powermatcher.core.messaging.framework.MessagingAdapter;
import net.powermatcher.core.messaging.framework.Topic;
import net.powermatcher.core.messaging.mqttv3.Mqttv3ConnectionFactory;
import net.powermatcher.core.messaging.service.MessagingConnectionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author IBM
 * @version 0.9.0
 */
public class SimpleTestAgent extends MessagingAdapter {
	/**
	 * Define the logger (Logger) field.
	 */
	protected final static Logger logger = LoggerFactory.getLogger(SimpleTestAgent.class);
	/**
	 * Define the topic prefix (Topic) constant.
	 */
	private static final Topic TOPIC_PREFIX = Topic.create("PowerMatcher/Test");
	/**
	 * Define the request topic (Topic) constant.
	 */
	private static final Topic REQUEST_TOPIC = TOPIC_PREFIX.addLevel("request").addLevel("test");
	/**
	 * Define the request pattern (Topic) constant.
	 */
	private static final Topic REQUEST_PATTERN = TOPIC_PREFIX.addLevel("request").addLevel(Topic.MULTI_LEVEL_WILDCARD);
	/**
	 * Define the response topic prefix (Topic) constant.
	 */
	private static final Topic RESPONSE_TOPIC_PREFIX = TOPIC_PREFIX.addLevel("response");

	/**
	 * Main with the specified arguments parameter.
	 * 
	 * @param args
	 *            The arguments (<code>String[]</code>) parameter.
	 */
	public static void main(final String[] args) {
		new SimpleTestAgent().run(args);
	}

	/**
	 * Define the agent configuration file (String) field.
	 */
	private String agentConfigFile = "agent_config.properties";

	/**
	 * Define the message (String) field.
	 */
	private String message;

	/**
	 * Constructs an instance of this class.
	 * 
	 */
	public SimpleTestAgent() {
	}

	/**
	 * Constructs an instance of this class from the specified properties
	 * parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 */
	public SimpleTestAgent(final Configurable configuration) {
		super(configuration);
	}

	@Override
	protected void binding() throws Exception {
		super.binding();
		publish(REQUEST_TOPIC);
	}

	/**
	 * Gets the subscriptions (Topic[]) value.
	 * 
	 * @return The subscriptions (<code>String[]</code>) value.
	 */
	@Override
	public Topic[] getSubscriptions() {
		return new Topic[] { REQUEST_PATTERN };
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
		if (topic.matches(REQUEST_PATTERN)) {
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
		logInfo("handleRequest topic=" + topic + " data=" + data);
		publish(RESPONSE_TOPIC_PREFIX.addLevel("text"), this.message);
		publish(RESPONSE_TOPIC_PREFIX.addLevel("bin"), this.message.getBytes());
		publish(RESPONSE_TOPIC_PREFIX.addLevel("empty"));
	}

	/**
	 * Initialize.
	 */
	protected void initialize() {
		this.message = getProperty("message", "NO MESSAGE");
	}

	/**
	 * Load properties with the specified file name parameter.
	 * 
	 * @param fileName
	 *            The file name (<code>String</code>) parameter.
	 * @throws IOException
	 *             IOException.
	 */
	protected Properties loadProperties(final String fileName) throws IOException {
		logger.info("Loading the agent properties");
		Properties agentProperties = new Properties();
		try {
			agentProperties.load(new FileInputStream(fileName));
			return agentProperties;
		} catch (final IOException e) {
			logger.error("Could not open properties file: " + fileName, e);
			throw e;
		}
	}

	/**
	 * Read command line arguments with the specified arguments parameter and
	 * return the boolean result.
	 * 
	 * @param args
	 *            The arguments (<code>String[]</code>) parameter.
	 * @return Results of the read command line arguments (<code>boolean</code>)
	 *         value.
	 */
	private boolean readCommandLineArgs(final String[] args) {
		for (int index = 0; index < args.length; ++index) {
			if (args[index].equalsIgnoreCase("-agentConfig") && index + 1 < args.length) {
				this.agentConfigFile = args[++index];
			} else {
				logger.error("Usage: SimpleTestAgent [-agentConfig <file>]");
				return false;
			}
		}
		return true;
	}

	/**
	 * Run with the specified arguments parameter.
	 * 
	 * @param args
	 *            The arguments (<code>String[]</code>) parameter.
	 */
	private void run(final String[] args) {
		boolean success = readCommandLineArgs(args);
		if (success) {
			try {
				Properties agentProperties = loadProperties(this.agentConfigFile);
				Configurable configuration = new BaseConfiguration(agentProperties);
				setConfiguration(configuration);
				Mqttv3ConnectionFactory connectionFactory = new Mqttv3ConnectionFactory(); 
				MessagingConnectionService agentConnection = connectionFactory.createAdapter(configuration, this);
				logInfo("Binding agent and connection");
				bind();
				agentConnection.bind();
				int secs = 10;
				logInfo("Waiting " + secs + " seconds");
				Thread.sleep(secs * 1000l);
				logInfo("Unbinding agent and connection");
				agentConnection.unbind();
				unbind();
			} catch (final Exception e) {
				success = false;
			}
		}
		logInfo("Done");
	}

	@Override
	public void setConfiguration(final Configurable configuration) {
		super.setConfiguration(configuration);
		initialize();
	}
}
