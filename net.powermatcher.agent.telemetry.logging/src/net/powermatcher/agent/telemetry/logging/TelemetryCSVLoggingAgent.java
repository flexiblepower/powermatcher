package net.powermatcher.agent.telemetry.logging;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.powermatcher.agent.telemetry.logging.config.TelemetryCSVLoggingAgentConfiguration;
import net.powermatcher.agent.telemetry.logging.task.FileUpdateTask;
import net.powermatcher.core.agent.logging.LogRecord;
import net.powermatcher.core.agent.logging.config.CSVLoggingAgentConfiguration;
import net.powermatcher.core.configurable.service.ConfigurationService;
import net.powermatcher.core.object.ActiveObject;
import net.powermatcher.telemetry.model.data.MeasurementData;
import net.powermatcher.telemetry.model.data.MeasurementSingleValue;
import net.powermatcher.telemetry.model.data.StatusData;
import net.powermatcher.telemetry.model.data.StatusSingleValue;
import net.powermatcher.telemetry.model.data.TelemetryData;
import net.powermatcher.telemetry.service.TelemetryListenerConnectorService;
import net.powermatcher.telemetry.service.TelemetryService;

/**
 * @author IBM
 * @version 0.9.0
 */
public class TelemetryCSVLoggingAgent extends ActiveObject implements TelemetryService, TelemetryListenerConnectorService {
	/**
	 * Define the measurement log records (Map) field.
	 */
	private Map<String, LogRecord> measurementLogRecords;
	/**
	 * Define the status log records (Map) field.
	 */
	private Map<String, LogRecord> statusLogRecords;
	/**
	 * Define the measurement logging pattern (DateFormat) field.
	 */
	private DateFormat measurementLoggingPattern;
	/**
	 * Define the status logging pattern (DateFormat) field.
	 */
	private DateFormat statusLoggingPattern;
	/**
	 * Define the list separator (String) field.
	 */
	private String listSeparator;
	/**
	 * Define the logging date format (DateFormat) constant. This is the format
	 * that is understood by Excel's CSV import.
	 */
	private DateFormat loggingDateFormat;
	/**
	 * Define the future (ScheduledFuture) to control the file update task.
	 */
	private ScheduledFuture<?> fileUpdateFuture;

	/**
	 * Constructs an instance of this class.
	 * 
	 * @see #TelemetryCSVLoggingAgent(ConfigurationService)
	 */
	public TelemetryCSVLoggingAgent() {
		super();
	}

	/**
	 * Constructs an instance of this class from the specified properties
	 * parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @see #TelemetryCSVLoggingAgent()
	 */
	public TelemetryCSVLoggingAgent(final ConfigurationService configuration) {
		super(configuration);
	}

	/**
	 * Append log records with the specified logging pattern, header row and log
	 * records parameters.
	 * 
	 * @param loggingPattern
	 *            The logging pattern (<code>DateFormat</code>) parameter.
	 * @param headerRow
	 *            The header row (<code>String[]</code>) parameter.
	 * @param logRecords
	 *            The log records (<code>Map<String,LogRecord></code>)
	 *            parameter.
	 */
	private void appendLogRecords(final DateFormat loggingPattern, final String[] headerRow,
			final Map<String, LogRecord> logRecords) {
		long currentTime = getCurrentTimeMillis();
		if (currentTime != 0) {
			Date logTime = new Date(currentTime);
			String fileName = loggingPattern.format(logTime);
			File file = new File(fileName);
			boolean exists = file.exists();
			PrintStream printer = null;
			try {
				OutputStream out = new FileOutputStream(file, true);
				printer = new PrintStream(out);
				if (!exists) {
					printer.println(toCSVLine(headerRow));
				}
				synchronized (logRecords) {
					for (LogRecord logRecord : logRecords.values()) {
						printer.println(toCSVLine(logRecord.getDataRow(this.loggingDateFormat, logTime)));
						logRecord.clear();
					}
				}
			} catch (IOException e) {
				logError("Error logging to file " + fileName, e);
			} finally {
				if (printer != null) {
					printer.close();
				}
			}
		}
	}

	/**
	 * Bind.
	 * 
	 * @throws Exception
	 *             Exception.
	 */
	@Override
	public void bind() throws Exception {
	}

	/**
	 * Do file update.
	 */
	private void doFileUpdate() {
		logMeasurementLogRecords();
		logStatusLogRecords();
	}

	/**
	 * Gets the telemetry listener (TelemetryService) value.
	 * 
	 * @return The telemetry listener (<code>TelemetryService</code>) value.
	 */
	@Override
	public TelemetryService getTelemetryListener() {
		return this;
	}

	/**
	 * Initialize.
	 */
	private void initialize() {
		this.measurementLogRecords = new HashMap<String, LogRecord>();
		this.statusLogRecords = new HashMap<String, LogRecord>();
		this.measurementLoggingPattern = new SimpleDateFormat(getProperty(
				TelemetryCSVLoggingAgentConfiguration.MEASUREMENT_LOGGING_PATTERN_PROPERTY,
				TelemetryCSVLoggingAgentConfiguration.MEASUREMENT_LOGGING_PATTERN_DEFAULT));
		this.statusLoggingPattern = new SimpleDateFormat(getProperty(
				TelemetryCSVLoggingAgentConfiguration.STATUS_LOGGING_PATTERN_PROPERTY,
				TelemetryCSVLoggingAgentConfiguration.STATUS_LOGGING_PATTERN_DEFAULT));
		this.listSeparator = getProperty(CSVLoggingAgentConfiguration.LIST_SEPARATOR_PROPERTY,
				CSVLoggingAgentConfiguration.LIST_SEPARATOR_DEFAULT);
		this.loggingDateFormat = new SimpleDateFormat(getProperty(CSVLoggingAgentConfiguration.DATE_FORMAT_PROPERTY,
				CSVLoggingAgentConfiguration.DATE_FORMAT_DEFAULT));
	}

	/**
	 * Log measurement log records.
	 */
	private void logMeasurementLogRecords() {
		appendLogRecords(this.measurementLoggingPattern, MeasurementLogRecord.getHeaderRow(), this.measurementLogRecords);
	}

	/**
	 * Log status log records.
	 */
	private void logStatusLogRecords() {
		appendLogRecords(this.statusLoggingPattern, StatusLogRecord.getHeaderRow(), this.statusLogRecords);
	}

	/**
	 * Publish measurement data with the specified data parameter.
	 * 
	 * @param data
	 *            The data (<code>TelemetryData</code>) parameter.
	 */
	private void processMeasurementData(final TelemetryData data) {
		MeasurementData[] measurementData = data.getMeasurementData();
		if (measurementData.length > 0) {
			synchronized (this.measurementLogRecords) {
				for (int i = 0; i < measurementData.length; i++) {
					MeasurementSingleValue[] measurementSingleValue = measurementData[i].getSingleValues();
					for (int j = 0; j < measurementSingleValue.length; j++) {
						MeasurementLogRecord logRecord = new MeasurementLogRecord(data.getClusterId(), data.getAgentId(),
								measurementData[i].getValueName());
						logRecord.setUnits(measurementData[i].getUnits());
						logRecord.setValue(measurementSingleValue[j].getValue().toString());
						this.measurementLogRecords.put(logRecord.getKey(), logRecord);
					}
				}
			}
			if (getUpdateInterval() == 0) {
				logMeasurementLogRecords();
			}
		}
	}

	/**
	 * Publish status data with the specified data parameter.
	 * 
	 * @param data
	 *            The data (<code>TelemetryData</code>) parameter.
	 */
	private void processStatusData(final TelemetryData data) {
		StatusData[] statusData = data.getStatusData();
		if (statusData.length > 0) {
			synchronized (this.statusLogRecords) {
				for (int i = 0; i < statusData.length; i++) {
					StatusSingleValue[] statusSingleValue = statusData[i].getSingleValues();
					for (int j = 0; j < statusSingleValue.length; j++) {
						StatusLogRecord logRecord = new StatusLogRecord(data.getClusterId(), data.getAgentId(),
								statusData[i].getValueName());
						logRecord.setValue(statusSingleValue[j].getValue());
						this.statusLogRecords.put(logRecord.getKey(), logRecord);
					}
				}
			}
			if (getUpdateInterval() == 0) {
				logStatusLogRecords();
			}
		}
	}

	/**
	 * Publish generic data with the specified data parameter.
	 * 
	 * @param data
	 *            The data (<code>TelemetryData</code>) parameter.
	 */
	@Override
	public void processTelemetryData(final TelemetryData data) {
		processMeasurementData(data);
		processStatusData(data);
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

	/**
	 * Start the periodic bid update task of the agent. This method will be
	 * called when the scheduler is bound to the active object.
	 */
	@Override
	protected void startPeriodicTasks() {
		FileUpdateTask task = new FileUpdateTask() {

			@Override
			public void run() {
				try {
					doFileUpdate();
				} catch (Throwable t) {
					logError("File update failed", t);
				}
			}

		};
		this.fileUpdateFuture = getScheduler().scheduleAtFixedRate(task, 0, getUpdateInterval(), TimeUnit.SECONDS);
	}

	/**
	 * Stop the periodic bid update task of the agent. This method will be
	 * called when the scheduler is unbound from the active object.
	 */
	@Override
	protected void stopPeriodicTasks() {
		this.fileUpdateFuture.cancel(false);
	}

	/**
	 * To csvline with the specified row parameter and return the String result.
	 * 
	 * @param row
	 *            The row (<code>String[]</code>) parameter.
	 * @return Results of the to csvline (<code>String</code>) value.
	 */
	private String toCSVLine(final String[] row) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < row.length; i++) {
			if (row[i] != null) {
				sb.append(row[i]);
			}
			if (i < row.length - 1) {
				sb.append(this.listSeparator);
			}
		}
		return sb.toString();
	}

	/**
	 * Unbind.
	 */
	@Override
	public void unbind() {
	}

}
