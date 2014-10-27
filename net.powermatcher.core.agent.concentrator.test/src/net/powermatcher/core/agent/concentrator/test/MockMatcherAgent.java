package net.powermatcher.core.agent.concentrator.test;

import java.util.Properties;

import net.powermatcher.core.agent.framework.MatcherAgent;
import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.agent.framework.service.UpMessagable;
import net.powermatcher.core.configurable.BaseConfiguration;

public class MockMatcherAgent extends MatcherAgent implements UpMessagable {

	public BidInfo lastReceivedBid;
	
	public MockMatcherAgent(String id) {
		Properties properties = new Properties();
		properties.setProperty("id", id);
		this.setConfiguration(new BaseConfiguration(properties));
	}
	
	public void sendPrice(PriceInfo price) {
		this.publishPriceInfo(price);
	}
	
	@Override
	public void updateBidInfo(String agentId, BidInfo newBidInfo) {
		this.lastReceivedBid = newBidInfo;
		super.updateBidInfo(agentId, newBidInfo);
	}
}
