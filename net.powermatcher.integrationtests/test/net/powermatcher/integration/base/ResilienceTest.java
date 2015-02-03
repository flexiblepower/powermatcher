package net.powermatcher.integration.base;

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;
import java.util.List;
import java.util.zip.DataFormatException;

import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.integration.util.CsvBidReader;
import net.powermatcher.integration.util.CsvExpectedResultsReader;
import net.powermatcher.mock.MockAgent;
import net.powermatcher.mock.SimpleSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author FAN
 * @version 2.0
 */
public class ResilienceTest {

    protected static final String MATCHERNAME = "auctioneer";
    private static final Logger LOGGER = LoggerFactory.getLogger(ResilienceTest.class);

    // Reader for the bid info input file
    protected CsvBidReader bidReader;

    // Reader for the expected results
    protected CsvExpectedResultsReader resultsReader;

    // The market basis
    protected MarketBasis marketBasis;

    // List of matcher agents (for setting market basis)
    protected List<MatcherEndpoint> matchers;

    // List of agents sending bids from
    protected List<MockAgent> agentList;

    protected void setMarketBasis(MarketBasis marketBasis) {
        this.marketBasis = marketBasis;
    }

    protected Bid nextBidToMatcher(int id) throws IOException, DataFormatException {
        MockAgent newAgent;
        Bid bid = bidReader.nextBid();

        if (bid != null) {
            newAgent = createAgent(id);
            newAgent.sendBid(bid);
        }

        return bid;
    }

    protected void sendBidsToMatcher() throws IOException, DataFormatException {
        ArrayBid bid = null;
        MockAgent newAgent;

        double[] aggregatedDemand = new double[marketBasis.getPriceSteps()];

        boolean stop = false;
        int i = 0;
        do {
            try {
                bid = bidReader.nextBid();

                if (bid != null) {
                    // Aggregated demand calculation
                    double[] demand = bid.getDemand();
                    for (int j = 0; j < demand.length; j++) {
                        aggregatedDemand[j] = aggregatedDemand[j] + demand[j];
                    }
                    if (agentList.size() > i) {
                        newAgent = agentList.get(i);
                    } else {
                        newAgent = createAgent(i);
                    }
                    newAgent.sendBid(bid);
                    i++;
                } else {
                    stop = true;
                }
            } catch (IllegalArgumentException e) {
                LOGGER.error("Incorrect bid specification caught: " + e.getMessage());
                bid = null;
            }
        } while (!stop);

        // Write aggregated demand array
        LOGGER.info("Aggregated demand: ");
        for (int j = 0; j < aggregatedDemand.length; j++) {
            if (j == (aggregatedDemand.length - 1)) {
                LOGGER.info(String.valueOf(aggregatedDemand[j]));
            } else {
                LOGGER.info(String.valueOf((aggregatedDemand[j] + ",")));
            }
        }
    }

    private MockAgent createAgent(int i, MatcherEndpoint matcher) {
        String agentId = "agent" + (i + 1);
        MockAgent newAgent = new MockAgent(agentId);
        agentList.add(i, newAgent);

        newAgent.setDesiredParentId(matcher.getAgentId());
        new SimpleSession(newAgent, matcher).connect();

        return newAgent;
    }

    public String getExpectedResultsFile(String testID, String suffix) {
        String csvSuffix = null;
        if (suffix == null) {
            csvSuffix = ".csv";
        } else {
            csvSuffix = suffix + ".csv";
        }
        return "input/" + testID + "/AggBidPrice" + csvSuffix;
    }

    public String getBidInputFile(String testID, String suffix) {
        String csvSuffix = null;
        if (suffix == null) {
            csvSuffix = ".csv";
        } else {
            csvSuffix = suffix + ".csv";
        }
        return "input/" + testID + "/Bids" + csvSuffix;
    }

    protected void checkAggregatedBid(ArrayBid aggregatedBid) {
        assertArrayEquals(resultsReader.getAggregatedBid().getDemand(), aggregatedBid.getDemand(), 0);
    }
}
