package net.powermatcher.agent.peakshavingconcentrator;


import net.powermatcher.agent.peakshavingconcentrator.config.ClippingConcentratorConfiguration;
import net.powermatcher.core.agent.concentrator.framework.AbstractPeakShavingConcentrator;
import net.powermatcher.core.agent.concentrator.service.PeakShavingNotificationService;
import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.configurable.service.Configurable;


/**
 * @author IBM
 * @version 0.9.0
 */
public class ClippingConcentrator extends AbstractPeakShavingConcentrator {
	/**
	 * Price uplift on calculated minimum price after applying 'peak shaving'
	 */
	private static final int CONCENTRATOR_MININUM_PRICE_UPLIFT = 1;
	/**
	 * Minimum price calculated by concentrator after applying 'peak shaving'
	 */
	private int concentratorMininumPrice = Integer.MIN_VALUE;
	/**
	 * Maximum price calculated by concentrator after applying 'peak shaving'
	 */
	private int concentratorMaximumPrice = Integer.MAX_VALUE;
	/**
	 * Power limits ('peak shaving') enabled
	 */
	private boolean powerLimitEnabled = false;
	/**
	 * Constructs an instance of this class.
	 * 
	 * @see #ClippingConcentrator(Configurable)
	 */
	public ClippingConcentrator() {
		super();
	}

	/**
	 * Constructs an instance of this class from the specified properties
	 * parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @see #ClippingConcentrator()
	 */
	public ClippingConcentrator(final Configurable configuration) {
		super(configuration);
	}

	
	
	/**
	 * Returns if power limit ('peak shaving') is enabled.
	 * @return	true when enabled. Otherwise false.
	 */
	public boolean isPowerLimitEnabled() {
		return powerLimitEnabled;
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
	
	@Override
	public synchronized void setFlowConstraints(final double newCeiling, final double newFloor) {
		this.floor = newFloor;
		this.ceiling = newCeiling;
		
		if (this.floor == 0 && this.ceiling == 0) {
			setPowerLimitEnabled(false);
		}
		else {
			setPowerLimitEnabled(true);
		}
		
		BidInfo aggregatedBid = this.getAggregatedBid();
		if (aggregatedBid != null) {
			this.handleAggregatedBidUpdate(aggregatedBid);
		}
		
		PriceInfo priceInfo = getLastPriceInfo();
		if (priceInfo != null) {
			this.updatePriceInfo(priceInfo);
		}
	}

	public void setPowerLimitEnabled(boolean powerLimitEnabled) {
		this.powerLimitEnabled = powerLimitEnabled;
		
		if (isInfoEnabled()) {
			if (this.powerLimitEnabled) {
				logInfo("Peak shaving is enabled. Upper limit: " + this.ceiling + " lower limit: "
						+ this.floor);
			}
			else {
				logInfo("Peak shaving is not enabled.");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.powermatcher.core.agent.concentrator.framework.AbstractPeakShavingConcentrator#unbind(net.powermatcher.core.agent.concentrator.service.PeakShavingNotificationService)
	 */
	@Override
	public void unbind(PeakShavingNotificationService peakShavingAdapter) {
		super.unbind(peakShavingAdapter);
		initializePeakShavingFromProperties();
	}
	
	/**
	 * Adjust the downstream price for for peak shaving, if enabled. Note: this
	 * implementation does not create a copy of newPriceInfo, but modifies it.
	 * 
	 * @param newPriceInfo
	 *            The new price info (<code>PriceInfo</code>) parameter.
	 * @return The adjusted price
	 */
	@Override
	protected PriceInfo adjustPrice(final PriceInfo newPriceInfo) {
		PriceInfo adjustedPriceInfo = newPriceInfo;
		if (isPowerLimitEnabled()) {
			adjustedPriceInfo = new PriceInfo(newPriceInfo.getMarketBasis(), newPriceInfo.getCurrentPrice());
			int newPrice = newPriceInfo.getNormalizedPrice();
			if (newPrice < this.concentratorMininumPrice) {
				if (isInfoEnabled()) {
					logInfo("Applying peak shaving concentrator minimum price. Market price " + newPrice + " becomes "
							+ this.concentratorMininumPrice);
				}
				newPrice = this.concentratorMininumPrice;
			} else if (newPrice > this.concentratorMaximumPrice) {
				if (isInfoEnabled()) {
					logInfo("Applying peak shaving concentrator maximum price. Market price " + newPrice + " becomes "
							+ this.concentratorMaximumPrice);
				}
				newPrice = this.concentratorMaximumPrice;
			}
			if (newPrice != newPriceInfo.getNormalizedPrice()) {
				adjustedPriceInfo = new PriceInfo(newPriceInfo.getMarketBasis(), newPrice); 
			}
		}
		return adjustedPriceInfo;
	}

	
	/**
	 * Transform the aggregated bid by applying peak shaving, if peak shaving
	 * enabled. Note: this implementation does not create a copy of newBidInfo,
	 * but modifies it.
	 * 
	 * @param newAggregatedBid
	 *            The new aggregated bid info (<code>BidInfo</code>) parameter.
	 * @return Results of the transform aggregated bid (<code>BidInfo</code>)
	 *         value.
	 */
	@Override
	protected BidInfo transformAggregatedBid(final BidInfo newAggregatedBid) {
		this.aggregatedBidIn = newAggregatedBid;
		
		BidInfo transformedBid;		
		if (isPowerLimitEnabled()) {
			transformedBid = applyPeakShaving(newAggregatedBid);
		} else {
			transformedBid = newAggregatedBid;
		}
		
		this.aggregatedBidOut = transformedBid;
		
		return this.aggregatedBidOut;
	}
	


	/**
	 * Applies peak shaving on the bid in the parameter and returns the result
	 * in a new bid message.
	 * 
	 * @param bid
	 *            The bid (<code>BidInfo</code>) parameter.
	 * @return The 'shaven' bid
	 */
	private BidInfo applyPeakShaving(final BidInfo bid) {
		MarketBasis marketBasis = bid.getMarketBasis();
		double[] demand = bid.getDemand();
		this.concentratorMininumPrice = marketBasis.toNormalizedPrice(0);
		this.concentratorMaximumPrice = marketBasis.toNormalizedPrice(marketBasis.getPriceSteps() - 1);
		boolean peakShavingApplied = false;
		double power = 0;
		for (int priceStep = 0; priceStep < demand.length; priceStep++) {
			int price = marketBasis.toNormalizedPrice(priceStep);
			power = demand[priceStep];
			if (power > this.ceiling) {
				power = this.ceiling;
				this.concentratorMininumPrice = Math.max(this.concentratorMininumPrice, price
						+ CONCENTRATOR_MININUM_PRICE_UPLIFT);
				peakShavingApplied = true;
			} else if (power < this.floor) {
				power = this.floor;
				this.concentratorMaximumPrice = Math.min(this.concentratorMaximumPrice, price
						- CONCENTRATOR_MININUM_PRICE_UPLIFT);
				peakShavingApplied = true;
			}
			demand[priceStep] = power;
		}
		if (peakShavingApplied) {
			BidInfo newBid = new BidInfo(marketBasis, demand);
			this.concentratorMininumPrice = Math.min(this.concentratorMaximumPrice, this.concentratorMininumPrice);
			this.concentratorMaximumPrice = Math.max(this.concentratorMaximumPrice, this.concentratorMininumPrice);
			if (isInfoEnabled()) {
				int lastPrice = getLastPriceInfo() == null ? 0 : getLastPriceInfo().getNormalizedPrice();
				logInfo("Concentrator peak shaving min price: " + this.concentratorMininumPrice + ", max price: "
						+ this.concentratorMaximumPrice + " (most recent market price: " + lastPrice + ")");
				logInfo("Modified bid after peak shaving: " + bid.toString());
			}
			return newBid;
		}
		return bid;
	}	
	


	/**
	 * Initialize.
	 */
	private void initialize() {
		this.initializePeakShavingFromProperties();
	}



	private void initializePeakShavingFromProperties() {
		this.setPowerLimitEnabled(getProperty(ClippingConcentratorConfiguration.PEAK_SHAVING_ENABLED_PROPERTY,
				ClippingConcentratorConfiguration.PEAK_SHAVING_ENABLED_DEFAULT));
 
		if (isPowerLimitEnabled()) {
			this.setFlowConstraints(getDoubleProperty(ClippingConcentratorConfiguration.POWER_UPPER_LIMIT_PROPERTY), 
					getDoubleProperty(ClippingConcentratorConfiguration.POWER_LOWER_LIMIT_PROPERTY));
		} else {
			this.setFlowConstraints(0d, 0d);
		}
	}
	
}
