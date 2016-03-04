package net.powermatcher.mock;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.messages.BidUpdate;
import net.powermatcher.api.messages.PriceUpdate;

/**
 *
 * @author FAN
 * @version 2.1
 */
public class MockDeviceAgent
    extends MockObservableAgent
    implements AgentEndpoint {

    private final String desiredParentId;
    protected Session session;
    private PriceUpdate lastPriceUpdate;

    public MockDeviceAgent(String agentId, String desiredParentId) {
        super(agentId);
        this.desiredParentId = desiredParentId;
    }

    @Override
    public String getDesiredParentId() {
        return desiredParentId;
    }

    @Override
    public AgentEndpoint.Status getStatus() {
        final Session session = this.session;
        return new AgentEndpoint.Status() {
            @Override
            public boolean isConnected() {
                return session != null;
            }

            @Override
            public MarketBasis getMarketBasis() {
                return session.getMarketBasis();
            }

            @Override
            public String getClusterId() {
                return session.getClusterId();
            }

            @Override
            public Session getSession() {
                return session;
            }
        };
    }

    @Override
    public void connectToMatcher(Session session) {
        this.session = session;
    }

    @Override
    public void matcherEndpointDisconnected(Session session) {
        this.session = null;
    }

    @Override
    public void handlePriceUpdate(PriceUpdate priceUpdate) {
        lastPriceUpdate = priceUpdate;
    }

    public void sendBid(Bid bid, int bidNumber) {
        session.updateBid(new BidUpdate(bid, bidNumber));
    }

    public void sendBid(BidUpdate bidUpdate) {
        session.updateBid(bidUpdate);
    }

    /**
     * @return the current value of session.
     */
    public Session getSession() {
        return session;
    }

    /**
     * @return the current value of lastPriceUpdate.
     */
    public PriceUpdate getLastPriceUpdate() {
        return lastPriceUpdate;
    }
}
