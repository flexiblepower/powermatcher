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
import net.powermatcher.api.monitoring.IncomingPriceEvent;
import net.powermatcher.api.monitoring.OutgoingBidEvent;
import net.powermatcher.api.monitoring.Qualifier;

public abstract class BaseDeviceAgent extends BaseAgent implements AgentEndpoint, Comparable<BaseDeviceAgent> {
	private final AtomicInteger bidNumberGenerator = new AtomicInteger();
	
	private Session session;

	@Override
	public synchronized final void connectToMatcher(Session session) {
		this.session = session;
	}

	@Override
	public synchronized final void matcherEndpointDisconnected(Session session) {
		this.session = null;
	}
	
	public final Session getSession() {
		return session;
	}
	
	protected synchronized final MarketBasis getMarketBasis() {
        if (session == null) {
            return null;
        } else {
            return session.getMarketBasis();
        }
    }
	
	protected synchronized final PointBid createBid(PricePoint... pricePoints) {
		if(session == null) {
			return null;
		} else {
			return new PointBid(session.getMarketBasis(), bidNumberGenerator.incrementAndGet(), pricePoints);
		}
	}

	@Override
	public synchronized void updatePrice(PriceUpdate priceUpdate) {
		publishEvent(new IncomingPriceEvent(getClusterId(), getAgentId(), session.getSessionId(), now(), priceUpdate.getPrice(), Qualifier.AGENT));
	}

	private Bid lastBid;
	
	
	public final Bid getLastBid() {
		return lastBid;
	}
	
	public synchronized final void publishBid(Bid newBid) {
		if(session != null) {
			lastBid = newBid;
			session.updateBid(newBid);
			publishEvent(new OutgoingBidEvent(getClusterId(), getAgentId(), session.getSessionId(), now(), newBid, Qualifier.AGENT));
		}
	}
	
	protected int getCurrentBidNr() {
		return bidNumberGenerator.get();
	}
	
	protected abstract Date now();
	
	@Override
    public int compareTo(BaseDeviceAgent o) {
        return getAgentId().compareTo(o.getAgentId());
    }
}
