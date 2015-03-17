package net.powermatcher.mock;

import static org.junit.Assert.assertArrayEquals;

import java.util.HashMap;
import java.util.Map;

import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.messages.BidUpdate;
import net.powermatcher.api.messages.PriceUpdate;

/**
 *
 * @author FAN
 * @version 2.0
 */
public class MockMatcherAgent
    extends MockAgent
    implements MatcherEndpoint {

    private final Map<String, Object> matcherProperties;
    private BidUpdate lastReceivedBid;
    private MarketBasis marketBasis;
    private final String clusterId;

    public MockMatcherAgent(String agentId, String clusterId) {
        super(agentId);
        this.clusterId = clusterId;
        matcherProperties = new HashMap<String, Object>();
        matcherProperties.put("matcherId", agentId);
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
     * @return the current value of matcherProperties.
     */
    public Map<String, Object> getMatcherProperties() {
        return matcherProperties;
    }

    /**
     * @return the current value of marketBasis.
     */
    public MarketBasis getMarketBasis() {
        return marketBasis;
    }

    public void setMarketBasis(MarketBasis marketBasis) {
        this.marketBasis = marketBasis;
    }

    public void publishPrice(PriceUpdate priceUpdate) {
        session.updatePrice(priceUpdate);
    }

    @Override
    public String getClusterId() {
        return clusterId;
    }

    public void assertTotalBid(double... demandArray) {
        assertArrayEquals(demandArray, lastReceivedBid.getBid().toArrayBid().getDemand(), 0);
    }
}
