package net.powermatcher.mock;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.powermatcher.api.ObjectiveEndpoint;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;

public class MockObjectiveAgent extends MockAgent implements ObjectiveEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockObjectiveAgent.class);

    private static final String CURRENCY_EUR = "EUR";
    private static final String COMMODITY_ELECTRICITY = "electricity";
    
    private Map<String, Object> objectiveAgentProperties;

    public MockObjectiveAgent(String agentId) {
        super(agentId);
        this.objectiveAgentProperties = new HashMap<String, Object>();
        this.objectiveAgentProperties.put("objectiveagent", agentId);
    }

    @Override
    public void notifyPriceUpdate(Price newPrice) {
        LOGGER.info("ObjectiveAgent: received price update [{}] ", newPrice);
    }

    @Override
    public Bid handleAggregateBid(Bid aggregatedBid) {
        MarketBasis marketBasis = new MarketBasis(COMMODITY_ELECTRICITY, CURRENCY_EUR, 5, -1.0d, 7.0d);
        double[] demand = new double[] { 100.0d, 50.0d, 50.0d, 0.0d, 0.0d };

        Bid objectiveBid = new Bid(marketBasis, demand);

        Bid aggregatedObjectiveBid = objectiveBid.aggregate(aggregatedBid);

        LOGGER.info("ObjectiveAgent: new aggregated bid: [{}] ", aggregatedObjectiveBid.getDemand());
        return aggregatedObjectiveBid;
    }
}
