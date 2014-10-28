package net.powermatcher.core.agent.framework;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.powermatcher.core.agent.framework.config.AgentConfiguration.LoggingLevel;
import net.powermatcher.core.agent.framework.config.MatcherAgentConfiguration;
import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.agent.framework.log.AbstractLogInfo;
import net.powermatcher.core.agent.framework.log.LogListenerService;
import net.powermatcher.core.agent.framework.service.AgentService;
import net.powermatcher.core.agent.framework.service.MatcherConnectorService;
import net.powermatcher.core.agent.framework.service.MatcherService;
import net.powermatcher.core.configurable.service.ConfigurationService;
import net.powermatcher.core.scheduler.service.TimeService;

/**
 * <p>
 * A matcher is an agent that receives, aggregates bids and processes bids from
 * its connected client agents and returns price info updates and market basis
 * updates.
 * </p>
 * <p>
 * The MatcherAgent class is an abstract class that implements generic matcher
 * functionality for matcher implementations in child classes. The following
 * functionality is implemented:
 * <ul>
 * <li>handling received bid updates</li>
 * <li>publishing market basis updates</li>
 * <li>publishing price info updates</li>
 * </ul>
 * </p>
 * <p>
 * In order to communicate with the child agents, the matcher agent needs to
 * bind with the agent adapter of the child agent. After successful binding the
 * matcher can send price info updates and market basis updates to the child
 * agent via the AgentService interface that is implemented by the child agent
 * adapter.
 * <p>
 * Update bid info messages received from child adapters will be added first to
 * the bid cache (BidCache). Then the bid cache is cleaned up, removing obsolete
 * bids, and then a new aggregated bid is generated. A new aggregated bid is
 * send to the matcher regularly, managed by a configurable update timer.
 * </p>
 * <p>
 * The MatcherAgent class is a child class of Agent, inheriting the AgentService
 * implementation that enables the matcher agent to receive market basis and
 * price info update from matcher agents higher in the cluster hierarchy.
 * </p>
 * <p>
 * Logging is implemented by calling the MatcherLoggingService interface. The
 * agent needs to bind successfully with one or more adapters that implement the
 * MatcherLoggingService before logging can take place.
 * </p>
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see MatcherService
 * @see AgentService
 * @see LogListenerService
 * @see BidCache
 */
public abstract class MatcherAgent extends Agent implements MatcherService, MatcherConnectorService {
	/**
	 * Define the agent bid log level (LoggingLevel) field.
	 */
	private LoggingLevel matcherAgentBidLogLevel;
	/**
	 * Define the matcher aggregated bid log level (LoggingLevel) field.
	 */
	private LoggingLevel matcherAggregatedBidLogLevel;

	/**
	 * Define the matcher price log level (LoggingLevel) field.
	 */
	private LoggingLevel matcherPriceLogLevel;
	/**
	 * Container to store the most recent received bids
	 */
	private BidCache bidCache;
	/**
	 * Define the child agent (AgentService) field.
	 */
	private List<AgentService> childAgentAdapters = new ArrayList<AgentService>();
	/**
	 * Define the last published price info (PriceInfo) field.
	 */
	private PriceInfo lastPublishedPriceInfo;

	/**
	 * Constructs an instance of this class.
	 * 
	 * @see #MatcherAgent(ConfigurationService)
	 */
	protected MatcherAgent() {
		super();
	}

	/**
	 * Constructs an instance of this class from the specified properties
	 * parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @see #MatcherAgent()
	 */
	protected MatcherAgent(final ConfigurationService configuration) {
		super(configuration);
	}

	/**
	 * An agent has been added to the aggregation cache
	 * 
	 * @param agentId
	 *            The agent ID (<code>String</code>) parameter.
	 * @param newBidInfo
	 *            The new bid info (<code>BidInfo</code>) parameter.
	 */
	protected void agentAdded(final String agentId, final BidInfo newBidInfo) {
		logInfo("Agent added to cache: " + agentId);
	}

	/**
	 * An agent has been removed from the aggregation cache
	 * 
	 * @param agentId
	 *            The agent ID (<code>String</code>) parameter.
	 */
	protected void agentRemoved(final String agentId) {
		logInfo("Agent removed from cache: " + agentId);
	}

	/**
	 * Bind with the agent adapter of the child agent. After successful binding
	 * the matcher can send price info updates and market basis updates to the
	 * child agent via the AgentService interface that is implemented by the
	 * child agent adapter.
	 * 
	 * @param childAgentAdapter
	 *            The child agent adapter (<code>AgentService</code>) parameter.
	 */
	@Override
	public void bind(final AgentService childAgentAdapter) {
		synchronized (getLock()) {
			this.childAgentAdapters.add(childAgentAdapter);
			MarketBasis marketBasis = getCurrentMarketBasis();
			if (marketBasis != null) {
				childAgentAdapter.updateMarketBasis(marketBasis);
			}
		}
	}

	/**
	 * Bind the time source for obtaining the real or simulated time.
	 * 
	 * @param timeSource
	 *            The time source (<code>TimeService</code>) to bind.
	 * @see #unbind(TimeService)
	 */
	@Override
	public void bind(final TimeService timeSource) {
		super.bind(timeSource);
		this.bidCache.bind(timeSource);
	}

	/**
	 * Remove agents with expired bids from the aggregation cache.
	 */
	private void cleanupCache() {
		Set<String> removedAgents = this.bidCache.cleanup();
		for (String agentId : removedAgents) {
			agentRemoved(agentId);
		}
	}

	/**
	 * Do the periodic bid update. Cleans up the bid cache removing expired bids
	 * and creates and processes a new aggregated bid.
	 * 
	 * @see #handleAggregatedBidUpdate(BidInfo)
	 * @see #publishMarketBasisUpdate(MarketBasis)
	 */
	@Override
	protected void doBidUpdate() {
		cleanupCache();
		BidInfo newAggregatedBid = getAggregatedBid();
		if (newAggregatedBid != null) {
			handleAggregatedBidUpdate(newAggregatedBid);
		}
	}

	/**
	 * Gets the aggregated bid (BidInfo) value.
	 * 
	 * @return The aggregated bid. Null when no aggregated bid is available
	 *         (yet).
	 */
	protected BidInfo getAggregatedBid() {
		MarketBasis marketBasis = getCurrentMarketBasis();
		return this.bidCache.getAggregatedBid(marketBasis);
	}

	/**
	 * Get the list of currently registered child agent adapters.
	 * 
	 * @return List of currently registered child agent adapters
	 */
	protected List<AgentService> getChildAgentAdapters() {
		return this.childAgentAdapters;
	}

	/**
	 * Gets the last published price info (PriceInfo) value.
	 * 
	 * @return The last published price info (<code>PriceInfo</code>) value.
	 * @see #setLastPublishedPriceInfo(PriceInfo)
	 */
	protected PriceInfo getLastPublishedPriceInfo() {
		return this.lastPublishedPriceInfo;
	}

	/**
	 * Gets the matcher (MatcherService) value.
	 * 
	 * @return The matcher (<code>MatcherService</code>) value.
	 */
	@Override
	public MatcherService getMatcher() {
		return this;
	}

	/**
	 * Handler for processing a new aggregated bid. By default only sets last
	 * aggregated bid.
	 * 
	 * @param newBidInfo
	 *            The new bid info (<code>BidInfo</code>) parameter.
	 */
	protected void handleAggregatedBidUpdate(final BidInfo newBidInfo) {
		this.logBidInfo(getId(), AbstractLogInfo.MATCHER_LOG_QUALIFIER, newBidInfo, getLastPublishedPriceInfo(), this.matcherAggregatedBidLogLevel);
	}

	/**
	 * Initialize.
	 */
	private void initialize() {
		int updateInterval = getProperty(MatcherAgentConfiguration.BID_EXPIRATION_TIME_PROPERTY,
				MatcherAgentConfiguration.BID_EXPIRATION_TIME_DEFAULT);
		this.bidCache = new BidCache(updateInterval);
		this.matcherAgentBidLogLevel = LoggingLevel.valueOf(getProperty(
				MatcherAgentConfiguration.MATCHER_AGENT_BID_LOG_LEVEL_PROPERTY,
				MatcherAgentConfiguration.MATCHER_AGENT_BID_LOG_LEVEL_DEFAULT));
		this.matcherAggregatedBidLogLevel = LoggingLevel.valueOf(getProperty(
				MatcherAgentConfiguration.MATCHER_AGGREGATED_BID_LOG_LEVEL_PROPERTY,
				MatcherAgentConfiguration.MATCHER_AGGREGATED_BID_LOG_LEVEL_DEFAULT));
		this.matcherPriceLogLevel = LoggingLevel.valueOf(getProperty(
				MatcherAgentConfiguration.MATCHER_PRICE_LOG_LEVEL_PROPERTY,
				MatcherAgentConfiguration.MATCHER_PRICE_LOG_LEVEL_DEFAULT));
	}

	/**
	 * Determine if the new aggregated bid has changed significantly compared to
	 * the last bid. If the aggregated bid has changed significantly compared to
	 * the last bid sent, the new aggregated bid will be sent to the parent
	 * matcher immediately. Currently, true is returned only when no bid was
	 * sent to the parent matcher before, or the update loop has been disabled.
	 * 
	 * @param newAggregatedBid
	 *            The new aggregated bid (<code>BidInfo</code>) parameter.
	 * @return True if the new aggregated bid should be sent to the parent
	 *         matcher immediately.
	 */
	protected boolean isAggregatedBidChangeSignificant(final BidInfo newAggregatedBid) {
		return getUpdateInterval() == 0;
	}

	/**
	 * Publish market basis update with the specified new market basis
	 * parameter.
	 * 
	 * @param newMarketBasis
	 *            The new market basis (<code>MarketBasis</code>) parameter.
	 */
	public void publishMarketBasisUpdate(final MarketBasis newMarketBasis) {
		synchronized (getLock()) {
			if (newMarketBasis != null) {
				for (AgentService childAgentAdapter : getChildAgentAdapters()) {
					childAgentAdapter.updateMarketBasis(newMarketBasis);
				}
			}
		}
	}

	/**
	 * Publish price info with the specified new price info parameter.
	 * 
	 * @param newPriceInfo
	 *            The new price info (<code>PriceInfo</code>) parameter.
	 * @see #getLastPublishedPriceInfo()
	 * @see #setLastPublishedPriceInfo(PriceInfo)
	 */
	public void publishPriceInfo(final PriceInfo newPriceInfo) {
		synchronized (getLock()) {
			setLastPublishedPriceInfo(newPriceInfo);
			if (newPriceInfo != null) {
				logPriceInfo(AbstractLogInfo.MATCHER_LOG_QUALIFIER, newPriceInfo, this.matcherPriceLogLevel);
				for (AgentService childAgentAdapter : getChildAgentAdapters()) {
					childAgentAdapter.updatePriceInfo(newPriceInfo);
				}
			}
		}
	}

	/**
	 * Sets the configuration value.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 */
	@Override
	public void setConfiguration(final ConfigurationService configuration) {
		super.setConfiguration(configuration);
		initialize();
	}

	/**
	 * Sets the last published price info value.
	 * 
	 * @param newPriceInfo
	 *            The new price info (<code>PriceInfo</code>) parameter.
	 * @see #getLastPublishedPriceInfo()
	 */
	protected void setLastPublishedPriceInfo(final PriceInfo newPriceInfo) {
		this.lastPublishedPriceInfo = newPriceInfo;
	}

	/**
	 * Unbind. Removes the agent adapter that belongs to the client agent that
	 * is connected to the matcher.
	 * 
	 * @param childAgentAdapter
	 *            The child agent adapter (<code>AgentService</code>) parameter.
	 */
	@Override
	public void unbind(final AgentService childAgentAdapter) {
		synchronized (getLock()) {
			assert this.childAgentAdapters.contains(childAgentAdapter);
			this.childAgentAdapters.remove(childAgentAdapter);
		}
	}

	/**
	 * Unbind the time source for obtaining the real or simulated time.
	 * 
	 * @param timeSource
	 *            The time source (<code>TimeService</code>) to unbind.
	 * 
	 * @see #bind(TimeService)
	 */
	@Override
	public void unbind(final TimeService timeSource) {
		this.bidCache.unbind(timeSource);
		super.unbind(timeSource);
	}

	/**
	 * Update bid info with the specified agent ID and new bid parameters.
	 * Process the updated bid by updating the bid cache (i.e. add it to the
	 * cache). Then the cache is cleaned up by removing the expired bids and a
	 * new aggregated bid is created and processed.
	 * 
	 * @param agentId
	 *            The agent ID (<code>String</code>) parameter.
	 * @param newBidInfo
	 *            The new bid info (<code>BidInfo</code>) parameter.
	 */
	@Override
	public void updateBidInfo(final String agentId, final BidInfo newBidInfo) {
		if (newBidInfo == null) {
			throw new IllegalArgumentException("Bid cannot be null");
		}
		if (isInfoEnabled()) {
			logInfo("Aggregate new bid received from " + agentId + ": " + newBidInfo);
		}
		/*
		 * Add to or update in the bid cache and get the updated aggregated bid
		 * from cache
		 */
		if (this.bidCache.updateBid(agentId, newBidInfo) == null) {
			agentAdded(agentId, newBidInfo);
		}
		this.logBidInfo(agentId, AbstractLogInfo.AGENT_LOG_QUALIFIER, newBidInfo, getLastPublishedPriceInfo(), this.matcherAgentBidLogLevel);
		if (getUpdateInterval() == 0) {
			cleanupCache();
		}
		BidInfo newAggregatedBid = getAggregatedBid();
		if (newAggregatedBid != null && isAggregatedBidChangeSignificant(newAggregatedBid)) {
			if (isInfoEnabled()) {
				logInfo("Aggregated bid change is significant, handle immediately");
			}
			handleAggregatedBidUpdate(newAggregatedBid);
		}
	}

}
