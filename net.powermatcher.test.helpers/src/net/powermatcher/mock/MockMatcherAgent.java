package net.powermatcher.mock;

import java.util.Arrays;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.messages.BidUpdate;
import net.powermatcher.api.messages.PriceUpdate;

/**
 * This {@link MockMatcherAgent} can be used in testcases when a simple matcher is needed. It can only connect to a
 * single agent and you can manually send a price update.
 *
 * @author FAN
 * @version 2.1
 */
public class MockMatcherAgent
    extends MockObservableAgent
    implements MatcherEndpoint {

    private final String clusterId;
    private final MarketBasis marketBasis;

    private volatile Session session;
    private volatile BidUpdate lastReceivedBid;

    public MockMatcherAgent(String agentId, String clusterId, MarketBasis marketBasis) {
        super(agentId);
        this.clusterId = clusterId;
        this.marketBasis = marketBasis;
    }

    @Override
    public AgentEndpoint.Status getStatus() {
        final MarketBasis marketBasis = this.marketBasis;
        final String clusterId = this.clusterId;
        return new AgentEndpoint.Status() {
            @Override
            public boolean isConnected() {
                return true;
            }

            @Override
            public MarketBasis getMarketBasis() {
                return marketBasis;
            }

            @Override
            public String getClusterId() {
                return clusterId;
            }

            @Override
            public Session getSession() {
                return null;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connectToAgent(Session session) {
        session.setMarketBasis(marketBasis);
        this.session = session;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void agentEndpointDisconnected(Session session) {
        this.session = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleBidUpdate(Session session, BidUpdate bidUpdate) {
        lastReceivedBid = bidUpdate;
    }

    /**
     * @return the current value of lastReceivedBid.
     */
    public BidUpdate getLastReceivedBid() {
        return lastReceivedBid;
    }

    public void resetLastReceivedBid() {
        lastReceivedBid = null;
    }

    /**
     * @return The current session with the connected agent
     */
    public Session getSession() {
        return session;
    }

    public void publishPrice(PriceUpdate priceUpdate) {
        session.updatePrice(priceUpdate);
    }

    public void assertTotalBid(double... expectedDemand) {
        double[] realDemand = lastReceivedBid.getBid().getDemand();
        if (expectedDemand.length != realDemand.length) {
            throw new AssertionError(Arrays.toString(expectedDemand) + " != " + Arrays.toString(realDemand));
        }
        for (int ix = 0; ix < expectedDemand.length; ix++) {
            if (expectedDemand[ix] != realDemand[ix]) {
                throw new AssertionError(Arrays.toString(expectedDemand) + " != " + Arrays.toString(realDemand));
            }
        }
    }
}
