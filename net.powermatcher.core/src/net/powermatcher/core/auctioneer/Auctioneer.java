package net.powermatcher.core.auctioneer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import net.powermatcher.api.AgentRole;
import net.powermatcher.api.MatcherRole;
import net.powermatcher.api.Session;
import net.powermatcher.api.TimeService;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.core.BidCache;
import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

@Component
public class Auctioneer implements MatcherRole {

	private MarketBasis marketBasis;
	
	private BidCache aggregatedBids;
	
	private Set<Session> sessions = new HashSet<Session>();
	
	private TimeService timeService;
	
	private ScheduledExecutorService executorService;	
	
	@Override
	public Session connect(AgentRole agentRole) {
		// Create Session
		Session session = new Session(this.marketBasis, UUID.randomUUID().toString(), agentRole, this);
		this.sessions.add(session);

		this.aggregatedBids.updateBid(session.getSessionId(), new Bid(this.marketBasis));
		
		return session;
	}

	@Override
	public void disconnect(Session session) {
		// Find session
		if (!sessions.remove(session)) {
			return;
		}
		
		this.aggregatedBids.removeAgent(session.getSessionId());
	}

	@Override
	public void updateBid(Session session, Bid newBid) {
		// TODO Auto-generated method stub
		if (!sessions.contains(session)) {
			// TODO throw exception
			return;
		}
		
		if (!newBid.getMarketBasis().equals(this.marketBasis)) {
			// TODO throw exception
			return;
		}
		
		// Update agent in aggregatedBids
		this.aggregatedBids.updateBid(session.getSessionId(), newBid);
	}

	@Reference
	public void setTimeService(TimeService timeService) {
		this.timeService = timeService;
	}

	@Reference
	public void setExecutorService(ScheduledExecutorService executorService) {
		this.executorService = executorService;
	}
	
	@Activate
	void activate(final Map<String, Object> properties) {
		// TODO make configurable
		// TODO remove marketref
		// TODO remove significance
		// TODO expirationTime
		// TODO fixed rate configurable
		this.marketBasis = new MarketBasis("ELECTRICITY", "EUR", 10, 0, 100, 2, 0);
		this.aggregatedBids = new BidCache(this.timeService, 176);
		
		this.executorService.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				publishNewPrice();
			}
		}, 5, 5, TimeUnit.SECONDS);
	}
	
	protected void publishNewPrice() {
		Bid aggregatedBid = this.aggregatedBids.getAggregatedBid(this.marketBasis);

		Price newPrice = aggregatedBid.calculateIntersection(0);

		for (Session session : this.sessions) {
			session.getAgentRole().updatePrice(newPrice);
		}
	}
}
