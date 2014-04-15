package net.powermatcher.server.event.tasks.test;


import java.io.InvalidObjectException;

import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.MarketBasisCache;
import net.powermatcher.core.agent.framework.data.PricePoint;
import net.powermatcher.core.agent.framework.log.BidLogInfo;
import net.powermatcher.core.messaging.protocol.adapter.han.AbstractHANMessage;
import net.powermatcher.core.messaging.protocol.adapter.han.HANBidMessage;
import net.powermatcher.core.messaging.protocol.adapter.han.HANMarketBasisMapper;
import net.powermatcher.core.messaging.protocol.adapter.han.AbstractHANMessage.Commodity;
import net.powermatcher.core.messaging.protocol.adapter.log.BidLogMessage;

import org.eclipse.paho.client.mqttv3.MqttException;


/**
 * @author IBM
 *
 */
public class BidLogMessageMqttPublisher extends MqttPublisher {

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
	public BidLogMessageMqttPublisher(String brokerUrl, String clientId)
			throws MqttException {
		super(brokerUrl, clientId);

		//this.setUpMarketBasis();
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
	public void publish(String topicName, BidLogMessage msg) throws InvalidObjectException, MqttException, InterruptedException {		
		super.publish(topicName, msg.toBytes());
	}
	
	
	
//	private void setUpMarketBasis() {
//		this.marketBasis = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, PRICE_STEPS, MINIMUM_PRICE, MAXIMUM_PRICE, 0, MARKET_REF);
//	}	
	
//	private BidLogMessage setUpBidLogInfoMessage(BidInfo bidInfo, double effectivePrice) {
//		double effectiveDemand = bidInfo.getDemand(effectivePrice);
//		double maximumDemand = bidInfo.getMaximumDemand();
//		double minimumDemand = bidInfo.getMinimumDemand();
//		BidLogInfo bidLogInfo = new BidLogInfo(CLUSTER_ID, AGENT_ID, QUALIFIER, this.marketBasis, effectivePrice, effectiveDemand, minimumDemand, maximumDemand, bidInfo);
//		return this.setUpBidLogInfoMessage(bidInfo, clusterId, agentId, qualifier, marketBasis, effectivePrice);
//		//new BidLogMessage(bidLogInfo);
//	}
	
//	private BidLogMessage setUpBidLogInfoMessage(BidInfo bidInfo, String clusterId, String agentId, String qualifier, MarketBasis marketBasis, double effectivePrice) {
//		double effectiveDemand = bidInfo.getDemand(effectivePrice);
//		double maximumDemand = bidInfo.getMaximumDemand();
//		double minimumDemand = bidInfo.getMinimumDemand();
//		BidLogInfo bidLogInfo = new BidLogInfo(clusterId, agentId, qualifier, marketBasis, effectivePrice, effectiveDemand, minimumDemand, maximumDemand, bidInfo);
//		return new BidLogMessage(bidLogInfo);
//	}	
	
//	public BidLogMessage setUpBidInfo1() {
//		BidInfo bidInfo1 = new BidInfo(this.marketBasis);
//		
//		return setUpBidLogInfoMessage(bidInfo1, TEST_PRICE);
//	}
//	
//	public BidLogMessage setUpBidInfo2() {
//		double[] demand2 = new double[] { 100.0d, 50.0d, 50.0d, 0.0d, 0.0d };
//		BidInfo bidInfo2 = new BidInfo(this.marketBasis, demand2);
//		
//		return setUpBidLogInfoMessage(bidInfo2, TEST_PRICE);
//	}
	
//	public BidLogMessage setUpBidInfo3() {
//		PricePoint[] points3 = new PricePoint[] { new PricePoint(-1, 50.0d), new PricePoint(-1, 0.0d) };
//		BidInfo bidInfo3 = new BidInfo(this.marketBasis, points3);
//		
//		return setUpBidLogInfoMessage(bidInfo3, 11.0d);
//	}
//	
//	public BidLogMessage setUpBidInfo4() {
//		PricePoint[] points3 = new PricePoint[] { new PricePoint(3, 50.0d), new PricePoint(3, 50.0d) };
//		BidInfo bidInfo3 = new BidInfo(this.marketBasis, points3);
//		
//		return setUpBidLogInfoMessage(bidInfo3, TEST_PRICE);
//	}
}
