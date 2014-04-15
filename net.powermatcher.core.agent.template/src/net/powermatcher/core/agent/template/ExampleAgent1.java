package net.powermatcher.core.agent.template;


import net.powermatcher.core.agent.framework.Agent;
import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.agent.framework.data.PricePoint;
import net.powermatcher.core.agent.template.config.ExampleAgent1Configuration;
import net.powermatcher.core.configurable.service.ConfigurationService;

/**
 * This class implements a PowerMatcher agent that publishes a step-shaped bid for a configurable demand and price value.
 * The same bid is published repeatedly at the configured update interval until the configuration is changed.
 * <ul>
 * <li>If the configured demand is positive, it means that the distributed energy resource will 
 * use the configured demand if the price is less than the configuration bid price, and 0 otherwise</li> 
 * <li>If the configured demand is negative, it means that the distributed energy resource will 
 * supply at the configured demand if the price is greater than or equal to the configuration bid price.</li> 
 * </ul>
 * <code>ExampleAgent1</code> runs in isolation, without interfacing with the outside world.
 * <code>ExampleAgent2</code> extends this agent with interfaces.  
 *   
 * @see ExampleAgent1Configuration
 * @see ExampleAgent2
 *  
 * @author IBM
 * @version 0.9.0
 */
public class ExampleAgent1 extends Agent {
	/**
	 * Define the bid step price (int) field.
	 * The bid price is a configuration property for the agent.
	 * 
	 * @see ExampleAgent1Configuration#BID_PRICE_PROPERTY
	 */
	private double bidPrice;
	/**
	 * Define the bid power (double) field.
	 * The bid power is a configuration property for the agent. 
	 * 
	 * @see ExampleAgent1Configuration#BID_POWER_PROPERTY
	 */
	private double bidPower;

	/**
	 * Constructs an unconfigured instance of this class.
	 * 
	 * @see #ExampleAgent1(ConfigurationService)
	 * @see #setConfiguration(ConfigurationService)
	 */
	public ExampleAgent1() {
		super();
	}

	/**
	 * Constructs a configured instance of this class from the specified configuration
	 * parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @see #ExampleAgent1()
	 */
	public ExampleAgent1(final ConfigurationService configuration) {
		super(configuration);
	}

	/**
	 * Do the periodic bid update. This method is intended for updating the agents status
	 * and publish a new bid reflecting that status. It is periodically invoked
	 * by the framework at the configured update interval. Even if the status did not change,
	 * the last bid should be republished to maintain the registration of
	 * this agent with the matcher.
	 * 
	 */
	@Override
	protected void doBidUpdate() {
		/*
		 * Get the current market basis, which is the last market basis received by this
		 * agent. The current market basis may be null if the agent has not yet
		 * received a market basis from its matcher. 
		 */
		MarketBasis marketBasis = getCurrentMarketBasis();
		if (marketBasis != null) {

			/*
			 * Convert the configured bid price to normalized price units (npu).
			 * Normalize price unit 0 is the price step that corresponds to price 0.0.
			 */
			int normalizedPrice = marketBasis.toNormalizedPrice(this.bidPrice);
			normalizedPrice = marketBasis.boundNormalizedPrice(normalizedPrice);

			/*
			 * Construct a step-shaped bid that consist of 2 price points.
			 * <ul>
			 * <li>If the configured demand is positive, the points are {(demand,npu);(0.0,npu)}.</li>
			 * <li>If the configured demand is negative, the points are {(0.0,npu);(demand,npu)}.</li>
			 * </ul>
			 */
			PricePoint pricePoint1;
			PricePoint pricePoint2;

			if (this.bidPower >= 0) {
				pricePoint1 = new PricePoint(normalizedPrice, this.bidPower);
				pricePoint2 = new PricePoint(normalizedPrice, 0);
			} else {
				pricePoint1 = new PricePoint(normalizedPrice, 0);
				pricePoint2 = new PricePoint(normalizedPrice, this.bidPower);
			}

			BidInfo newBidInfo = new BidInfo(marketBasis, pricePoint1, pricePoint2);
			
			/*
			 * Publish the new bid to the matcher.
			 * If no matcher is connected to the agent, the bid is silently discarded.
			 * If a matcher is connected to the agent, the bid sequence number is set
			 * in newBidInfo and the bid is publised. 
			 * 
			 * @see #setLastBid(BidInfo)
			 */
			BidInfo updatedBidInfo = publishBidUpdate(newBidInfo);
			logInfo("Published new bid " + updatedBidInfo);
		}
	}

	/**
	 * Initialize the agent by setting the agent's fields with the configured
	 * properties or their default values, if applicable.
	 */
	private void initialize() {
		/*
		 * Get the configured bid step price, or the default value it is not configured.
		 */
		this.bidPrice = getProperty(ExampleAgent1Configuration.BID_PRICE_PROPERTY, ExampleAgent1Configuration.BID_PRICE_DEFAULT);
		/*
		 * Get the configured bid step demand, or the default value it is not configured.
		 */
		this.bidPower = getProperty(ExampleAgent1Configuration.BID_POWER_PROPERTY, ExampleAgent1Configuration.BID_POWER_DEFAULT);
	}

	/**
	 * Configures the agent.
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
	 * Update market basis with the specified new market basis parameter.
	 * This callback method is invoked when the agent receives its initial
	 * or updated market basis.<br>
	 * This implementation extends the default behavior, which is to 
	 * set the new market basis, by logging the updated market basis.
	 * 
	 * @param newMarketBasis
	 *            The new market basis (<code>MarketBasis</code>) parameter.
	 * @see #getCurrentMarketBasis()
	 * @see #setCurrentMarketBasis(MarketBasis)
	 */
	@Override
	public void updateMarketBasis(final MarketBasis newMarketBasis) {
		logInfo("New market basis received:" + newMarketBasis);
		super.updateMarketBasis(newMarketBasis);
	}

	/**
	 * Update price info with the specified new price info parameter.
	 * This callback method is invoked when the agent receives its initial
	 * or updated market price.<br>
	 * This implementation extends the default behavior, which is to 
	 * set the new price, by logging the updated price.
	 * 
	 * @param newPriceInfo
	 *            The new price info (<code>PriceInfo</code>) parameter.
	 * @see #getCurrentMarketBasis()
	 * @see #setCurrentMarketBasis(MarketBasis)
	 */
	@Override
	public void updatePriceInfo(final PriceInfo newPriceInfo) {
		logInfo("New price info received:" + newPriceInfo);
		super.updatePriceInfo(newPriceInfo);
	}

}
