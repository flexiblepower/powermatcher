package net.powermatcher.server.telemetry.database.jdbc;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * @author IBM
 * @version 0.9.0
 */
public class EventDataJDBCAccess {

	private static final Calendar UTC = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

	private static EventDataJDBCAccess singleton;

	public static EventDataJDBCAccess singleton() {
		if (singleton == null) {
			singleton = new EventDataJDBCAccess();
		}
		
		return singleton;
	}

	/**
	 * Define the logger (Logger) field.
	 */
	private final static Logger logger = Logger.getLogger(EventDataJDBCAccess.class.getName());
	
	public void addMeasurementData(Connection conn, String clusterId, String agentId, String valueName, String units, Date ts, Float value, Integer period) throws SQLException {
		String sql = "INSERT INTO TLMY.MEASUREMENTDATA (CLUSTERID, AGENTID, VALUENAME, UNITS, TIMESTAMP, VALUE, PERIOD) values (?, ?, ?, ?, ?, ?, ?)";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
		
			ps.setString(1, clusterId);
			ps.setString(2, agentId);
			ps.setString(3, valueName);
			ps.setString(4, units);
			ps.setTimestamp(5, toTimestamp(ts), UTC);
			ps.setFloat(6, value.floatValue());
			ps.setObject(7, period, Types.INTEGER);
			int count = ps.executeUpdate();
			if (count != 1) {
				// SQL failed!
				String msg = "Error inserting ({0}, {1}, {2}, {3}, {4}, {5}, {6})";
				Object args[] = new Object[] { clusterId, agentId, valueName, units, ts, value, period };
				logWarning("addMeasurementData", MessageFormat.format(msg, args));
				
				// TODO: should an exception be thrown here? Otherwise caller would not be noticed!
			}
		} catch (SQLException e) {
			String msg = "Error inserting ({1}, {2}, {3}, {4}, {5}, {6}, {7}): {0}";
			Object args[] = new Object[] { e, clusterId, agentId, valueName, units, ts, value, period };
			logError("addMeasurementData", MessageFormat.format(msg, args));
			throw e;
		}
	}

	public void addStatusData(Connection conn, String clusterId, String agentId, String valueName, Date ts, String value) throws SQLException {
		String sql = "INSERT INTO TLMY.STATUSDATA (CLUSTERID, AGENTID, VALUENAME, TIMESTAMP, VALUE) values (?, ?, ?, ?, ?)";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
		
			ps.setString(1, clusterId);
			ps.setString(2, agentId);
			ps.setString(3, valueName);
			ps.setTimestamp(4, toTimestamp(ts), UTC);
			ps.setString(5, value);
			int count = ps.executeUpdate();
			if (count != 1) {
				// SQL failed!
				String msg = "Error inserting ({0}, {1}, {2}, {3}, {4})";
				Object args[] = new Object[] { clusterId, agentId, valueName, ts, value };
				logWarning("addStatusData", MessageFormat.format(msg, args));
			}
		} catch (SQLException e) {
			String msg = "Error inserting ({1}, {2}, {3}, {4}, {5}: {0}";
			Object args[] = new Object[] { e, clusterId, agentId, valueName, ts, value };
			logError("addStatusData", MessageFormat.format(msg, args));
			throw e;
		}
	}

	public void addControlData(Connection conn, String clusterId, String agentId, String valueName, Date ts, String value, String units) throws SQLException {
		String sql = "INSERT INTO TLMY.CONTROLDATA (CLUSTERID, AGENTID, VALUENAME, TIMESTAMP, VALUE, UNITS) values (?, ?, ?, ?, ?, ?)";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
		
			ps.setString(1, clusterId);
			ps.setString(2, agentId);
			ps.setString(3, valueName);
			ps.setTimestamp(4, toTimestamp(ts), UTC);
			ps.setString(5, value);
			ps.setString(6, units);
			int count = ps.executeUpdate();
			if (count != 1) {
				// SQL failed!
				String msg = "Error inserting ({0}, {1}, {2}, {3}, {4}, {5})";
				Object args[] = new Object[] { clusterId, agentId, valueName, ts, value, units };
				logWarning("addControlData", MessageFormat.format(msg, args));
			}
		} catch (SQLException e) {
			String msg = "Error inserting ({1}, {2}, {3}, {4}, {5}, {6}: {0}";
			Object args[] = new Object[] { e, clusterId, agentId, valueName, ts, value, units };
			logError("addControlData", MessageFormat.format(msg, args));
			throw e;
		}
			
	}

	public void addAlertData(Connection conn, String clusterId, String agentId, Date ts, String value) throws SQLException {
		String sql = "INSERT INTO TLMY.ALERTDATA (CLUSTERID, AGENTID, TIMESTAMP, VALUE) values (?, ?, ?, ?)";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
		
			ps.setString(1, clusterId);
			ps.setString(2, agentId);
			ps.setTimestamp(3, toTimestamp(ts), UTC);
			ps.setString(4, value);
			int count = ps.executeUpdate();
			if (count != 1) {
				// SQL failed!
				String msg = "Error inserting ({0}, {1}, {2}, {3})";
				Object args[] = new Object[] { clusterId, agentId, ts, value };
				logWarning("addAlertData", MessageFormat.format(msg, args));
			}
		} catch (SQLException e) {
			String msg = "Error inserting ({1}, {2}, {3}, {4}: {0}";
			Object args[] = new Object[] { e, clusterId, agentId, ts, value };
			logError("addAlertData", MessageFormat.format(msg, args));
			throw e;
		}
	}

	public void addMonitoringData(Connection conn, String clusterId, String configurationItem, String configurationItemName, String componentName, String serverName, String status, Date statusDate, Character severity) throws SQLException {
		String sql = "INSERT INTO TLMY.MONITORINGDATA (NAMESPACEID, CI_ID, CI_NAME, COMPONENT_NAME, SERVER_NAME, CI_STATUS, CI_STATUS_DATE, CI_SEVERITY) values (?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = null;
		String msg = null;
		Object args[] = null;
		try {
			ps = conn.prepareStatement(sql);
		
			ps.setString(1, clusterId);
			ps.setString(2, configurationItem);
			ps.setString(3, configurationItemName);
			ps.setString(4, componentName);
			ps.setString(5, serverName);
			ps.setString(6, status);
			ps.setTimestamp(7, toTimestamp(statusDate), UTC);
			ps.setString(8, severity.toString());
			int count = ps.executeUpdate();
			if (count != 1) {
				// SQL failed!
				msg = "Error inserting ({0}, {1}, {2}, {3}, {4}, {5}, {6}, {7})";
				args = new Object[] { clusterId, configurationItem, configurationItemName, componentName, serverName, status, statusDate, severity };
				logWarning("addMonitoringData", MessageFormat.format(msg, args));
			}
		} catch (com.ibm.websphere.ce.cm.DuplicateKeyException dke) {
			// Duplicate key exception due to equal time stamp of messages processed
			// within the same millisecond. First log warning. Then retry once and if it fails log error.
			msg = "Retrying insert after duplicate key error: ({0}, {1}, {2}, {3}, {4}, {5}, {6}, {7})";
			args = new Object[] { clusterId, configurationItem, configurationItemName, componentName, serverName, status, statusDate, severity };
			try {
				// Retry same insert.
				int count = ps.executeUpdate();
				if (count != 1) {
					// SQL failed!
					msg = "Error inserting ({0}, {1}, {2}, {3}, {4}, {5}, {6}, {7})";
					args = new Object[] { clusterId, configurationItem, configurationItemName, componentName, serverName, status, statusDate, severity };
					logWarning("addMonitoringData", MessageFormat.format(msg, args));
				}
			} catch (SQLException e) {
				msg = "Error inserting ({1}, {2}, {3}, {4}, {5}, {6}, {7}, {8}: {0})";
				args = new Object[] { e, clusterId, configurationItem, configurationItemName, componentName, serverName, status, statusDate, severity };
				logError("addMonitoringData", MessageFormat.format(msg, args));
				throw e;
			}			
		} catch (SQLException e) {
			msg = "Error inserting ({1}, {2}, {3}, {4}, {5}, {6}, {7}, {8}: {0})";
			args = new Object[] { e, clusterId, configurationItem, configurationItemName, componentName, serverName, status, statusDate, severity };
			logError("addMonitoringData", MessageFormat.format(msg, args));
			throw e;
		}
	}

	public void addRequestResponseData(Connection conn, String clusterId, String agentId, boolean isRequest, String requestType, String requestId, Date ts, String properties) throws SQLException {
		String sql = "INSERT INTO TLMY.REQUESTRESPONSEDATA (CLUSTERID, AGENTID, ISREQUEST, REQUESTTYPE, REQUESTID, TIMESTAMP, PROPERTIES) values (?, ?, ?, ?, ?, ?, ?)";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
		
			ps.setString(1, clusterId);
			ps.setString(2, agentId);
			ps.setBoolean(3, isRequest);
			ps.setString(4, requestType);
			ps.setString(5, requestId);
			ps.setTimestamp(6, toTimestamp(ts), UTC);
			ps.setString(7, properties);
			int count = ps.executeUpdate();
			if (count != 1) {
				// SQL failed!
				String msg = "Error inserting ({0}, {1}, {2}, {3}, {4}, {5}, {6})";
				Object args[] = new Object[] { clusterId, agentId, isRequest, requestType, requestId, ts, properties };
				logWarning("addRequestResponseData", MessageFormat.format(msg, args));
			}
		} catch (SQLException e) {
			String msg = "Error inserting ({1}, {2}, {3}, {4}, {5}, {6}, {7}: {0})";
			Object args[] = new Object[] { e, clusterId, agentId, isRequest, requestType, requestId, ts, properties };
			logError("addRequestResponseData", MessageFormat.format(msg, args));
			throw e;
		}
	}

	private static Timestamp toTimestamp(Date ts) {
		return new Timestamp(ts.getTime());
	}

	private void logError(String methodName, String msg) {
		logger.severe(methodName + ": " + msg);
	}

	private void logWarning(String methodName, String msg) {
		logger.warning(methodName + ": " + msg);
	}

}
