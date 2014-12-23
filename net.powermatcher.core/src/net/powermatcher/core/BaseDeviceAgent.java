package net.powermatcher.core;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.PointBid;
import net.powermatcher.api.data.PricePoint;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.api.monitoring.Qualifier;
import net.powermatcher.api.monitoring.events.OutgoingBidEvent;

public abstract class BaseDeviceAgent extends BaseAgent implements AgentEndpoint, Comparable<BaseDeviceAgent> {
    private final AtomicInteger bidNumberGenerator = new AtomicInteger();

    private Bid lastBid;
    private Session session;

    @Override
    public final synchronized void connectToMatcher(Session session) {
        this.session = session;
    }

    @Override
    public final synchronized void matcherEndpointDisconnected(Session session) {
        this.session = null;
    }

    public final Session getSession() {
        return session;
    }

    protected final synchronized MarketBasis getMarketBasis() {
        if (session == null) {
            return null;
        } else {
            return session.getMarketBasis();
        }
    }

    protected final synchronized PointBid createBid(PricePoint... pricePoints) {
        if (session == null) {
            return null;
        } else {
            return new PointBid(session.getMarketBasis(), bidNumberGenerator.incrementAndGet(), pricePoints);
        }
    }

    public abstract void updatePrice(PriceUpdate priceUpdate);

    public final Bid getLastBid() {
        return lastBid;
    }

    public final synchronized void publishBid(Bid newBid) {
        if (session != null) {
            lastBid = newBid;
            session.updateBid(newBid);
            publishEvent(new OutgoingBidEvent(getClusterId(), getAgentId(), session.getSessionId(), now(), newBid,
                    Qualifier.AGENT));
        }
    }

    protected int getCurrentBidNr() {
        return bidNumberGenerator.get();
    }

    protected abstract Date now();

    protected abstract void doBidUpdate();

    @Override
    public int compareTo(BaseDeviceAgent o) {
        return getAgentId().compareTo(o.getAgentId());
    }

    public boolean canEqual(Object other) {
        return other instanceof BaseDeviceAgent;
    }

    @Override
    public boolean equals(Object obj) {
        BaseDeviceAgent other = (BaseDeviceAgent) ((obj instanceof BaseDeviceAgent) ? obj : null);
        if (other == null) {
            return false;
        }

        if (this == other) {
            return true;
        }

        return this.canEqual(other) && super.equals(other) && this.lastBid.equals(other.lastBid);
    }

    @Override
    public int hashCode() {
        return 211 * (super.hashCode() + lastBid.hashCode() + session.hashCode());
    }

}
