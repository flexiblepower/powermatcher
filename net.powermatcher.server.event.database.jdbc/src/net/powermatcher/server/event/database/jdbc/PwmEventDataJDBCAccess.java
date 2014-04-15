package net.powermatcher.server.event.database.jdbc;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Logger;

public class PwmEventDataJDBCAccess {
	
	/**
	 * Define the logger (Logger) field.
	 */
	protected final static Logger logger = Logger.getLogger(PwmEventDataJDBCAccess.class.getName());
	
	private static final Calendar UTC = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
	
	private static PwmEventDataJDBCAccess singleton;

	private static Map<MarketBasisData, MarketBasisData> marketBasisDataCache;
	
	public static PwmEventDataJDBCAccess singleton() {
		if (singleton == null) {
			singleton = new PwmEventDataJDBCAccess();
		}
		
		return singleton;
	}

	
	public void addPriceData(Connection conn, String clusterId, String agentId, String qualifier, Date timestamp, float marketPrice, String commodity, String currency, float minPrice, float maxPrice, int priceSteps) throws SQLException {
		Integer marketBasisId = null;
		String sql = "INSERT INTO PWMEVENT.PRICEDATA (CLUSTERID, AGENTID, QUALIFIER, MARKETBASISID, TIMESTAMP, MARKETPRICE) values (?, ?, ?, ?, ?, ?)";
		try {
			// Insert the marketbasis and get the id
			marketBasisId = findOrInsertMarketBasis(conn, clusterId, commodity, currency, minPrice, maxPrice, priceSteps);
			
			PreparedStatement ps = conn.prepareStatement(sql);
		
			ps.setString(1, clusterId);
			ps.setString(2, agentId);
			ps.setString(3, qualifier);
			ps.setInt(4, marketBasisId);
			ps.setTimestamp(5, new Timestamp(timestamp.getTime()), UTC);
			ps.setFloat(6, (float)marketPrice);
			int count = ps.executeUpdate();
			if (count != 1) {
				// SQL failed!
				String msg = "Error inserting ({0}, {1}, {2}, {3}, {4}, {5}";
				Object args[] = new Object[] { clusterId, agentId, qualifier, marketBasisId, timestamp, marketPrice };
				logger.warning(this + "addPriceData" + MessageFormat.format(msg, args));
			}
		} catch (SQLException e) {
			String msg = "Error inserting ({1}, {2}, {3}, {4}, {5}, {6}): {0}";
			Object args[] = new Object[] { e, clusterId, agentId, qualifier, marketBasisId, timestamp, marketPrice };
			logger.severe(this + "addPriceData" + MessageFormat.format(msg, args));
			throw e;
		}
	}
	
	public int findOrInsertMarketBasis(Connection conn, String clusterId, String commodity, String currency, float minPrice, float maxPrice, int priceSteps) throws SQLException {
		
		MarketBasisData mbd = findInMarketBasisDataCache(conn, clusterId, commodity, currency, minPrice, maxPrice, priceSteps);
		if (mbd == null) {
			mbd = insertMarketBasisData(conn, clusterId, commodity, currency, minPrice, maxPrice, priceSteps);
			updateMarketBasisDataCache(mbd);
		}

		return mbd.getId();
	}
	


	public MarketBasisData insertMarketBasisData(Connection conn, String clusterId, String commodity, String currency, float minPrice, float maxPrice, int priceSteps) throws SQLException {
		MarketBasisData mbd = null;
		String sql = "INSERT INTO PWMEVENT.MARKETBASISDATA (CLUSTERID, COMMODITY, CURRENCY, MINPRICE, MAXPRICE, PRICESTEPS) values (?, ?, ?, ?, ?, ?)";
		try {
			PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		
			ps.setString(1, clusterId);
			ps.setString(2, commodity);
			ps.setString(3, currency);
			ps.setFloat(4, minPrice);
			ps.setFloat(5, maxPrice);
			ps.setInt(6, priceSteps);
			int count = ps.executeUpdate();
			if (count != 1) {
				// SQL failed!
				String msg = "Error inserting ({0}, {1}, {2}, {3}, {4}, {5}";
				Object args[] = new Object[] { clusterId, commodity, currency, minPrice, maxPrice, priceSteps };
				// Discuss with Aldo. Is this not a severe error?
				logger.warning(this + "addMarketBasisData" + MessageFormat.format(msg, args));
			}
			else {
				ResultSet rs = ps.getGeneratedKeys();
				if (rs.next()) {
					int id = rs.getInt(1);
					mbd = new MarketBasisData(id, clusterId, commodity, currency, minPrice, maxPrice, priceSteps);
		        } else {
		            throw new SQLException("Creating user failed, no generated key obtained.");
		        }
				
				
			}
		} catch (SQLException e) {
			String msg = "Error inserting ({1}, {2}, {3}, {4}, {5}, {6}): {0}";
			Object args[] = new Object[] { e, clusterId, commodity, currency, minPrice, maxPrice, priceSteps };
			logger.severe(this + "addMarketBasisData" + MessageFormat.format(msg, args));
			throw e;
		}
		
		return mbd;
	}
	

	public void addBidData(Connection conn, String clusterId, String agentId, String qualifier,	Date timestamp, 
			float minDemand, float maxDemand, float effectivePrice, float effectiveDemand, Float stepPrice, float demandFactor, 
			byte[] demandCurve, String commodity, String currency, float minPrice, float maxPrice, int priceSteps) throws SQLException {
		Integer marketBasisId = null;
		String sql = "INSERT INTO PWMEVENT.BIDDATA (CLUSTERID, AGENTID, QUALIFIER, MARKETBASISID, TIMESTAMP, MINDEMAND, MAXDEMAND, EFFECTIVEPRICE, EFFECTIVEDEMAND, STEPPRICE, DEMANDFACTOR, DEMANDCURVE) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try {
			// Insert the marketbasis and get the id
			marketBasisId = findOrInsertMarketBasis(conn, clusterId, commodity, currency, minPrice, maxPrice, priceSteps);
			
			PreparedStatement ps = conn.prepareStatement(sql);
		
			ps.setString(1, clusterId);
			ps.setString(2, agentId);
			ps.setString(3, qualifier);
			ps.setInt(4, marketBasisId);
			ps.setTimestamp(5, new Timestamp(timestamp.getTime()), UTC);
			ps.setFloat(6, minDemand);
			ps.setFloat(7, maxDemand);
			ps.setFloat(8, effectivePrice);
			ps.setFloat(9, effectiveDemand);
		
			if (stepPrice == null) {
				ps.setNull(10, java.sql.Types.FLOAT);
			} else {
				ps.setFloat(10, stepPrice);
			}
			
			ps.setFloat(11, demandFactor);
			
			if (demandCurve == null) {
				ps.setNull(12, java.sql.Types.VARCHAR);
			} else {
				ps.setBytes(12, demandCurve);
			}
						
			int count = ps.executeUpdate();
			if (count != 1) {
				// SQL failed!
				String msg = "Error inserting ({0}, {1}, {2}, {3}, {4}, {5}, {6}, {7}, {8}, {9}, {10}, {11}";
				Object args[] = new Object[] { clusterId, agentId, qualifier, marketBasisId, timestamp, minDemand, maxDemand, effectivePrice, effectiveDemand, stepPrice, demandFactor, demandCurve };
				logger.warning(this + "addBidData" + MessageFormat.format(msg, args));
			}
		} catch (SQLException e) {
			String msg = "Error inserting ({1}, {2}, {3}, {4}, {5}, {6}, {7}, {8}, {9}, {10}, {11}, {12}): {0}";
			Object args[] = new Object[] { e, clusterId, agentId, qualifier, marketBasisId, timestamp, minDemand, maxDemand, effectivePrice, effectiveDemand, stepPrice, demandFactor, demandCurve };
			logger.severe(this + "addBidData" + MessageFormat.format(msg, args));
			throw e;
		}
	}
	
	
	
	/*
	 *  MarketBasisDataCache methods.
	 */
	
	private void initMarketBasisDataCache(Connection conn) throws SQLException {
		marketBasisDataCache = new HashMap<MarketBasisData, MarketBasisData>();
		
		String sqlSelect = "SELECT * FROM PWMEVENT.MARKETBASISDATA " +
				"WHERE (CLUSTERID, COMMODITY, CURRENCY, MINPRICE, MAXPRICE, PRICESTEPS, CREATETIMESTAMP) " +
				"IN (SELECT CLUSTERID, COMMODITY, CURRENCY, MINPRICE, MAXPRICE, PRICESTEPS, MAX(CREATETIMESTAMP) AS CREATETIMESTAMP " +
				"FROM PWMEVENT.MARKETBASISDATA GROUP BY CLUSTERID, COMMODITY, CURRENCY, MINPRICE, MAXPRICE, PRICESTEPS)";
		
		PreparedStatement psSelect = conn.prepareStatement(sqlSelect);
		ResultSet rs = psSelect.executeQuery();
		MarketBasisData mbd = null;
		while (rs.next()) {
			mbd = new MarketBasisData();
			mbd.setId(rs.getInt("ID"));
			mbd.setClusterId(rs.getString("CLUSTERID"));
			mbd.setCommodity(rs.getString("COMMODITY"));
			mbd.setCurrency(rs.getString("CURRENCY"));
			mbd.setMinPrice(rs.getFloat("MINPRICE"));
			mbd.setMaxPrice(rs.getFloat("MAXPRICE"));
			mbd.setPriceSteps(rs.getInt("PRICESTEPS"));
			updateMarketBasisDataCache(mbd);
		}
	}
	
	private MarketBasisData findInMarketBasisDataCache(Connection conn, String clusterId, String commodity, String currency, float minPrice, float maxPrice, Integer priceSteps) throws SQLException {

		if (marketBasisDataCache == null) {
			initMarketBasisDataCache(conn);
		}
		
		MarketBasisData mbd = new MarketBasisData(clusterId, commodity, currency, minPrice, maxPrice, priceSteps);
		return marketBasisDataCache.get(mbd);
	}
	
	private void updateMarketBasisDataCache(MarketBasisData mbd) {
		marketBasisDataCache.put(mbd, mbd);
	}
}
