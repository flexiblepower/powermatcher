package net.powermatcher.server.event.tasks.test;


import java.io.InvalidObjectException;
import java.util.Date;

import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.agent.framework.log.PriceLogInfo;
import net.powermatcher.core.messaging.protocol.adapter.log.PriceLogMessage;

import org.eclipse.paho.client.mqttv3.MqttException;


/**
 * @author IBM
 *
 */
public class PriceLogMessageMqttPublisher extends MqttPublisher {

	private static final String CLUSTER_ID = "SAMPLE";
	private static final String AGENT_ID = "a1";
	private static final String QUALIFIER = "q1";
	private static final String CURRENCY_EUR = "EUR";
	private static final String COMMODITY_ELECTRICITY = "electricity";
	private static final int PRICE_STEPS = 5;
	private static final double MINIMUM_PRICE = -1.0d;
	private static final double MAXIMUM_PRICE = 7.0d;
	private static final int MARKET_REF = 123;
	private static final double TEST_PRICE = 55.5d;
	
	// Fields for bid message
	private MarketBasis marketBasis;

	
	/**
	 * Constructor.
	 * 
	 * @param brokerUrl
	 * @param clientId
	 * @throws MqttException
	 */
	public PriceLogMessageMqttPublisher(String brokerUrl, String clientId)
			throws MqttException {
		super(brokerUrl, clientId);

		this.setUpMarketBasis();
	}

	/**
	 * Publish a Han Bid Message to the specified topic.
	 * 
	 * @param topicName
	 * @param msg
	 * @throws InvalidObjectException
	 * @throws MqttException
	 * @throws InterruptedException
	 */
	public void publish(String topicName, PriceLogMessage msg) throws InvalidObjectException, MqttException, InterruptedException {		
		super.publish(topicName, msg.toBytes());
	}
	
	
	
	private void setUpMarketBasis() {
		this.marketBasis = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, PRICE_STEPS, MINIMUM_PRICE, MAXIMUM_PRICE, 0, MARKET_REF);
	}	
	
	
	public PriceLogMessage setUpTestPriceLogInfoMessage() {
		return setUpPriceLogInfoMessage(CLUSTER_ID, AGENT_ID, QUALIFIER, this.marketBasis, TEST_PRICE);
	}

	public PriceLogMessage setUpPriceLogInfoMessage(double price) {
		return setUpPriceLogInfoMessage(CLUSTER_ID, AGENT_ID, QUALIFIER, this.marketBasis, price);
	}

	public PriceLogMessage setUpPriceLogInfoMessage(MarketBasis mb, double price) {
		return setUpPriceLogInfoMessage(CLUSTER_ID, AGENT_ID, QUALIFIER, mb, price);
	}
	
	public PriceLogMessage setUpPriceLogInfoMessage(String clusterId, String agentId, String qualifier, MarketBasis mb, double price) {
		PriceInfo priceInfo = new PriceInfo(mb, price);
		PriceLogInfo priceLogInfo =  new PriceLogInfo(clusterId, agentId, qualifier, new Date(), priceInfo);
		return new PriceLogMessage(priceLogInfo);
	}
}
