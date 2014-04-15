package net.powermatcher.core.agent.framework.log;


import java.util.Date;

import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;


/**
 * @author IBM
 * @version 0.9.0
 */
public class BidLogInfo extends AbstractLogInfo {
	/**
	 * Define the bid info (BidInfo) field.
	 */
	private BidInfo bidInfo;
	/**
	 * Define the effective price (double) field.
	 */
	private double effectivePrice;
	/**
	 * Define the effective demand (double) field.
	 */
	private double effectiveDemand;
	/**
	 * Define the minimum demand (double) field.
	 */
	private double minimumDemand;
	/**
	 * Define the maximum demand (double) field.
	 */
	private double maximumDemand;

	/**
	 * Constructs an instance of this class from the specified cluster ID, agent
	 * ID, qualifier, time stamp, market basis, effective price, effective
	 * demand, minimum demand, maximum demand and bid info parameters.
	 * 
	 * @param clusterId
	 *            The cluster ID (<code>String</code>) parameter.
	 * @param agentId
	 *            The agent ID (<code>String</code>) parameter.
	 * @param qualifier
	 *            The qualifier (<code>String</code>) parameter.
	 * @param timestamp
	 *            The time stamp (<code>Date</code>) parameter.
	 * @param marketBasis
	 *            The market basis (<code>MarketBasis</code>) parameter.
	 * @param effectivePrice
	 *            The effective price (<code>double</code>) parameter.
	 * @param effectiveDemand
	 *            The effective demand (<code>double</code>) parameter.
	 * @param minimumDemand
	 *            The minimum demand (<code>double</code>) parameter.
	 * @param maximumDemand
	 *            The maximum demand (<code>double</code>) parameter.
	 * @param bidInfo
	 *            The bid info (<code>BidInfo</code>) parameter.
	 */
	public BidLogInfo(final String clusterId, final String agentId, final String qualifier, final Date timestamp,
			final MarketBasis marketBasis, final double effectivePrice, final double effectiveDemand,
			final double minimumDemand, final double maximumDemand, final BidInfo bidInfo) {
		super(clusterId, agentId, qualifier, timestamp, marketBasis);
		this.bidInfo = bidInfo;
		this.effectivePrice = effectivePrice;
		this.effectiveDemand = effectiveDemand;
		this.minimumDemand = minimumDemand;
		this.maximumDemand = maximumDemand;
	}

	/**
	 * Append data with the specified strb parameter.
	 * 
	 * @param strb
	 *            The strb (<code>StringBuilder</code>) parameter.
	 */
	@Override
	protected void appendData(final StringBuilder strb) {
		super.appendData(strb);
		strb.append(", effectivePrice=");
		strb.append(getEffectivePrice());
		strb.append(", effectiveDemand=");
		strb.append(getEffectiveDemand());
		strb.append(", minimumDemand=");
		strb.append(getMinimumDemand());
		strb.append(", maximumDemand=");
		strb.append(getMaximumDemand());
		strb.append(", bidInfo=");
		strb.append(getBidInfo());
	}

	/**
	 * Gets the bid info value.
	 * 
	 * @return The bid info (<code>BidInfo</code>) value.
	 */
	public BidInfo getBidInfo() {
		return this.bidInfo;
	}

	/**
	 * Gets the effective demand (double) value.
	 * 
	 * @return The effective demand (<code>double</code>) value.
	 */
	public double getEffectiveDemand() {
		return this.effectiveDemand;
	}

	/**
	 * Gets the effective price (double) value.
	 * 
	 * @return The effective price (<code>double</code>) value.
	 */
	public double getEffectivePrice() {
		return this.effectivePrice;
	}

	/**
	 * Gets the maximum demand (double) value.
	 * 
	 * @return The maximum demand (<code>double</code>) value.
	 */
	public double getMaximumDemand() {
		return this.maximumDemand;
	}

	/**
	 * Gets the minimum demand (double) value.
	 * 
	 * @return The minimum demand (<code>double</code>) value.
	 */
	public double getMinimumDemand() {
		return this.minimumDemand;
	}

}
