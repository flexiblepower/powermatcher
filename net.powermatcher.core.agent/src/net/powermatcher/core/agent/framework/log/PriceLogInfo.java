package net.powermatcher.core.agent.framework.log;


import java.util.Date;

import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PriceInfo;


/**
 * @author IBM
 * @version 0.9.0
 */
public class PriceLogInfo extends AbstractLogInfo {
	/**
	 * Define the current price (double) field.
	 */
	private double currentPrice;

	/**
	 * Constructs an instance of this class from the specified cluster ID, agent
	 * ID, qualifier, time stamp, market basis and current price parameters.
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
	 * @param currentPrice
	 *            The current price (<code>double</code>) parameter.
	 * @see #PriceLogInfo(String,String,String,Date,PriceInfo)
	 */
	public PriceLogInfo(final String clusterId, final String agentId, final String qualifier, final Date timestamp,
			final MarketBasis marketBasis, final double currentPrice) {
		super(clusterId, agentId, qualifier, timestamp, marketBasis);
		this.currentPrice = currentPrice;
	}

	/**
	 * Constructs an instance of this class from the specified cluster ID, agent
	 * ID, qualifier, time stamp and price info parameters.
	 * 
	 * @param clusterId
	 *            The cluster ID (<code>String</code>) parameter.
	 * @param agentId
	 *            The agent ID (<code>String</code>) parameter.
	 * @param qualifier
	 *            The qualifier (<code>String</code>) parameter.
	 * @param timestamp
	 *            The time stamp (<code>Date</code>) parameter.
	 * @param priceInfo
	 *            The price info (<code>PriceInfo</code>) parameter.
	 * @see #PriceLogInfo(String,String,String,Date,MarketBasis,double)
	 */
	public PriceLogInfo(final String clusterId, final String agentId, final String qualifier, final Date timestamp,
			final PriceInfo priceInfo) {
		this(clusterId, agentId, qualifier, timestamp, priceInfo.getMarketBasis(), priceInfo.getCurrentPrice());
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
		strb.append(", currentPrice=");
		strb.append(getCurrentPrice());
	}

	/**
	 * Gets the current price (double) value.
	 * 
	 * @return The current price (<code>double</code>) value.
	 */
	public double getCurrentPrice() {
		return this.currentPrice;
	}

}
