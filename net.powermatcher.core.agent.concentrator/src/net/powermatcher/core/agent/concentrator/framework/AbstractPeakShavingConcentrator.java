package net.powermatcher.core.agent.concentrator.framework;


import net.powermatcher.core.agent.concentrator.config.ConcentratorConfiguration;
import net.powermatcher.core.agent.concentrator.service.PeakShavingConnectorService;
import net.powermatcher.core.agent.concentrator.service.PeakShavingNotificationService;
import net.powermatcher.core.agent.concentrator.service.PeakShavingService;
import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.configurable.service.ConfigurationService;


/**
 * @author IBM
 * @version 0.9.0
 */
public abstract class AbstractPeakShavingConcentrator extends AbstractConcentrator implements PeakShavingService,
		PeakShavingConnectorService {

	/**
	 * Define the peak shaving adapter (PeakShavingNotificationService) field.
	 */
	private PeakShavingNotificationService peakShavingAdapter;

	/**
	 * The immediate update configuration property 
	 */
	protected boolean immediateUpdate;

	/**
	 * Minimum power level applied in 'peak shaving'
	 */
	protected double floor = -Double.MAX_VALUE;
	/**
	 * Maximum power level applied in 'peak shaving'
	 */
	protected double ceiling = Double.MAX_VALUE;
	/**
	 * Measured flow as reported via the peak shaving interface 
	 */
	protected double measuredFlow = Double.NaN;
	/**
	 * Define the allocation field. 
	 */
	protected double allocation;
	
	/* 
	 * The bids and demand function 
	 */
	
	/**
	 * The current aggregated bid, based on the bids received from the
	 * concentrator's children
	 */
	protected BidInfo aggregatedBidIn = null;
	/**
	 *  The current aggregated bid propagated to the concentrator's parent 
	 */
	protected BidInfo aggregatedBidOut = null;

	/*
	 *  The prices 
	 */

	/**
	 * The current price received from the concentrator's parent
	 */
	protected PriceInfo priceIn = null;
	/**
	 * The current price propagated to the concentrator's children 
	 */
	protected PriceInfo priceOut = null;

	/**
	 * Constructs an instance of this class.
	 * 
	 * @see #AbstractPeakShavingConcentrator(ConfigurationService)
	 */
	public AbstractPeakShavingConcentrator() {
		super();
	}

	/**
	 * Constructs an instance of this class from the specified properties
	 * parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @see #AbstractPeakShavingConcentrator()
	 */
	public AbstractPeakShavingConcentrator(final ConfigurationService configuration) {
		super(configuration);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.powermatcher.core.agent.concentrator.service.PeakShavingConnectorService
	 * #bind(net.powermatcher.core.agent.concentrator.service.
	 * PeakShavingNotificationService)
	 */
	@Override
	public void bind(final PeakShavingNotificationService peakShavingAdapter) {
		assert this.peakShavingAdapter == null;
		this.peakShavingAdapter = peakShavingAdapter;
	}

	@Override
	public synchronized double getAllocation() {
		// calculating an allocation is only feasible if the aggregated bid and
		// current price are known.
		if (this.aggregatedBidIn == null || this.priceOut == null) {
			return Double.NaN;
		}

		// use the framework to determine the allocation
		return this.aggregatedBidIn.getDemand(this.priceOut.getCurrentPrice());
	}


	@Override
	public synchronized double getFlowReduction() {
		// retrieve the current allocation
		double allocation = this.getAllocation();

		// if allocation, aggregated bid in or price in is unknown, we can't
		// calculate the flow reduction.
		if (Double.isNaN(allocation) || this.aggregatedBidIn == null || this.priceIn == null) {
			return Double.NaN;
		}

		// calculate the allocation which would have been the fact if no
		// transformation would have been performed
		double untransformedAllocation = this.aggregatedBidIn.getDemand(this.priceIn.getCurrentPrice());

		// debug
		logInfo("BID(IN)" + this.aggregatedBidIn);
		logInfo("BID(OUT)" + this.aggregatedBidOut);
		logInfo("CURRENTPRICE(OUT)=" + this.priceOut.getCurrentPrice());
		logInfo("CURRENTPRICE(in)=" + this.priceIn.getCurrentPrice());
		logInfo("ALLOCATION=" + allocation);
		logInfo("UNTRANSFORMEDALLOC=" + untransformedAllocation);		
		// end debug
		
		// calculate and return the flow reduction as the absolute value of the
		// difference between the allocation with and without transformation
		return Math.abs(untransformedAllocation - allocation);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.powermatcher.core.agent.concentrator.service.PeakShavingConnectorService
	 * #getPeakShavingAgent()
	 */
	@Override
	public PeakShavingService getPeakShavingAgent() {
		return this;
	}
	
	@Override
	public synchronized double getUncontrolledFlow() {
		// the uncontrolled flow can only be calculated if the measured flow is
		// known
		if (Double.isNaN(this.measuredFlow)) {
			return Double.NaN;
		}

		// retrieve the current allocation
		double allocation = this.getAllocation();

		// if that isn't known the uncontrolled flow can't be calculated
		if (Double.isNaN(allocation)) {
			return Double.NaN;
		}

		// calculate the uncontrolled flow using the difference between the
		// measured flow and the allocation for the cluster
		return this.measuredFlow - allocation;
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

	@Override
	public synchronized void setFlowConstraints(final double newCeiling, final double newFloor) {
		if (Double.isNaN(newCeiling) || Double.isNaN(newFloor)) {
			throw new IllegalArgumentException("The floor and ceiling must be a number (and not NaN).");
		}

		if (newCeiling < newFloor) {
			throw new IllegalArgumentException("The floor constraint shouldn't be higher than the ceiling constraint!");
		}

		// store the new ceiling and floor
		// and if either or both of them are infinity, use the heighest or
		// lowest possible number in stead to ensure good results from the
		// algorithm
		this.ceiling = Double.isInfinite(newCeiling) ? Double.MAX_VALUE : newCeiling;
		this.floor = Double.isInfinite(newFloor) ? Double.MIN_VALUE : newFloor;

		if (this.immediateUpdate) {
			// do an update of the aggregated bid (clip with the new ceiling and
			// floor).
			if (this.aggregatedBidIn != null) {
				this.handleAggregatedBidUpdate(this.aggregatedBidIn);
			}
	
			// and update the price towards the concentrator's children (ensuring
			// that the new constraints are met).
			if (this.priceIn != null) {
				this.updatePriceInfo(this.priceIn);
			}
		}
	}
	
	@Override
	public synchronized void setMeasuredFlow(final double newMeasuredFlow) {
		if (Double.isInfinite(newMeasuredFlow) || Double.isNaN(newMeasuredFlow)) {
			throw new IllegalArgumentException(
					"The measured flow cannot be infinity and must be a number (and not NaN)!");
		}

		// we remember the new measurement
		this.measuredFlow = newMeasuredFlow;

		if (this.immediateUpdate) {
			// and do an update of the aggregated bid (clip taking the new
			// measurements into account).
			if (this.aggregatedBidIn != null) {
				this.handleAggregatedBidUpdate(this.aggregatedBidIn);
			}
	
			// as well as the price towards the concentrator's children (ensure
			// that, given the new measurements, the floor and ceiling constraints
			// are met)
			if (this.priceIn != null) {
				this.updatePriceInfo(this.priceIn);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.powermatcher.core.agent.concentrator.service.PeakShavingConnectorService
	 * #unbind(net.powermatcher.core.agent.concentrator.service.
	 * PeakShavingNotificationService)
	 */
	@Override
	public void unbind(final PeakShavingNotificationService peakShavingAdapter) {
		this.peakShavingAdapter = null;
	}


	/**
	 * Update the price info (<code>PriceInfo</code>) value and 
	 * notify the peak shaving adapter.
	 * 
	 * @see net.powermatcher.core.agent.concentrator.framework.AbstractConcentrator#updatePriceInfo(net.powermatcher.core.agent.framework.data.PriceInfo)
	 */
	@Override
	public void updatePriceInfo(PriceInfo newPriceInfo) {
		super.updatePriceInfo(newPriceInfo);
		allocationUpdatedNotification();
	}
	
	@Override
	protected synchronized PriceInfo adjustPrice(final PriceInfo newPrice) {
		this.priceIn = this.priceOut = newPrice;
		return this.priceOut;
	}

	
	/**
	 * Notify the peak shaving adapter the allocation
	 * has been updated.
	 */
	protected void allocationUpdatedNotification() {
		if (getPeakShavingAdapter() != null) {
			getPeakShavingAdapter().updatedAllocation();
		}		
	}
	
	
	
	/**
	 * @return the peakShavingAdapter
	 */
	protected PeakShavingNotificationService getPeakShavingAdapter() {
		return peakShavingAdapter;
	}
	
	
	/**
	 * Handle the aggregated bid update (<code>BidInfo</code>) value and 
	 * notify the peak shaving adapter.
	 * 
	 * @see net.powermatcher.core.agent.concentrator.framework.AbstractConcentrator#handleAggregatedBidUpdate(net.powermatcher.core.agent.framework.data.BidInfo)
	 */
	@Override
	protected void handleAggregatedBidUpdate(BidInfo newBidInfo) {
		super.handleAggregatedBidUpdate(newBidInfo);
		allocationUpdatedNotification();
	}
	
	/**
	 * Initialize.
	 */
	private void initialize() {
		this.immediateUpdate = getProperty(ConcentratorConfiguration.IMMEDIATE_UPDATE_PROPERTY,
				ConcentratorConfiguration.IMMEDIATE_UPDATE_DEFAULT);
	}

	@Override
	protected synchronized BidInfo transformAggregatedBid(final BidInfo newAggregatedBid) {
		this.aggregatedBidIn = this.aggregatedBidOut = newAggregatedBid;
		return this.aggregatedBidOut;
	}
}
