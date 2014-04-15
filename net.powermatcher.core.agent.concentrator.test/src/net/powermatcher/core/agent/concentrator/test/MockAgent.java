package net.powermatcher.core.agent.concentrator.test;

import java.util.Properties;

import net.powermatcher.core.agent.framework.Agent;
import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.agent.framework.service.AgentService;
import net.powermatcher.core.configurable.BaseConfiguration;

public class MockAgent extends Agent implements AgentService  {

	public PriceInfo lastPriceUpdate;
	
	public MockAgent(String id) {
		Properties properties = new Properties();
		properties.setProperty("id", id);
		this.setConfiguration(new BaseConfiguration(properties));
	}
	
	@Override
	public void updatePriceInfo(PriceInfo newPriceInfo) {
		this.lastPriceUpdate = newPriceInfo;
		super.updatePriceInfo(newPriceInfo);
	}
	
	@Override
	protected void doBidUpdate() {
		/*
		 * Bids are published directly by sendBid, not by a periodic task. 
		 */
	}

	public void sendBid(BidInfo newBidInfo) {
		this.publishBidUpdate(newBidInfo);
	}
}
