package net.powermatcher.core.agent.test;


import net.powermatcher.core.agent.framework.Agent;
import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.agent.framework.data.PricePoint;
import net.powermatcher.core.agent.test.config.TestAgentConfiguration;
import net.powermatcher.core.configurable.service.Configurable;


/**
 * @author IBM
 * @version 0.9.0
 */
public class TestAgent extends Agent {
	/**
	 * Define the minimum price (int) field.
	 */
	private int minimumPrice;
	/**
	 * Define the maximum price (int) field.
	 */
	private int maximumPrice;
	/**
	 * Define the minimum power (double) field.
	 */
	private double minimumPower;
	/**
	 * Define the maximum power (double) field.
	 */
	private double maximumPower;
	/**
	 * Define the steps (int) field.
	 */
	private int steps;
	/**
	 * Define the step (int) field.
	 */
	private int step;

	/**
	 * Constructs an instance of this class.
	 * 
	 * @see #TestAgent(Configurable)
	 */
	public TestAgent() {
		super();
	}

	/**
	 * Constructs an instance of this class from the specified properties
	 * parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @see #TestAgent()
	 */
	public TestAgent(final Configurable configuration) {
		super(configuration);
	}

	/**
	 * Do the periodic bid update.
	 */
	@Override
	protected void doBidUpdate() {
		MarketBasis marketBasis = getCurrentMarketBasis();
		if (marketBasis != null) {
			int newPrice = this.minimumPrice + (this.step * (this.maximumPrice - this.minimumPrice)) / this.steps;
			double newPower = this.minimumPower + (this.step * (this.maximumPower - this.minimumPower)) / this.steps;
			if (++this.step == this.steps) {
				this.step = 0;
			}
			PricePoint pricePoint1 = new PricePoint(newPrice, this.minimumPower >= 0 ? newPower : 0);
			PricePoint pricePoint2 = new PricePoint(newPrice, this.minimumPower < 0 ? newPower : 0);
			BidInfo newBidInfo = new BidInfo(marketBasis, pricePoint1, pricePoint2);
			BidInfo updatedBidInfo = publishBidUpdate(newBidInfo);
			logInfo("Published new bid " + updatedBidInfo);
		}
	}

	/**
	 * Initialize.
	 */
	private void initialize() {
		this.minimumPrice = getProperty(TestAgentConfiguration.MINIMUM_PRICE_PROPERTY,
				TestAgentConfiguration.MINIMUM_PRICE_DEFAULT);
		this.maximumPrice = getProperty(TestAgentConfiguration.MAXIMUM_PRICE_PROPERTY,
				TestAgentConfiguration.MAXIMUM_PRICE_DEFAULT);
		this.minimumPower = getProperty(TestAgentConfiguration.MINIMUM_POWER_PROPERTY,
				TestAgentConfiguration.MINIMUM_POWER_DEFAULT);
		this.maximumPower = getProperty(TestAgentConfiguration.MAXIMUM_POWER_PROPERTY,
				TestAgentConfiguration.MAXIMUM_POWER_DEFAULT);
		this.steps = getProperty(TestAgentConfiguration.STEPS_PROPERTY, TestAgentConfiguration.STEPS_DEFAULT);
	}

	/**
	 * Sets the configuration value.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 */
	@Override
	public void setConfiguration(final Configurable configuration) {
		super.setConfiguration(configuration);
		initialize();
	}

	/**
	 * Update price info with the specified new price info parameter.
	 * 
	 * @param newPriceInfo
	 *            The new price info (<code>PriceInfo</code>) parameter.
	 */
	@Override
	public void updatePriceInfo(final PriceInfo newPriceInfo) {
		logInfo("New price info received:" + newPriceInfo);
		super.updatePriceInfo(newPriceInfo);
	}

}
