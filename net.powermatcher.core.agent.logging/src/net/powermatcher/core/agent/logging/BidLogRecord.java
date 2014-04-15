package net.powermatcher.core.agent.logging;


import java.text.DateFormat;
import java.util.Date;

import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.log.BidLogInfo;


/**
 * @author IBM
 * @version 0.9.0
 */
public class BidLogRecord extends LogRecord {
	/**
	 * Define the header row (String[]) constant.
	 */
	private static final String[] HEADER_ROW = new String[] {
			"logTime",
			"clusterId",
			"id",
			"qualifier",
			"commodity",
			"currency",
			"minimumPrice",
			"maximumPrice",
			"minimumDemand",
			"maximumDemand",
			"effectiveDemand",
			"effectivePrice",
			"lastUpdateTime",
			"bidInfo" };

	/**
	 * Gets the header row (String[]) value.
	 * 
	 * @return The header row (<code>String[]</code>) value.
	 */
	public static String[] getHeaderRow() {
		return HEADER_ROW;
	}

	/**
	 * Define the bid log info (BidLogInfo) field.
	 */
	private BidLogInfo bidLogInfo;

	/**
	 * Constructs an instance of this class from the specified bid log info
	 * parameter.
	 * 
	 * @param bidLogInfo
	 *            The bid log info (<code>BidLogInfo</code>) parameter.
	 */
	public BidLogRecord(final BidLogInfo bidLogInfo) {
		super(bidLogInfo.getClusterId(), bidLogInfo.getAgentId(), bidLogInfo.getQualifier());
		this.bidLogInfo = bidLogInfo;
	}

	/**
	 * Clear the log record to indicate that there is no new data for this
	 * equipment id.
	 */
	@Override
	public void clear() {
		this.bidLogInfo = null;
	}

	/**
	 * Get data row with the specified log time parameter and return the
	 * String[] result.
	 * 
	 * @param dateFormat
	 *            The date format (<code>DateFormat</code>) parameter.
	 * @param logTime
	 *            The log time (<code>Date</code>) parameter.
	 * @return Results of the get data row (<code>String[]</code>) value.
	 */
	@Override
	public String[] getDataRow(final DateFormat dateFormat, final Date logTime) {
		if (this.bidLogInfo == null) {
			return new String[] {
					dateFormat.format(logTime),
					this.clusterId,
					this.id,
					this.qualifier,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					null };
		} else {
			MarketBasis marketBasis = this.bidLogInfo.getMarketBasis();
			BidInfo bidInfo = this.bidLogInfo.getBidInfo();
			String bidInfoStr = null;
			if (bidInfo != null) {
				bidInfoStr = "\"" + bidInfo + "\"";
			}
			return new String[] {
					dateFormat.format(logTime),
					this.clusterId,
					this.id,
					this.qualifier,
					marketBasis.getCommodity(),
					marketBasis.getCurrency(),
					MarketBasis.PRICE_FORMAT.format(marketBasis.getMinimumPrice()),
					MarketBasis.PRICE_FORMAT.format(marketBasis.getMaximumPrice()),
					MarketBasis.DEMAND_FORMAT.format(this.bidLogInfo.getMinimumDemand()),
					MarketBasis.DEMAND_FORMAT.format(this.bidLogInfo.getMaximumDemand()),
					MarketBasis.DEMAND_FORMAT.format(this.bidLogInfo.getEffectiveDemand()),
					MarketBasis.PRICE_FORMAT.format(this.bidLogInfo.getEffectivePrice()),
					dateFormat.format(this.bidLogInfo.getTimestamp()),
					bidInfoStr };
		}
	}

}
