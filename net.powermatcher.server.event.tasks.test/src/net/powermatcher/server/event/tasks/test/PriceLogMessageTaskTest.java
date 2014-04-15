package net.powermatcher.server.event.tasks.test;


import java.io.InvalidObjectException;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.Before;
import org.junit.Test;

public class PriceLogMessageTaskTest {
	
	String brokerUrl = "tcp://localhost:1883";
	String clientId = "HanBidTestClient";
	String topic = "PowerMatcher/Test/xyz/PriceInfo";
	
	// Credentials
	private static String username = "username";
	private static String password = "password";
	

	
	private static PriceLogMessageMqttPublisher publisher;
	
	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		// Create an instance of the Sample client wrapper
		publisher = new PriceLogMessageMqttPublisher(brokerUrl, clientId);
		publisher.setCredentials(username, password);
	}

	/**
	 * @throws InterruptedException 
	 * @throws MqttException 
	 * @throws InvalidObjectException 
	 * 
	 */
	@Test
	public void testPublishBidInfo1() throws InvalidObjectException, MqttException, InterruptedException {
		
		publisher.publish(topic, publisher.setUpTestPriceLogInfoMessage());
	}

	/**
	 * @throws InterruptedException 
	 * @throws MqttException 
	 * @throws InvalidObjectException 
	 * 
	 */
	@Test
	public void testPublishBidInfo2() throws InvalidObjectException, MqttException, InterruptedException {
		
		publisher.publish(topic, publisher.setUpPriceLogInfoMessage(100d));
	}

	/**
	 * @throws InterruptedException 
	 * @throws MqttException 
	 * @throws InvalidObjectException 
	 * 
	 */
	@Test
	public void testPublishBidInfo3() throws InvalidObjectException, MqttException, InterruptedException {

		publisher.publish(topic, publisher.setUpPriceLogInfoMessage(70.0d));
	}
}
