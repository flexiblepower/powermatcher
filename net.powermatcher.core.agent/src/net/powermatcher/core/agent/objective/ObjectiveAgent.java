package net.powermatcher.core.agent.objective;


import net.powermatcher.core.agent.framework.MatcherAgent;
import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.agent.framework.data.PricePoint;
import net.powermatcher.core.agent.objective.config.ObjectiveAgentConfiguration;
import net.powermatcher.core.agent.objective.service.ObjectiveConnectorService;
import net.powermatcher.core.agent.objective.service.ObjectiveControlService;
import net.powermatcher.core.agent.objective.service.ObjectiveNotificationService;
import net.powermatcher.core.configurable.service.ConfigurationService;


/**
 * @author IBM
 * @version 0.9.0
 */
public class ObjectiveAgent extends MatcherAgent implements ObjectiveConnectorService, ObjectiveControlService {
	/**
	 * Define the price points (PricePoint[]) field.
	 */
	private PricePoint[] pricePoints;

	/**
	 * Define the notification service (ObjectiveNotificationService) field.
	 */
	private ObjectiveNotificationService notificationService;

	/**
	 * Define the objective bid (BidInfo) field.
	 */
	private BidInfo objectiveBid;

	/**
	 * Define the update already published (boolean) field.
	 * Set to true when an updated bid has already been published from setObjectiveBid
	 * to avoid duplicate publishing of the same bid from doBidUpdate.
	 */
	private boolean updateAlreadyPublished;

	/**
	 * Constructs an instance of this class.
	 * 
	 * @see #ObjectiveAgent(ConfigurationService)
	 */
	public ObjectiveAgent() {
		super();
	}

	/**
	 * Constructs an instance of this class from the specified properties
	 * parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @see #ObjectiveAgent()
	 */
	public ObjectiveAgent(final ConfigurationService configuration) {
		super(configuration);
	}

	/**
	 * Bind the objective adapter to the objective agent.
	 * 
	 * @param notificationService
	 *            The notification service (
	 *            <code>ObjectiveNotificationService</code>) the objective agent
	 *            should use.
	 */
	@Override
	public void bind(final ObjectiveNotificationService notificationService) {
		assert this.notificationService == null;
		this.notificationService = notificationService;
	}

	/**
	 * Do the periodic bid update.
	 * 
	 * @see #handleAggregatedBidUpdate(BidInfo)
	 */
	@Override
	protected void doBidUpdate() {
		super.doBidUpdate();
		if (this.updateAlreadyPublished) {
			this.updateAlreadyPublished = false;
		} else {
			BidInfo newBidInfo = getObjectiveOrDefaultBid();
			publishBidUpdate(newBidInfo);
		}
	}

	/**
	 * Gets the aggregated bid (BidInfo) value.
	 * 
	 * @return The aggregated bid. Null when no aggregated bid is available
	 *         (yet).
	 */
	@Override
	public BidInfo getAggregatedBid() {
		return super.getAggregatedBid();
	}

	/**
	 * Gets the bid (BidInfo) value.
	 * 
	 * @return The bid (<code>BidInfo</code>) value.
	 */
	protected BidInfo getDefaultBid() {
		MarketBasis marketBasis = getCurrentMarketBasis();
		if (marketBasis != null) {
			return new BidInfo(marketBasis, getPricePoints());
		}
		return null;
	}

	/**
	 * Gets the last price info (PriceInfo) value.
	 * 
	 * @return The last price info (<code>PriceInfo</code>) value.
	 * @see #setLastPriceInfo(PriceInfo)
	 */
	@Override
	public PriceInfo getLastPriceInfo() {
		return super.getLastPriceInfo();
	}

	/**
	 * Get the bid that the objective agent is currently submitting to the
	 * cluster.
	 * 
	 * @return The objective bid to submit to the cluster.
	 */
	@Override
	public BidInfo getObjectiveBid() {
		return this.objectiveBid;
	}

	@Override
	public ObjectiveControlService getObjectiveControlService() {
		return this;
	}

	/**
	 * Get the objective bid set by the adapter, or the default bid if not
	 * controlled by an adapter.
	 * 
	 * @return The objective bid, or the default bid if object bid is not set.
	 */
	private BidInfo getObjectiveOrDefaultBid() {
		BidInfo newBidInfo = this.objectiveBid;
		if (newBidInfo == null) {
			newBidInfo = getDefaultBid();
		}
		return newBidInfo;
	}

	/**
	 * Gets the price points (PricePoint[]) value.
	 * 
	 * @return The price points (<code>PricePoint[]</code>) value.
	 */
	protected PricePoint[] getPricePoints() {
		return this.pricePoints;
	}

	/**
	 * Handler for processing a new aggregated bid. By default does nothing.
	 * 
	 * @param newBidInfo
	 *            The new bid info (<code>BidInfo</code>) parameter.
	 */
	@Override
	protected void handleAggregatedBidUpdate(final BidInfo newBidInfo) {
		ObjectiveNotificationService notificationService = this.notificationService;
		if (notificationService != null) {
			notificationService.handleAggregatedBidUpdate(newBidInfo);
		}
	}

	/**
	 * Initialize.
	 */
	private void initialize() {
		String[] points = getProperty(ObjectiveAgentConfiguration.BID_PROPERTY,
				ObjectiveAgentConfiguration.BID_PROPERTY_DELIMITER, ObjectiveAgentConfiguration.BID_PROPERTY_DEFAULT);
		this.pricePoints = new PricePoint[points.length];
		for (int i = 0; i < points.length; i++) {
			this.pricePoints[i] = new PricePoint(points[i]);
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
	 * Set the bid that the objective agent will submit to the cluster. If the
	 * new bid not the same as the last bid, it is published immediately.
	 * 
	 * @param objectiveBid
	 *            The objective bid to submit to the cluster, or null to submit
	 *            the configured default bid.
	 */
	@Override
	public void setObjectiveBid(final BidInfo objectiveBid) {
		this.objectiveBid = objectiveBid;
		BidInfo newBidInfo = getObjectiveOrDefaultBid();
		BidInfo lastBid = getLastBid();
		if (lastBid == null || !new BidInfo(lastBid, newBidInfo.getBidNumber()).equals(newBidInfo)) {
			publishBidUpdate(newBidInfo);
			this.updateAlreadyPublished = true;
		}
	}

	/**
	 * Unbind the objective adapter from the objective agent.
	 * 
	 * @param notificationService
	 *            The notification service (
	 *            <code>ObjectiveNotificationService</code>) to be unbound.
	 */
	@Override
	public void unbind(final ObjectiveNotificationService notificationService) {
		this.notificationService = null;
		this.objectiveBid = null;
	}

	/**
	 * Update price info with the specified new price info parameter.
	 * A typical device agent implementation will override this method and
	 * specify the agent#39s reaction upon an updated market price.
	 * 
	 * @param newPriceInfo
	 *            The new price info (<code>PriceInfo</code>) parameter.
	 * @see #getLastPriceInfo()
	 * @see #setLastPriceInfo(PriceInfo)
	 */
	@Override
	public void updatePriceInfo(PriceInfo newPriceInfo) {
		super.updatePriceInfo(newPriceInfo);
		if (notificationService != null) {
			notificationService.handlePriceUpdate(newPriceInfo);
		}
	}

	/**
	 * Update bid info with the specified agent ID and new bid parameters.
	 * Process the updated bid by updating the bid cache (i.e. add it to the
	 * cache). Then the cache is cleaned up by removing the expired bids and a
	 * new aggregated bid is created and processed.
	 * The objective agent always aggregates it's own bid immediately in publishBidUpdate,
	 * The objective agent's own bid is ignored when it is received here.
	 * 
	 * @param agentId
	 *            The agent ID (<code>String</code>) parameter.
	 * @param newBidInfo
	 *            The new bid info (<code>BidInfo</code>) parameter.
	 * @see #publishBidUpdate(BidInfo)
	 */
	@Override
	public void updateBidInfo(String agentId, BidInfo newBidInfo) {
		if (!agentId.equals(getId())) {
			super.updateBidInfo(agentId, newBidInfo);
		}
	}

	/**
	 * Update market basis with the specified new market basis parameter.
	 * The updated market basis must be published as the adapter needs to
	 * know.
	 * 
	 * @param newMarketBasis
	 *            The new market basis (<code>MarketBasis</code>) parameter.
	 */
	@Override
	public void updateMarketBasis(final MarketBasis newMarketBasis) {
		super.updateMarketBasis(newMarketBasis);
		publishMarketBasisUpdate(newMarketBasis);
	}

	/**
	 * Update bid info with the new bid parameter.
	 * Process the updated bid by updating the bid cache (i.e. add it to the
	 * cache). Then the cache is cleaned up by removing the expired bids and a
	 * new aggregated bid is created and processed.
	 * The objective agent always aggregates it's own bid immediately.
	 * The objective agent's own bid is ignored when it is received in this.updateBidInfo.
	 * 
	 * @param newBidInfo
	 *            The new bid info (<code>BidInfo</code>) parameter.
	 * @see #updateBidInfo(String, BidInfo)
	 */
	@Override
	protected BidInfo publishBidUpdate(BidInfo newBidInfo) {
		if (newBidInfo != null) {
			BidInfo publishedBid = super.publishBidUpdate(newBidInfo);
			super.updateBidInfo(getId(), newBidInfo);
			return publishedBid; 
		}
		return null;
	}

}
