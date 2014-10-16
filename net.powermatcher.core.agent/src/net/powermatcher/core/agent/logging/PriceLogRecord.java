package net.powermatcher.core.agent.logging;


import java.text.DateFormat;
import java.util.Date;

import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.log.PriceLogInfo;


/**
 * @author IBM
 * @version 0.9.0
 */
public class PriceLogRecord extends LogRecord {
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
			"currentPrice",
			"lastUpdateTime" };

	/**
	 * Gets the header row (String[]) value.
	 * 
	 * @return The header row (<code>String[]</code>) value.
	 */
	public static String[] getHeaderRow() {
		return HEADER_ROW;
	}

	/**
	 * Define the price log info (PriceLogInfo) field.
	 */
	private PriceLogInfo priceLogInfo;

	/**
	 * Constructs an instance of this class from the specified price log info
	 * parameter.
	 * 
	 * @param priceLogInfo
	 *            The price log info (<code>PriceLogInfo</code>) parameter.
	 */
	public PriceLogRecord(final PriceLogInfo priceLogInfo) {
		super(priceLogInfo.getClusterId(), priceLogInfo.getAgentId(), priceLogInfo.getQualifier());
		this.priceLogInfo = priceLogInfo;
	}

	/**
	 * Clear the log record to indicate that there is no new data for this
	 * equipment id.
	 */
	@Override
	public void clear() {
		this.priceLogInfo = null;
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
		if (this.priceLogInfo == null) {
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
					null };
		} else {
			MarketBasis marketBasis = this.priceLogInfo.getMarketBasis();
			return new String[] {
					dateFormat.format(logTime),
					this.clusterId,
					this.id,
					this.qualifier,
					marketBasis.getCommodity(),
					marketBasis.getCurrency(),
					MarketBasis.PRICE_FORMAT.format(marketBasis.getMinimumPrice()),
					MarketBasis.PRICE_FORMAT.format(marketBasis.getMaximumPrice()),
					MarketBasis.PRICE_FORMAT.format(this.priceLogInfo.getCurrentPrice()),
					dateFormat.format(this.priceLogInfo.getTimestamp()) };
		}
	}

}
