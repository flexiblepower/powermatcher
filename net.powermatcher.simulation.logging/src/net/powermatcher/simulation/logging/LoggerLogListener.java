package net.powermatcher.simulation.logging;

import net.powermatcher.core.agent.framework.log.BidLogInfo;
import net.powermatcher.core.agent.framework.log.LogListenerService;
import net.powermatcher.core.agent.framework.log.PriceLogInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * For testing
 */
public class LoggerLogListener implements LogListenerService {

	private static Logger logger = LoggerFactory.getLogger(LoggerLogListener.class);

	@Override
	public void handleBidLogInfo(BidLogInfo bidLogInfo) {
		logger.info("New bid from " + bidLogInfo.getAgentId() + ": " + bidLogInfo.getBidInfo().toString());
	}

	@Override
	public void handlePriceLogInfo(PriceLogInfo priceLogInfo) {
		logger.info("New price from " + priceLogInfo.getAgentId() + ": " + priceLogInfo.getCurrentPrice());
	}

}
