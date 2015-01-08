package net.powermatcher.mock;

import java.util.HashMap;
import java.util.Map;

import net.powermatcher.api.ObjectiveEndpoint;
import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.PriceUpdate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author FAN
 * @version 2.0
 */
public class MockObjectiveAgent extends MockAgent implements ObjectiveEndpoint {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(MockObjectiveAgent.class);

	private static final String CURRENCY_EUR = "EUR";
	private static final String COMMODITY_ELECTRICITY = "electricity";

	private Map<String, Object> objectiveAgentProperties;

	public MockObjectiveAgent(String agentId) {
		super(agentId);
		this.objectiveAgentProperties = new HashMap<String, Object>();
		this.objectiveAgentProperties.put("objectiveagent", agentId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void notifyPriceUpdate(PriceUpdate newPrice) {
		LOGGER.info("ObjectiveAgent: received price update [{}] ", newPrice);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Bid handleAggregateBid(Bid aggregatedBid) {
		MarketBasis marketBasis = new MarketBasis(COMMODITY_ELECTRICITY,
				CURRENCY_EUR, 11, 0, 10);
		double[] demand = new double[] { 100.0d, 50.0d, 50.0d, 0.0d, 0.0d,
				0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d };

		Bid objectiveBid = new ArrayBid(marketBasis, 0, demand);

		Bid aggregatedObjectiveBid = objectiveBid.aggregate(aggregatedBid);

		LOGGER.info("ObjectiveAgent: new aggregated bid: [{}] ",
				((ArrayBid) aggregatedObjectiveBid).getDemand());
		return aggregatedObjectiveBid;
	}
}
