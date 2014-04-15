package net.powermatcher.server.event.tasks.test;


import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/**
 * @author IBM
 *
 */
public class MqttPublisher {

	/**
	 * Define the logger (Logger) field.
	 */
	protected final static Logger logger = Logger.getLogger(MqttPublisher.class.getName());
	
	// Private instance variables
	private MqttClient client;
	private String brokerUrl;
	private MqttConnectOptions conOpt;
	private String clientId;
	
	/**
	 * MqttPublisher constructor.
	 *  
	 * @param brokerUrl
	 * 			the URL of the broker
	 * @param clientId
	 * 			the id of the MQTT client.
	 * @throws MqttException
	 */
	public MqttPublisher(String brokerUrl, String clientId) throws MqttException {
		super();
		this.brokerUrl = brokerUrl;
		this.clientId = clientId;
		
		// Construct the MqttClient instance
		this.client = new MqttClient(this.brokerUrl, clientId);
		
		// Connection options object
		this.conOpt = new MqttConnectOptions();
	}
	
	/**
	 * Set username and password if this is required for 
	 * publishing to the topic.
	 * 
	 * @param username
	 * @param password
	 */
	public void setCredentials(String username, String password) {
		this.conOpt.setUserName(username);
		this.conOpt.setPassword(password.toCharArray());
	}
	
	/**
	 * Performs a single publish
	 * 
	 * @param topicName
	 *            the topic to publish to
	 * @param payload
	 *            the payload of the message to publish
	 * @throws MqttException
	 * @throws InterruptedException 
	 */
	public void publish(String topicName, byte[] payload)
			throws MqttException, InterruptedException {

		// Connect to the server
		this.client.connect(this.conOpt);

		logger.info("Connected to " + this.brokerUrl);

		// Get an instance of the topic
		MqttTopic topic = this.client.getTopic(topicName);

		MqttMessage message = new MqttMessage(payload);

		// Publish the message
		logger.info("Client " + this.clientId + " publishing at: "
				+ System.currentTimeMillis() + " to topic \"" + topicName
				+ " payload:" + message.getPayload().toString());
		MqttDeliveryToken token = topic.publish(message);

		// Wait until the message has been delivered to the server
		token.waitForCompletion();

		// Disconnect the client
		this.client.disconnect();
		logger.info("Disconnected");

	}
	
}
