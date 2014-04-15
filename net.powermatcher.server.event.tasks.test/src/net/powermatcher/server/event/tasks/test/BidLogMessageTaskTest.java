package net.powermatcher.server.event.tasks.test;


import java.io.InvalidObjectException;
import java.util.Date;

import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PricePoint;
import net.powermatcher.core.agent.framework.log.BidLogInfo;
import net.powermatcher.core.messaging.protocol.adapter.log.BidLogMessage;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.junit.Before;
import org.junit.Test;


public class BidLogMessageTaskTest {
	
	private static String brokerUrl = "tcp://localhost:1883";
	private static String clientId = "HanBidTestClient";
	private static String topic = "PowerMatcher/Test/xyz/UpdateBid/Log";
	
	// Credentials
	private static String username = null;
	private static String password = null;
	
	// Market base and message constants
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
	
	private static BidLogMessageMqttPublisher publisher;
	
	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		// Create an instance of the Sample client wrapper
		publisher = new BidLogMessageMqttPublisher(brokerUrl, clientId);
		if (username != null && password != null) {
			publisher.setCredentials(username, password);
		}
	}
	
	@Test
	public void testFixedPriceMessage() throws MqttException, InterruptedException {
		
		MarketBasis mb = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, PRICE_STEPS, MINIMUM_PRICE, MAXIMUM_PRICE, 0, MARKET_REF);
		BidInfo bidInfo = new BidInfo(mb);
		double effectivePrice = TEST_PRICE;
		BidLogMessage msg = createBidLogMessage(bidInfo, mb, effectivePrice);
		publisher.publish(topic, msg.toBytes());
	}
	
	@Test
	public void testDemandBasedMessage() throws MqttException, InterruptedException {
		MarketBasis mb = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, PRICE_STEPS, MINIMUM_PRICE, MAXIMUM_PRICE, 0, MARKET_REF);
		double[] demand = new double[] { 100.0d, 50.0d, 50.0d, 0.0d, 0.0d };
		BidInfo bidInfo = new BidInfo(mb, demand);
		double effectivePrice = TEST_PRICE;
		BidLogMessage msg = createBidLogMessage(bidInfo, mb, effectivePrice);
		publisher.publish(topic, msg.toBytes());
	}
	
	@Test
	public void testPointBasedMessage() throws MqttException, InterruptedException {
		MarketBasis mb = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, PRICE_STEPS, MINIMUM_PRICE, MAXIMUM_PRICE, 0, MARKET_REF);
		PricePoint[] points = new PricePoint[] { new PricePoint(-1, 50.0d), new PricePoint(-1, 0.0d) };
		BidInfo bidInfo = new BidInfo(mb, points);
		double effectivePrice = TEST_PRICE;
		BidLogMessage msg = createBidLogMessage(bidInfo, mb, effectivePrice);
		publisher.publish(topic, msg.toBytes());
	}
	
	@Test
	public void testContinuousDemandMessage() throws MqttException, InterruptedException {
		MarketBasis mb = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, PRICE_STEPS, MINIMUM_PRICE, MAXIMUM_PRICE, 0, MARKET_REF);
		PricePoint[] points = new PricePoint[] { new PricePoint(3, 50.0d), new PricePoint(3, 50.0d) };
		BidInfo bidInfo = new BidInfo(mb, points);
		double effectivePrice = TEST_PRICE;
		BidLogMessage msg = createBidLogMessage(bidInfo, mb, effectivePrice);
		publisher.publish(topic, msg.toBytes());
	}

	@Test
	public void testMaxPriceSteps() throws InvalidObjectException, MqttException, InterruptedException {
		int priceSteps = 1024;
		double minPrice = 0;
		double maxPrice = 1024;
		MarketBasis mb = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, priceSteps, minPrice, maxPrice, 0, MARKET_REF);
		
		double[] demand = new double[priceSteps];
		int price = priceSteps;
		for (int i = 0; i < demand.length; i++) {
			demand[i] = price--;
		}
		BidInfo bidInfo = new BidInfo(mb, demand);
		
		double effectivePrice = TEST_PRICE;
		BidLogMessage msg = createBidLogMessage(bidInfo, mb, effectivePrice);
		publisher.publish(topic, msg.toBytes());
	}

	
//	@Test
//	public void testExceedMaxPriceSteps() throws InvalidObjectException, MqttException, InterruptedException {
//		int priceSteps = 1025;
//		double minPrice = 0;
//		double maxPrice = 1024;
//		MarketBasis mb = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, priceSteps, minPrice, maxPrice, 0, MARKET_REF);
//		
//		double[] demand = new double[1025];
//		int price = 1024;
//		for (int i = 0; i < demand.length; i++) {
//			demand[i] = price--;
//		}
//		BidInfo bidInfo = new BidInfo(mb, demand);
//		
//		double effectivePrice = TEST_PRICE;
//		BidLogMessage msg = createBidLogMessage(bidInfo, mb, effectivePrice);
//		publisher.publish(topic, msg.toBytes());
//	}
	
//	@Test
//	public void testLogWithoutBidInfoMessage() throws MqttException, InterruptedException {
//		
//		MarketBasis mb = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, PRICE_STEPS, MINIMUM_PRICE, MAXIMUM_PRICE, 0, MARKET_REF);
//		BidInfo bidInfo = new BidInfo(mb);
//		double effectivePrice = TEST_PRICE;
//		BidLogInfo bidLogInfo =  new BidLogInfo(CLUSTER_ID, AGENT_ID, QUALIFIER, mb, effectivePrice, 55.0d, 0.0d, 70.0d, bidInfo);
//		BidLogMessage msg = new BidLogMessage(bidLogInfo);
//		publisher.publish(topic, msg.toBytes());
//	}

	private BidLogMessage createBidLogMessage(BidInfo bidInfo, MarketBasis mb, double effectivePrice) {
		double effectiveDemand = bidInfo.getDemand(effectivePrice);
		double maximumDemand = bidInfo.getMaximumDemand();
		double minimumDemand = bidInfo.getMinimumDemand();
		BidLogInfo bidLogInfo =  new BidLogInfo(CLUSTER_ID, AGENT_ID, QUALIFIER, new Date(), mb, effectivePrice, effectiveDemand, minimumDemand, maximumDemand, bidInfo);
		return new BidLogMessage(bidLogInfo);
	}
}
