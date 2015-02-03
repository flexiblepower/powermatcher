package net.powermatcher.core.monitoring;

import java.text.DateFormat;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.powermatcher.api.TimeService;
import net.powermatcher.api.monitoring.events.AgentEvent;
import net.powermatcher.api.monitoring.events.BidEvent;
import net.powermatcher.api.monitoring.events.PeakShavingEvent;
import net.powermatcher.api.monitoring.events.PriceUpdateEvent;
import net.powermatcher.api.monitoring.events.WhitelistEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the basic class to store incoming {@link AgentEvent}s. Subclasses of this abstract class implements their
 * specific logging method in the dumpLogs() method.
 *
 * @author FAN
 * @version 2.0
 */
public abstract class AgentEventLogger
    extends BaseObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentEventLogger.class);

    /**
     * The id of this {@link AgentEventLogger} instance
     */
    private String loggerId;

    /**
     * The {@link DateFormat} used to format the dates in a {@link LogRecord}
     */
    private DateFormat dateFormat;

    /**
     * A set containing all {@link PriceLogRecord} instances that haven't been written to file yet.
     */
    private final BlockingQueue<LogRecord> logRecords = new LinkedBlockingQueue<LogRecord>();

    /**
     * A delayed result-bearing action that can be cancelled. It's used to dump the logs at a set interval.
     */
    private ScheduledFuture<?> scheduledFuture;

    /**
     * Scheduler that can schedule commands to run after a given delay, or to execute periodically.
     */
    private ScheduledExecutorService scheduler;

    /**
     * A {@link TimeService} instance used for the logTime field of a {@link LogRecord}.
     */
    protected TimeService timeService;

    /**
     * The interval our {@link ScheduledFuture} uses.
     */
    private long logUpdateRate;

    /**
     * The {@link AgentEventType} this instance has to track
     */
    private AgentEventType eventType;

    /**
     * This method will be called by the annotated Activate() method of the subclasses.
     *
     * @param properties
     *            the configuration properties
     */
    public synchronized void baseActivate(Map<String, Object> properties) {
        processConfig(properties);
        createScheduledFuture();
    }

    /**
     * Sets the scheduledFuture
     */
    private void createScheduledFuture() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }

        scheduledFuture = scheduler.scheduleAtFixedRate(new Runnable() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void run() {
                dumpLogs();
            }
        }, 0, logUpdateRate, TimeUnit.SECONDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(AgentEvent event) {

        if (eventType.getClassType().isAssignableFrom(event.getClass())) {

            LogRecord logRecord = null;

            if (event instanceof BidEvent) {
                logRecord = new BidLogRecord((BidEvent) event, timeService.currentDate(), getDateFormat());
            } else if (event instanceof PriceUpdateEvent) {
                logRecord = new PriceUpdateLogRecord((PriceUpdateEvent) event, timeService.currentDate(),
                                                     getDateFormat());
            } else if (event instanceof WhitelistEvent) {
                logRecord = new WhitelistLogRecord((WhitelistEvent) event, timeService.currentDate(), getDateFormat(),
                                                   ((WhitelistEvent) event).getBlockedAgent());
            } else if (event instanceof PeakShavingEvent) {
                PeakShavingEvent peakShavingEvent = (PeakShavingEvent) event;
                logRecord = new PeakShavingLogRecord(peakShavingEvent,
                                                     timeService.currentDate(),
                                                     getDateFormat(),
                                                     peakShavingEvent.getFloor(),
                                                     peakShavingEvent.getCeiling(),
                                                     peakShavingEvent.getOldDemand(),
                                                     peakShavingEvent.getNewDemand(),
                                                     peakShavingEvent.getNewPrice(),
                                                     peakShavingEvent.getOldPrice());
            }

            addLogRecord(logRecord);
            getLogger().info("AgentEventLogger [{}] received event: {}", getLoggerId(), event);
        }
    }

    /**
     * This method will be called by the annotated Deactivate() method of the subclasses.
     */
    public void baseDeactivate() {
        scheduledFuture.cancel(false);
    }

    /**
     * This method will be called by the annotated Modfied() method of the subclasses.
     *
     * @param properties
     *            the configuration properties
     */
    public synchronized void baseModified(Map<String, Object> properties) {
        processConfig(properties);
        createScheduledFuture();
    }

    /**
     * This method processes the data in the Config interfaces of the subclasses
     *
     * @param properties
     *            the configuration properties
     */
    protected abstract void processConfig(Map<String, Object> properties);

    /**
     * The method where the subclass-specific logging is implemented
     */
    protected abstract void dumpLogs();

    /**
     * @param the
     *            logupdateRate to be set (in miliseconds)
     */
    protected void setLogUpdateRate(long logUpdateRate) {
        this.logUpdateRate = logUpdateRate;
    }

    /**
     * @return the current value of dateFormat
     */
    protected DateFormat getDateFormat() {
        return dateFormat;
    }

    /**
     * @param the
     *            {@link DateFormat} to be set
     */
    protected void setDateFormat(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    /**
     * @param the
     *            {@link ScheduledExecutorService} to be set
     */
    protected void setScheduler(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * @return the current value of loggerId.
     */
    protected String getLoggerId() {
        return loggerId;
    }

    /**
     * @param the
     *            loggerId to be set
     */
    protected void setLoggerId(String loggerId) {
        this.loggerId = loggerId;
    }

    /**
     * @return the current value of priceLogRecords.
     */
    protected BlockingQueue<LogRecord> getPriceLogRecords() {
        return logRecords;
    }

    /**
     * @return the current value of LOGGER.
     */
    protected static Logger getLogger() {
        return LOGGER;
    }

    /**
     * @return the current value of logRecords.
     */
    protected BlockingQueue<LogRecord> getLogRecords() {
        return logRecords;
    }

    /**
     * Removed the given {@link LogRecord} from logRecords.
     *
     * @param logRecord
     *            the {@link LogRecord} that will be removed.
     */
    protected void removeLogRecord(LogRecord logRecord) {
        logRecords.remove(logRecord);
    }

    /**
     * Adds the given {@link LogRecord} from logRecords.
     *
     * @param logRecord
     *            the new {@link LogRecord}.
     */
    protected void addLogRecord(LogRecord logRecord) {
        logRecords.add(logRecord);
    }

    /**
     * @return the current value of eventType.
     */
    protected AgentEventType getEventType() {
        return eventType;
    }

    /**
     * @param eventType
     *            the new {@link AgentEventType} this AgentEventLogger has to track.
     */
    protected void setEventType(AgentEventType eventType) {
        this.eventType = eventType;
    }
}
