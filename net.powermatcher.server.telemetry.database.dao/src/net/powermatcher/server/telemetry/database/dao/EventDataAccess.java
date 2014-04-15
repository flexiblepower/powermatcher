package net.powermatcher.server.telemetry.database.dao;


import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.sql.DataSource;

import net.powermatcher.server.telemetry.database.jdbc.EventDataJDBCAccess;
import net.powermatcher.telemetry.model.data.AlertData;
import net.powermatcher.telemetry.model.data.ControlData;
import net.powermatcher.telemetry.model.data.MeasurementData;
import net.powermatcher.telemetry.model.data.MeasurementSingleValue;
import net.powermatcher.telemetry.model.data.MonitoringData;
import net.powermatcher.telemetry.model.data.RequestResponseData;
import net.powermatcher.telemetry.model.data.RequestResponseProperty;
import net.powermatcher.telemetry.model.data.StatusData;
import net.powermatcher.telemetry.model.data.StatusSingleValue;
import net.powermatcher.telemetry.model.data.TelemetryData;


/**
 * @author IBM
 * @version 0.9.0
 */
public class EventDataAccess {

	private static final int MAX_LOGSTRING_LENGTH = 64;
	private static Map<String, EventDataAccess> myInstances = new HashMap<String, EventDataAccess>();
	private DataSource dataSource;
	private EventDataJDBCAccess jdbcAccess;
	
	/**
	 * Define the logger (Logger) field.
	 */
	private final static Logger logger = Logger.getLogger(EventDataAccess.class.getName());
	
	
	protected EventDataAccess(DataSource dataSource) {
		this.dataSource = dataSource;
		this.jdbcAccess = EventDataJDBCAccess.singleton();
	}
	
	public synchronized static EventDataAccess getInstance(String dataSourceName) throws Exception {
		EventDataAccess dataAccess = myInstances.get(dataSourceName);
		if (dataAccess == null) {
			DataSource dataSource = ResourceLocator.singleton().getDataSource(dataSourceName);
			if (dataSource != null) {
				dataAccess = new EventDataAccess(dataSource);
				myInstances.put(dataSourceName, dataAccess);
			}
		}
		
		return dataAccess;
	}

	public void addMeasurementData(TelemetryData message) throws SQLException {
		MeasurementData measurements[] = message.getMeasurementData();
		if (measurements.length > 0) {

			String clusterId = message.getClusterId();
			String agentId = message.getAgentId();
			try {
				
				Connection conn = this.dataSource.getConnection();
				
				for (int i = 0; i < measurements.length; i++) {
					MeasurementData measurement = measurements[i];
					String valueName = measurement.getValueName();
					String units = measurement.getUnits();
					MeasurementSingleValue singleValues[] = measurement.getSingleValues();
					for (int j = 0; j < singleValues.length; j++) {
						MeasurementSingleValue singleValue = singleValues[j];
						Date ts = singleValue.getTimestamp();
						Float value = singleValue.getValue();
						Integer period = singleValue.getPeriod();
						this.jdbcAccess.addMeasurementData(conn, clusterId, agentId, valueName, units, ts, value, period);
					}
				}
	
			} catch (SQLException e) {
				String msg = "Error inserting measurement data ({1}, {2}): {0}";
				Object args[] = new Object[] { e, clusterId, agentId };
				String errorMsg = MessageFormat.format(msg, args);
				logError("addMeasurementData", errorMsg);
				throw e;
			}
		}
		
	}

	public void addStatusData(TelemetryData message) throws SQLException {
		
		StatusData[] statuses = message.getStatusData();
		if (statuses.length > 0) {

			String clusterId = message.getClusterId();
			String agentId = message.getAgentId();
			try {
				
				Connection conn = this.dataSource.getConnection();
				
				for (int i = 0; i < statuses.length; i++) {
					StatusData status = statuses[i];
					String valueName = status.getValueName();
					StatusSingleValue[] singleValues = status.getSingleValues();
					for (int j = 0; j < singleValues.length; j++) {
						StatusSingleValue singleValue = singleValues[j];
						Date ts = singleValue.getTimestamp();
						String value = singleValue.getValue();
						this.jdbcAccess.addStatusData(conn, clusterId, agentId, valueName, ts, value);
					}
				}
	
			} catch (SQLException e) {
				String msg = "Error inserting status data ({1}, {2}): {0}";
				Object args[] = new Object[] { e, clusterId, agentId };
				String errorMsg = MessageFormat.format(msg, args);
				logError("addStatusData", errorMsg);
				throw e;
			}
		}
		
	}

	public void addControlData(TelemetryData message) throws SQLException {
		
		ControlData[] controls = message.getControlData();
		if (controls.length > 0) {

			String clusterId = message.getClusterId();
			String agentId = message.getAgentId();
			try {
				
				Connection conn = this.dataSource.getConnection();
				
				for (int i = 0; i < controls.length; i++) {
					ControlData control = controls[i];
					String valueName = control.getValueName();
					Date ts = control.getTimestamp();
					String value = control.getValue();
					String units = control.getUnits();
					this.jdbcAccess.addControlData(conn, clusterId, agentId, valueName, ts, value, units);
				}
	
			} catch (SQLException e) {
				String msg = "Error inserting status data ({1}, {2}): {0}";
				Object args[] = new Object[] { e, clusterId, agentId };
				logError("addControlData", MessageFormat.format(msg, args));
				throw e;
			}
		}
		
	}

	public void addAlertData(TelemetryData message) throws SQLException {
		
		AlertData[] alerts = message.getAlertData();
		if (alerts.length > 0) {

			String clusterId = message.getClusterId();
			String agentId = message.getAgentId();
			try {
				
				Connection conn = this.dataSource.getConnection();
				
				for (int i = 0; i < alerts.length; i++) {
					AlertData alert = alerts[i];
					Date ts = alert.getTimestamp();
					String value = alert.getValue();
					this.jdbcAccess.addAlertData(conn, clusterId, agentId, ts, value);
				}
	
			} catch (SQLException e) {
				String msg = "Error inserting alert data ({1}, {2}): {0}";
				Object args[] = new Object[] { e, clusterId, agentId };
				logError("addAlertData", MessageFormat.format(msg, args));
				throw e;
			}
		}
		
	}

	public void addRequestData(TelemetryData message) throws SQLException {
		addRequestResponseData(message, message.getRequestData(), true);
		
	}

	public void addResponseData(TelemetryData message) throws SQLException {
		addRequestResponseData(message, message.getResponseData(), false);
		
	}

	public void addMonitoringData(MonitoringData mData) throws SQLException {

		String clusterId = mData.getClusterId();
		String configurationItem = mData.getConfigurationItem();
		String configurationItemName = mData.getConfigurationItemName();
		String componentName = mData.getComponentName();
		String serverName = mData.getServerName();
		String status = mData.getStatus();
		Date statusDate = mData.getStatusDate();
		Character severity = mData.getSeverity();
		
		try {
			
			Connection conn = this.dataSource.getConnection();
			this.jdbcAccess.addMonitoringData(conn, clusterId, configurationItem, configurationItemName, componentName, serverName, status, statusDate, severity);
		} catch (SQLException e) {
			String msg = "Error inserting monitoring data ({1}, {2}, {3}, {4}, {5}, {6}, {7}, {8}): {0}";
			Object args[] = new Object[] { e, clusterId, configurationItem, configurationItemName, componentName, serverName, status, statusDate, severity };
			logError("addMonitoringData", MessageFormat.format(msg, args));
			throw e;
		}
	}
	
	private void addRequestResponseData(TelemetryData message, RequestResponseData[] requestResponses, boolean isRequest) throws SQLException {
		if (requestResponses.length > 0) {

			String clusterId = message.getClusterId();
			String agentId = message.getAgentId();
			try {
				
				Connection conn = this.dataSource.getConnection();
				
				for (int i = 0; i < requestResponses.length; i++) {
					RequestResponseData requestResponse = requestResponses[i];
					String requestType = requestResponse.getRequestType();
					String requestId = requestResponse.getRequestId();
					Date ts = requestResponse.getTimestamp();
					String properties = toLogString(requestResponse.getProperties());
					this.jdbcAccess.addRequestResponseData(conn, clusterId, agentId, isRequest, requestType, requestId, ts, properties);
				}
	
			} catch (SQLException e) {
				String msg = "Error inserting status data ({1}, {2}): {0}";
				Object args[] = new Object[] { e, clusterId, agentId };
				logError("addRequestResponseData", MessageFormat.format(msg, args));
				throw e;
			}
		}
	}

	private static String toLogString(Set<RequestResponseProperty> properties) {
		if (properties == null || properties.isEmpty()) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		
		for (Iterator<RequestResponseProperty> iterator = properties.iterator(); iterator.hasNext();) {
			RequestResponseProperty property = iterator.next();
			sb.append(property.getName());
			sb.append('=');
			Boolean logging = property.getLogging();
			if (logging == null || logging.booleanValue() == true) {
				sb.append('\'');
				sb.append(property.getValue());
				sb.append('\'');
			}
			if (iterator.hasNext()) {
				sb.append(", ");
			}
		}
		
		String result = sb.toString();
		if (result.length() > MAX_LOGSTRING_LENGTH) {
			result = result.substring(0, MAX_LOGSTRING_LENGTH - 3) + "...";
		}
		return result;
	}

	private void logError(String methodName, String msg) {
		logger.severe(methodName + ": " + msg);
	}

}
