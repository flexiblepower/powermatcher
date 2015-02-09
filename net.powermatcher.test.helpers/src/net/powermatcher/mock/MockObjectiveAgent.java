package net.powermatcher.mock;

import java.util.HashMap;
import java.util.Map;

import net.powermatcher.api.ObjectiveEndpoint;
import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.Price;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author FAN
 * @version 2.0
 */
public class MockObjectiveAgent
    extends MockAgent
    implements ObjectiveEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockObjectiveAgent.class);

    private final Map<String, Object> objectiveAgentProperties;

    private Bid objectiveBid = null;

    public MockObjectiveAgent(String agentId) {
        super(agentId);
        objectiveAgentProperties = new HashMap<String, Object>();
        objectiveAgentProperties.put("objectiveagent", agentId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyPrice(Price newPrice) {
        LOGGER.info("ObjectiveAgent: received price update [{}] ", newPrice);
    }

    public void setObjectiveBid(Bid objectiveBid) {
        this.objectiveBid = objectiveBid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bid handleAggregateBid(Bid aggregatedBid) {
        // double[] demand = new double[] { 100.0d, 50.0d, 50.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d };
        // Bid objectiveBid = new ArrayBid(marketBasis, 0, demand);

        if (objectiveBid != null) {
            Bid aggregatedObjectiveBid = objectiveBid.aggregate(aggregatedBid);
            LOGGER.info("ObjectiveAgent: new aggregated bid: [{}] ", ((ArrayBid) aggregatedObjectiveBid).getDemand());
            return aggregatedObjectiveBid;
        } else {
            return aggregatedBid;
        }
    }
}
