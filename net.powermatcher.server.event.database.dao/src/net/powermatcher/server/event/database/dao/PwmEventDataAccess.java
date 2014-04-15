package net.powermatcher.server.event.database.dao;


import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.sql.DataSource;

import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PricePoint;
import net.powermatcher.core.agent.framework.log.BidLogInfo;
import net.powermatcher.core.agent.framework.log.PriceLogInfo;
import net.powermatcher.core.messaging.protocol.adapter.log.BidLogMessage;
import net.powermatcher.core.messaging.protocol.adapter.log.PriceLogMessage;
import net.powermatcher.server.event.database.jdbc.PwmEventDataJDBCAccess;

public class PwmEventDataAccess {

	/**
	 * Define the logger (Logger) field.
	 */
	protected final static Logger logger = Logger.getLogger(PwmEventDataAccess.class.getName());

	private static final int MAX_PRICE_STEPS = 1024;
	
	private static Map<String, PwmEventDataAccess> myInstances = new HashMap<String, PwmEventDataAccess>();
	private DataSource dataSource;
	private PwmEventDataJDBCAccess jdbcAccess;

	
	protected PwmEventDataAccess(DataSource dataSource) {
		this.dataSource = dataSource;
		this.jdbcAccess = PwmEventDataJDBCAccess.singleton();
	}
	
	public synchronized static PwmEventDataAccess getInstance(String dataSourceName) throws Exception {
		PwmEventDataAccess dataAccess = myInstances.get(dataSourceName);
		if (dataAccess == null) {
			DataSource dataSource = ResourceLocator.singleton().getDataSource(dataSourceName);
			if (dataSource != null) {
				dataAccess = new PwmEventDataAccess(dataSource);
				myInstances.put(dataSourceName, dataAccess);
			}
		}
		
		return dataAccess;
	}

	/**
	 * Add the bid data in the message to the database.
	 * @param message		The bid log message.
	 * @throws SQLException
	 */
	public void addBidInfoData(BidLogMessage message) throws SQLException {
		logger.entering(this.getClass().toString(), "addBidInfoData");
		
		BidLogInfo bidLogInfo = message.getBidLogInfo();
		BidInfo bidInfo = bidLogInfo.getBidInfo();
	
		String clusterId = bidLogInfo.getClusterId();
		String agentId = bidLogInfo.getAgentId();
		String qualifier = bidLogInfo.getQualifier();
		Date timestamp = bidLogInfo.getTimestamp();
		
		// Get market basis data
		MarketBasis mb = bidInfo.getMarketBasis();
		String commodity = mb.getCommodity();
		String currency = mb.getCurrency();
		float minPrice = (float)mb.getMinimumPrice();
		float maxPrice = (float)mb.getMaximumPrice();
		int priceSteps = mb.getPriceSteps();
		
		// Change the Market basis if the max price steps is exceeded
		if (priceSteps > MAX_PRICE_STEPS) {
			priceSteps = MAX_PRICE_STEPS;
			MarketBasis mbNew = new MarketBasis(commodity, currency, priceSteps, minPrice, maxPrice, mb.getSignificance(), mb.getMarketRef());
			bidInfo = bidInfo.toMarketBasis(mbNew);
		}
		
		float minDemand = (float)bidLogInfo.getMinimumDemand();
		float maxDemand = (float)bidLogInfo.getMaximumDemand();
		float effectivePrice = (float)bidLogInfo.getEffectivePrice();
		float effectiveDemand = (float)bidLogInfo.getEffectiveDemand();
		
		// Derive data from bid info
		byte[] demandCurve = null;
		float demandFactor = 1;
		Float stepPrice = null;
		if (bidInfo != null) {
			// Determine price points.
			PricePoint[] pricePoints = bidInfo.getPricePoints();
			if (pricePoints != null && pricePoints.length == 2) {
				int price1 = pricePoints[0].getNormalizedPrice();
				int price2 = pricePoints[1].getNormalizedPrice();
				if (price1 == price2) {
					// Single step
					stepPrice = (float) price1;
				} 
				else {
					// Create demand array
					demandFactor = (float)bidInfo.getScaleFactor(Short.MAX_VALUE);
					demandCurve = demandToByteArray(bidInfo.getDemand(), demandFactor);
				}
			}
			else if (pricePoints != null && pricePoints.length == 1) {
				stepPrice = 0f;
			}
			else if (pricePoints == null || pricePoints.length > 2) {
				// Create demand array
				demandFactor = (float)bidInfo.getScaleFactor(Short.MAX_VALUE);
				demandCurve = demandToByteArray(bidInfo.getDemand(), demandFactor);
			}
		}
		
		try {
			Connection conn = this.dataSource.getConnection();
			this.jdbcAccess.addBidData(conn, clusterId, agentId, qualifier, timestamp, minDemand, 
							maxDemand, effectivePrice, effectiveDemand, stepPrice, demandFactor, demandCurve, commodity, currency, minPrice, maxPrice, priceSteps);
		} catch (SQLException e) {
			String msg = "Error inserting bid data ({1}, {2}, {3}, {4}, {5}, {6}, {7}, {8}, {9}, {10}, {11}, {12}, {13}, {14}, {15}, {16}, ): {0}";
			Object args[] = new Object[] { e, clusterId, agentId, qualifier, timestamp, minDemand, maxDemand, effectivePrice, effectiveDemand, stepPrice, demandFactor, demandCurve, commodity, currency, minPrice, maxPrice, priceSteps };			
			String errorMsg = MessageFormat.format(msg, args);
			logger.severe("addBidInfoData " + errorMsg);
			throw  e;
		}
		logger.exiting(this.getClass().toString(), "addBidInfoData");
	}
	

	
	/**
	 * Add the price info from the message to the database.
	 * @param message		The price log message containing the price info.
	 * @throws SQLException
	 */
	public void addPriceInfoData(PriceLogMessage message) throws SQLException {
		logger.entering(this.getClass().toString(), "addPriceInfoData");
		
		PriceLogInfo priceLogInfo = message.getPriceLogInfo();
		String clusterId = priceLogInfo.getClusterId();
		String agentId = priceLogInfo.getAgentId();
		String qualifier = priceLogInfo.getQualifier();
		Date timestamp = priceLogInfo.getTimestamp();
		float marketPrice = (float)priceLogInfo.getCurrentPrice();
			
		// Get market basis data
		MarketBasis mb = priceLogInfo.getMarketBasis();
		String commodity = mb.getCommodity();
		String currency = mb.getCurrency();
		float minPrice = (float)mb.getMinimumPrice();
		float maxPrice = (float)mb.getMaximumPrice();
		int priceSteps = mb.getPriceSteps();
		
		try {		
			Connection conn = this.dataSource.getConnection();
			this.jdbcAccess.addPriceData(conn, clusterId, agentId, qualifier, timestamp, marketPrice, commodity, currency, minPrice, maxPrice, priceSteps);
			
		} catch (SQLException e) {
			String msg = "Error inserting alert data ({1}, {2}, {3}, {4}, {5}, {6}, {7}, {8}, {9}, {10}): {0}";
			Object args[] = new Object[] { e, clusterId, agentId, qualifier, timestamp, marketPrice, commodity, currency, minPrice, maxPrice, priceSteps };
			logger.severe(MessageFormat.format(msg, args));
			throw e;
		}
		logger.exiting(this.getClass().toString(), "addPriceInfoData");

	}
	
	/**
	 * Convert the demand array to a byte array and scaled by a
	 * factor
	 * @param demand		The demand array.
	 * @param scaleFactor	The scale factor.
	 * @return
	 */
	private byte[] demandToByteArray(double demand[], float scaleFactor) {
		byte[] demandCurveBytes = null;
		if (demand != null) {
			int len = demand.length;
			demandCurveBytes = new byte[len * 2];
			for (int i = 0; i < len; i++) {
				short s = (short) (demand[i]/scaleFactor);
				demandCurveBytes[(2 * i)] = (byte) ((s >> 8) & 0x00FF);
				demandCurveBytes[(2 * i) + 1] = (byte) (s & 0x00FF);
			}
		}
		
		return demandCurveBytes;
	}
	
	public static short readShort(byte[] data, int offset) {
		return (short) (((data[offset] << 8)) | ((data[offset + 1] & 0xff)));
	}
}
