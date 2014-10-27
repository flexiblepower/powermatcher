package net.powermatcher.core.agent.logging;


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

import net.powermatcher.core.agent.framework.log.BidLogInfo;
import net.powermatcher.core.agent.framework.log.LogListenable;
import net.powermatcher.core.agent.framework.log.Logable;
import net.powermatcher.core.agent.framework.log.PriceLogInfo;
import net.powermatcher.core.agent.logging.config.CSVLoggingAgentConfiguration;
import net.powermatcher.core.agent.logging.task.FileUpdateTask;
import net.powermatcher.core.configurable.service.Configurable;
import net.powermatcher.core.object.ActiveObject;

/**
 * @author IBM
 * @version 0.9.0
 */
public class CSVLoggingAgent extends ActiveObject implements Logable, LogListenable {
	/**
	 * Define the PowerMatcher bid log records (Map) field.
	 */
	private Map<String, LogRecord> pwmBidLogRecords;
	/**
	 * Define the PowerMatcher price log records (Map) field.
	 */
	private Map<String, LogRecord> pwmPriceLogRecords;
	/**
	 * Define the PowerMatcher bid logging pattern (DateFormat) field.
	 */
	private DateFormat pwmBidLoggingPattern;
	/**
	 * Define the PowerMatcher price logging pattern (DateFormat) field.
	 */
	private DateFormat pwmPriceLoggingPattern;
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
	 * @see #CSVLoggingAgent(Configurable)
	 */
	public CSVLoggingAgent() {
		super();
	}

	/**
	 * Constructs an instance of this class from the specified properties
	 * parameter.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 * @see #CSVLoggingAgent()
	 */
	public CSVLoggingAgent(final Configurable configuration) {
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
		logPowerMatcherBidRecords();
		logPowerMatcherPriceRecords();
	}

	/**
	 * Gets the log listener (LogListenerService) value.
	 * 
	 * @return The log listener (<code>LogListenerService</code>) value.
	 */
	@Override
	public Logable getLogListener() {
		return this;
	}

	/**
	 * Handle bid log info with the specified bid log info parameter.
	 * 
	 * @param bidLogInfo
	 *            The bid log info (<code>BidLogInfo</code>) parameter.
	 */
	@Override
	public void logBidLogInfo(final BidLogInfo bidLogInfo) {
		BidLogRecord logRecord = new BidLogRecord(bidLogInfo);
		synchronized (this.pwmBidLogRecords) {
			this.pwmBidLogRecords.put(bidLogInfo.getAgentId(), logRecord);
		}
		if (getUpdateInterval() == 0) {
			logPowerMatcherBidRecords();
		}
	}

	/**
	 * Handle price log info with the specified price log info parameter.
	 * 
	 * @param priceLogInfo
	 *            The price log info (<code>PriceLogInfo</code>) parameter.
	 */
	@Override
	public void logPriceLogInfo(final PriceLogInfo priceLogInfo) {
		PriceLogRecord logRecord = new PriceLogRecord(priceLogInfo);
		synchronized (this.pwmPriceLogRecords) {
			this.pwmPriceLogRecords.put(priceLogInfo.getAgentId(), logRecord);
		}
		if (getUpdateInterval() == 0) {
			logPowerMatcherPriceRecords();
		}
	}

	/**
	 * Initialize.
	 */
	private void initialize() {
		this.pwmBidLogRecords = new HashMap<String, LogRecord>();
		this.pwmPriceLogRecords = new HashMap<String, LogRecord>();

		this.pwmBidLoggingPattern = new SimpleDateFormat(getProperty(
				CSVLoggingAgentConfiguration.POWERMATCHER_BID_LOGGING_PATTERN_PROPERTY,
				CSVLoggingAgentConfiguration.POWERMATCHER_BID_LOGGING_PATTERN_DEFAULT));
		this.pwmPriceLoggingPattern = new SimpleDateFormat(getProperty(
				CSVLoggingAgentConfiguration.POWERMATCHER_PRICE_LOGGING_PATTERN_PROPERTY,
				CSVLoggingAgentConfiguration.POWERMATCHER_PRICE_LOGGING_PATTERN_DEFAULT));

		this.listSeparator = getProperty(CSVLoggingAgentConfiguration.LIST_SEPARATOR_PROPERTY,
				CSVLoggingAgentConfiguration.LIST_SEPARATOR_DEFAULT);
		this.loggingDateFormat = new SimpleDateFormat(getProperty(CSVLoggingAgentConfiguration.DATE_FORMAT_PROPERTY,
				CSVLoggingAgentConfiguration.DATE_FORMAT_DEFAULT));
	}

	/**
	 * Log PowerMatcher bid records.
	 */
	private void logPowerMatcherBidRecords() {
		appendLogRecords(this.pwmBidLoggingPattern, BidLogRecord.getHeaderRow(), this.pwmBidLogRecords);
	}

	/**
	 * Log PowerMatcher price records.
	 */
	private void logPowerMatcherPriceRecords() {
		appendLogRecords(this.pwmPriceLoggingPattern, PriceLogRecord.getHeaderRow(), this.pwmPriceLogRecords);
	}

	/**
	 * Sets the configuration value.
	 * 
	 * @param configuration
	 *            The configuration (<code>ConfigurationService</code>)
	 *            parameter.
	 */
	@Override
	public void setConfiguration(final Configurable configuration) {
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
