package net.powermatcher.integration.base;

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.zip.DataFormatException;

import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.core.sessions.SessionManager;
import net.powermatcher.integration.util.CsvBidReader;
import net.powermatcher.integration.util.CsvExpectedResultsReader;
import net.powermatcher.mock.MockAgent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    // SessionManager to handle the connections between matcher and agents
    protected SessionManager sessionManager;

    protected void addAgent(MockAgent agent) {
        sessionManager.addAgentEndpoint(agent);
    }

    protected void removeAgents(List<MockAgent> agents, MatcherEndpoint matcher) {
        for (MockAgent agent : agents) {
            sessionManager.removeAgentEndpoint(agent);
        }
    }

    protected void setMarketBasis(MarketBasis marketBasis) {
        this.marketBasis = marketBasis;
    }

    protected Bid nextBidToMatcher(int id) throws IOException, DataFormatException {
        MockAgent newAgent;
        Bid bid = this.bidReader.nextBid();

        if (bid != null) {
            newAgent = createAgent(id);
            newAgent.sendBid(bid);
        }

        return bid;
    }

    protected void sendBidsToMatcher() throws IOException, DataFormatException {
        Bid bid = null;
        MockAgent newAgent;

        double[] aggregatedDemand = new double[this.marketBasis.getPriceSteps()];

        boolean stop = false;
        int i = 0;
        do {
            try {
                bid = this.bidReader.nextBid();

                if (bid != null) {
                    // Aggregated demand calculation
                    double[] demand = bid.getDemand();
                    for (int j = 0; j < demand.length; j++) {
                        aggregatedDemand[j] = aggregatedDemand[j] + demand[j];
                    }
                    if (agentList.size() > i) {
                        newAgent = this.agentList.get(i);
                    } else {
                        newAgent = createAgent(i);
                    }
                    newAgent.sendBid(bid);
                    i++;
                } else {
                    stop = true;
                }
            } catch (InvalidParameterException e) {
                LOGGER.error("Incorrect bid specification found: " + e.getMessage());
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

    private MockAgent createAgent(int i) {
        String agentId = "agent" + (i + 1);
        MockAgent newAgent = new MockAgent(agentId);
        this.agentList.add(i, newAgent);
        
        newAgent.setDesiredParentId(MATCHERNAME);
        addAgent(newAgent);

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

    protected void checkAggregatedBid(Bid aggregatedBid) {
        assertArrayEquals(this.resultsReader.getAggregatedBid().getDemand(), aggregatedBid.getDemand(), 0);
    }
}
