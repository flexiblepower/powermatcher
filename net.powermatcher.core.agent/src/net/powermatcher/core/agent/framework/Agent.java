package net.powermatcher.core.agent.framework;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.powermatcher.core.agent.framework.config.AgentConfiguration;
import net.powermatcher.core.agent.framework.config.AgentConfiguration.LoggingLevel;
import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.agent.framework.log.BidLogInfo;
import net.powermatcher.core.agent.framework.log.LogListenerService;
import net.powermatcher.core.agent.framework.log.LoggingConnectorService;
import net.powermatcher.core.agent.framework.log.PriceLogInfo;
import net.powermatcher.core.agent.framework.service.AgentConnectorService;
import net.powermatcher.core.agent.framework.service.AgentService;
import net.powermatcher.core.agent.framework.service.MatcherService;
import net.powermatcher.core.agent.framework.task.BidUpdateTask;
import net.powermatcher.core.configurable.service.ConfigurationService;
import net.powermatcher.core.object.ActiveObject;


/**
 * <p>
 * In a multi-agent system based on the PowerMatcher technology the parent class
 * of each agent is the Agent class.  The base components of a PowerMatcher cluster, 
 * the Auctioneer, Concentrator and Objective agent are all subclasses of the Agent class.
 * Device agents are also implemented by creating a subclass of the Agent class.
 * </p>
 * <p>
 * The Agent class provides the base functionality for an agent. It contains methods that
 * regularly update the agent&#39s status and that handle the market price and the market basis updates from
 * the parent matcher. To implement the specific behavior of the device agent these methods
 * can be overridden.
 * </p> 
 * <p>
 * <H3>Matcher Service interface</H3>
 * New bids are send to the parent matcher via the parent matcher adapter. This adapter implements the
 * MatcherService interface. The adapter is linked to the Agent using the <code>bind(MatcherService)</code> method.
 * Matcher agents like the Auctioneer and the Concentrator implement the MatcherService interface and can be
 * directly linked enabling synchronous transmission of the updated bid to the matcher. In a multi-agent 
 * system the agents will likely be distributed communicating via a network. This will require an adapter 
 * that implements the MatcherService using the specific protocol.
 * </p>
 * <p>
 * <H3>Agent Logging Service interface</H3>
 * An agent logs events through the logging adapter which implements the AgentLoggingService interface.
 * Events that are logged are the publication of new bids and updated price info.
 * </p>
 * <p>
 * <H3>Creating a device agent</H3>
 * A typical scenario of creating a PowerMatcher device agent is to create a subclass of class Agent and
 * to override the <code>doUpdate()</code> and the <code>updatePriceInfo(...)</code> methods. In the first you specify the new bid 
 * update based on the current status of your device. In the latter you implement the device agent#39s reaction
 * on an updated market price. Preferably you should also override the <code>updateMarketBasis(...)</code> method where
 * you define what should happen when the market basis is updated. 
 * </p>
 * @author IBM
 * @version 0.9.0
 * 
 * @see AgentService
 * @see MatcherService
 * @see LogListenerService
 * @see ConfigurationService
 * @see MarketBasis
 * @see PriceInfo
 * @see BidInfo
 */
public abstract class Agent extends ActiveObject implements AgentService, AgentConnectorService, LoggingConnectorService {
	/**
	 * Define the agent bid log level (LoggingLevel) field.
	 */
	private LoggingLevel agentBidLogLevel;
	/**
	 * Define the agent price log level (LoggingLevel) field.
	 */
	private LoggingLevel agentPriceLogLevel;
	/**
	 * Define the parent matcher adapters (MatcherService) field.
	 */
	private List<MatcherService> parentMatcherAdapters = new ArrayList<MatcherService>();
	/**
	 * Define the last market basis (MarketBasis) field.
	 */
	private MarketBasis currentMarketBasis;
	/**
	 * Define the last price info (PriceInfo) field.
	 */
	private PriceInfo lastPriceInfo;
	/**
	 * Define the last bid (BidInfo) field.
	 */
	private BidInfo lastBid;
	/**
	 * Define the bid number (int) field.
	 */
	private int bidNumber;
	/**
	 * Define the logging adapter (LogListenerService) field.
	 */
	private LogListenerService loggingAdapter;

	/**
	 * Define the object (Object) that can be used for internal critical
	 * sections.
	 */
	private Object lock = new Object();

	/**
	 * Define the future (ScheduledFuture) to control the bid update task.
	 */
	private ScheduledFuture<?> bidUpdateFuture;

	/**
	 * Constructs an instance of this class.
	 * 
	 * @see #Agent(ConfigurationService)
	 * @see #getAgent()
	 * @see #setChildAgent(AgentService)
	 */
	protected Agent() {
		super();
	}

	/**
	 * Constructs an instance of this class from the specified configuration
	 * parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @see #Agent()
	 * @see #getAgent()
	 * @see #setChildAgent(AgentService)
	 */
	protected Agent(final ConfigurationService configuration) {
		super(configuration);
	}

	/**
	 * Bind with the specified logging adapter parameter.
	 * 
	 * @param loggingAdapter
	 *            The  logging adapter (<code>LogListenerService</code>)
	 *            parameter.
	 * @see #bind(MatcherService)
	 */
	@Override
	public void bind(final LogListenerService loggingAdapter) {
		assert this.loggingAdapter == null;
		this.loggingAdapter = loggingAdapter;
	}

	/**
	 * Bind with the specified parent matcher adapter(<code>MatcherService</code>).
	 * Through this adapter update bids (<code>BidInfo</code>) will be sent to
	 * and price info updates (<code>PriceInfo</code>) and market basis updates 
	 * (<code>MarketBasis</code>) will be received from the parent matcher.
	 * 
	 * @param parentMatcherAdapter
	 *            The parent matcher adapter (<code>MatcherService</code>)
	 *            parameter.
	 */
	@Override
	public void bind(final MatcherService parentMatcherAdapter) {
		synchronized (getLock()) {
			this.parentMatcherAdapters.add(parentMatcherAdapter);
		}
	}

	/**
	 * Gets the agent (AgentService) value.
	 * 
	 * @return The agent (<code>AgentService</code>) value.
	 * @see #Agent()
	 * @see #Agent(ConfigurationService)
	 * @see #setChildAgent(AgentService)
	 */
	@Override
	public AgentService getAgent() {
		return this;
	}

	/**
	 * Gets the current market basis (MarketBasis) that was
	 * received from the parent matcher.
	 * 
	 * @return The current market basis (<code>MarketBasis</code>) value.
	 * @see #setCurrentMarketBasis(MarketBasis)
	 */
	protected MarketBasis getCurrentMarketBasis() {
		return this.currentMarketBasis;
	}

	/**
	 * Gets the last bid (BidInfo) that was published.
	 * 
	 * @return The last bid (<code>BidInfo</code>) value.
	 * @see #setLastBid(BidInfo)
	 */
	protected BidInfo getLastBid() {
		return this.lastBid;
	}

	/**
	 * Gets the last price info (PriceInfo) that was received
	 * from the parent matcher.
	 * 
	 * @return The last price info (<code>PriceInfo</code>) value.
	 * @see #setLastPriceInfo(PriceInfo)
	 */
	protected PriceInfo getLastPriceInfo() {
		return this.lastPriceInfo;
	}

	/**
	 * Return The generic object that can be used for internal critical
	 * sections.
	 * 
	 * @return The generic lock object.
	 */
	protected Object getLock() {
		return this.lock;
	}

	/**
	 * Get the list of currently registered parent matcher adapters.
	 * 
	 * @return List of currently registered parent matcher adapters
	 */
	protected List<MatcherService> getParentMatcherAdapters() {
		return this.parentMatcherAdapters;
	}

	/**
	 * Initialize.
	 */
	private void initialize() {
		this.agentBidLogLevel = LoggingLevel.valueOf(getProperty(
				AgentConfiguration.AGENT_BID_LOG_LEVEL_PROPERTY,
				AgentConfiguration.AGENT_BID_LOG_LEVEL_DEFAULT));
		this.agentPriceLogLevel = LoggingLevel.valueOf(getProperty(
				AgentConfiguration.AGENT_PRICE_LOG_LEVEL_PROPERTY,
				AgentConfiguration.AGENT_PRICE_LOG_LEVEL_DEFAULT));
	}

	/**
	 * Log bid info with the specified agent ID, qualifier, bid info and level
	 * parameters.
	 * 
	 * @param agentId
	 *            The agent ID (<code>String</code>) parameter.
	 * @param qualifier
	 *            The qualifier (<code>String</code>) parameter.
	 * @param bidInfo
	 *            The bid info (<code>BidInfo</code>) parameter.
	 * @param priceInfo
	 *            The effective agent or matcher price (<code>PriceInfo</code>) parameter.
	 * @param level
	 *            The level (<code>LoggingLevel</code>) parameter.
	 */
	protected void logBidInfo(final String agentId, final String qualifier, final BidInfo bidInfo, final PriceInfo priceInfo,
			final LoggingLevel level) {
		if (level != LoggingLevel.NO_LOGGING && this.loggingAdapter != null) {
			Date now = new Date(getCurrentTimeMillis());
			double effectivePrice = priceInfo == null ? 0 : priceInfo.getCurrentPrice();
			double effectiveDemand = bidInfo.getDemand(effectivePrice);
			double maximumDemand = bidInfo.getMaximumDemand();
			double minimumDemand = bidInfo.getMinimumDemand();
			BidInfo loggedBidInfo = (level == LoggingLevel.FULL_LOGGING) ? bidInfo : null;
			BidLogInfo bidLogInfo = new BidLogInfo(getClusterId(), agentId, qualifier, now, bidInfo.getMarketBasis(),
					effectivePrice, effectiveDemand, minimumDemand, maximumDemand, loggedBidInfo);
			this.loggingAdapter.handleBidLogInfo(bidLogInfo);
		}
	}

	/**
	 * Log price info with the specified qualifier, price info and level
	 * parameters.
	 * 
	 * @param qualifier
	 *            The qualifier (<code>String</code>) parameter.
	 * @param priceInfo
	 *            The price info (<code>PriceInfo</code>) parameter.
	 * @param level
	 *            The level (<code>LoggingLevel</code>) parameter.
	 */
	protected void logPriceInfo(final String qualifier, final PriceInfo priceInfo,
			final LoggingLevel level) {
		if (level != LoggingLevel.NO_LOGGING && this.loggingAdapter != null) {
			Date now = new Date(getCurrentTimeMillis());
			PriceLogInfo priceLogInfo = new PriceLogInfo(getClusterId(), getId(), qualifier, now, priceInfo);
			this.loggingAdapter.handlePriceLogInfo(priceLogInfo);
		}
	}

	/**
	 * Publish bid update with the specified new bid parameter. The method
	 * records the time stamp of the last bid update and logs the bid publication
	 * if the agent logging adapter is set.
	 * 
	 * @param newBidInfo
	 *            The new bid info (<code>BidInfo</code>) parameter.
	 * @return The new bid info updated with the next bid number.
	 */
	protected BidInfo publishBidUpdate(final BidInfo newBidInfo) {
		if (newBidInfo != null) {
			BidInfo updatedBidInfo = new BidInfo(newBidInfo, this.bidNumber++);
			logBidInfo(getId(), BidLogInfo.AGENT_LOG_QUALIFIER, updatedBidInfo, getLastPriceInfo(), this.agentBidLogLevel);
			synchronized (getLock()) {
				setLastBid(updatedBidInfo);
				for (MatcherService parentMatcherAdapter : getParentMatcherAdapters()) {
					parentMatcherAdapter.updateBidInfo(getId(), updatedBidInfo);
				}
			}
			return updatedBidInfo;
		}
		return null;
	}

	/**
	 * Sets the child agent value.
	 * 
	 * @param childAgent
	 *            The child agent (<code>AgentService</code>) parameter.
	 */
	protected void setChildAgent(final AgentService childAgent) {
		/* do nothing */
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
	 * Sets the current market basis value.
	 * 
	 * @param marketBasis
	 *            The market basis (<code>MarketBasis</code>) parameter.
	 * @see #getCurrentMarketBasis()
	 */
	protected void setCurrentMarketBasis(final MarketBasis marketBasis) {
		this.currentMarketBasis = marketBasis;
	}

	/**
	 * Sets the last bid value.
	 * 
	 * @param newBidInfo
	 *            The new bid info (<code>BidInfo</code>) parameter.
	 * @see #getLastBid()
	 */
	protected void setLastBid(final BidInfo newBidInfo) {
		this.lastBid = newBidInfo;
	}

	/**
	 * Sets the last price info value.
	 * 
	 * @param newPriceInfo
	 *            The new price info (<code>PriceInfo</code>) parameter.
	 * @see #getLastPriceInfo()
	 */
	protected void setLastPriceInfo(final PriceInfo newPriceInfo) {
		this.lastPriceInfo = newPriceInfo;
	}

	/**
	 * Unbind with the specified logging adapter parameter.
	 * 
	 * @param loggingAdapter
	 *            The logging adapter (<code>LogListenerService</code>)
	 *            parameter.
	 * @see #unbind(MatcherService)
	 */
	@Override
	public void unbind(final LogListenerService loggingAdapter) {
		this.loggingAdapter = null;
	}

	/**
	 * Stops the current update timer and unbinds with the current parent
	 * matcher adapter (<code>MatcherService</code>).
	 * 
	 * @param parentMatcherAdapter
	 *            The parent matcher adapter (<code>MatcherService</code>)
	 *            parameter.
	 */
	@Override
	public void unbind(final MatcherService parentMatcherAdapter) {
		synchronized (getLock()) {
			assert this.parentMatcherAdapters.contains(parentMatcherAdapter);
			this.parentMatcherAdapters.remove(parentMatcherAdapter);
		}
	}

	/**
	 * Update market basis with the specified new market basis parameter.
	 * 
	 * @param newMarketBasis
	 *            The new market basis (<code>MarketBasis</code>) parameter.
	 * @see #getCurrentMarketBasis()
	 * @see #setCurrentMarketBasis(MarketBasis)
	 */
	@Override
	public void updateMarketBasis(final MarketBasis newMarketBasis) {
		if (newMarketBasis == null) {
			throw new IllegalArgumentException("Market basis cannot be null");
		}
		setCurrentMarketBasis(newMarketBasis);
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
	public void updatePriceInfo(final PriceInfo newPriceInfo) {
		if (newPriceInfo == null) {
			throw new IllegalArgumentException("Price cannot be null");
		}
		this.logPriceInfo(PriceLogInfo.AGENT_LOG_QUALIFIER, newPriceInfo, this.agentPriceLogLevel);
		MarketBasis newMarketBasis = newPriceInfo.getMarketBasis();
		if (newMarketBasis != getCurrentMarketBasis()) {
			setCurrentMarketBasis(newMarketBasis);
		}
		setLastPriceInfo(newPriceInfo);
	}

	/**
	 * Start the periodic bid update task of the agent.
	 * This method will be called when the scheduler is bound to the active object.
	 */
	@Override
	protected void startPeriodicTasks() {
		BidUpdateTask task = new BidUpdateTask() {

			@Override
			public void run() {
				try {
					doBidUpdate();
				} catch (Throwable t) {
					logError("Bid update failed", t);
				}
			}
			
		};
		this.bidUpdateFuture = getScheduler().scheduleAtFixedRate(task, 0, getUpdateInterval(), TimeUnit.SECONDS);
	}

	/**
	 * Do the periodic bid update. This method is intended for updating the agents status
	 * and publish a new bid reflecting that status. It is periodically invoked
	 * by the bid update task.
	 * A device agent must implement this method to determine the agent#39s update
	 * status and define the update bid based on the new status. 
	 *  
	 * @see #publishBidUpdate(BidInfo)
	 */
	protected abstract void doBidUpdate();

	/**
	 * Stop the periodic bid update task of the agent.
	 * This method will be called when the scheduler is unbound from the active object.
	 */
	@Override
	protected void stopPeriodicTasks() {
		this.bidUpdateFuture.cancel(false);
	}

}
