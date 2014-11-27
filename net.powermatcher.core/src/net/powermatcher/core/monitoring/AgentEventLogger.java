package net.powermatcher.core.monitoring;

import java.text.DateFormat;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.powermatcher.api.TimeService;
import net.powermatcher.api.monitoring.BidEvent;
import net.powermatcher.api.monitoring.AgentEvent;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.api.monitoring.PriceEvent;
import net.powermatcher.core.monitoring.BaseObserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the basic class to store incoming {@link AgentEvent}s. Subclasses of this abstract class implements their
 * specific logging method in the dumpLogs() method.
 */
public abstract class AgentEventLogger extends BaseObserver {

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
     * A set containing all {@link BidLogRecord} instances that haven't been written to file yet.
     */
    private BlockingQueue<BidLogRecord> bidLogRecords = new LinkedBlockingQueue<>();

    /**
     * A set containing all {@link PriceLogRecord} instances that haven't been written to file yet.
     */
    private BlockingQueue<PriceLogRecord> priceLogRecords = new LinkedBlockingQueue<>();

    /**
     * Keeps the thread alive that performs the dumpLog() at a set interval
     */
    private ScheduledFuture<?> scheduledFuture;

    /**
     * Holds the {@link ScheduledExecutorService}
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
     * The method called when an {@link ObservableAgent} when they send an {@link AgentEvent}
     */
    @Override
    public void update(AgentEvent event) {
        if (event instanceof BidEvent) {
            BidLogRecord bidLogRecord = new BidLogRecord((BidEvent) event, timeService.currentDate(), dateFormat);
            bidLogRecords.add(bidLogRecord);
        } else if (event instanceof PriceEvent) {
            PriceLogRecord priceLogRecord = new PriceLogRecord((PriceEvent) event, timeService.currentDate(),
                    dateFormat);
            priceLogRecords.add(priceLogRecord);
        }
        LOGGER.info("AgentEventLogger [{}] received event: {}", loggerId, event);
    }

    /**
     * This method will be called by the annotated Activate() method of the subclasses.
     * 
     * @param properties the configuration properties
     */
    public synchronized void baseActivate(Map<String, Object> properties) {
        processConfig(properties);
        createScheduledFuture();
        LOGGER.info("AgentEventLogger [{}], activated", loggerId);
    }

    /**
     * Sets the scheduledFuture
     */
    private void createScheduledFuture() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
        }

        scheduledFuture = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                dumpLogs();
            }
        }, 0, logUpdateRate, TimeUnit.SECONDS);
    }

    /**
     * This method will be called by the annotated Deactivate() method of the subclasses.
     */
    public void baseDeactivate() {
        scheduledFuture.cancel(false);

        LOGGER.info("AgentEventLogger [{}], deactivated", loggerId);
    }

    /**
     * This method will be called by the annotated Modfied() method of the subclasses.
     * 
     * @param properties the configuration properties
     */
    public synchronized void baseModified(Map<String, Object> properties) {
        processConfig(properties);
        createScheduledFuture();

        LOGGER.info("AgentEventLogger [{}], modified", loggerId);
    }

    /**
     * This method procesloggerIdses the data in de Config interfaces of the subclasses
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
     * @param the logupdateRate to be set (in miliseconds)
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
     * @param the {@link DateFormat} to be set
     */
    protected void setDateFormat(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    /**
     * @param the {@link ScheduledExecutorService} to be set
     */
    protected void setScheduler(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * @return the current value of loggerId
     */
    protected String getLoggerId() {
        return loggerId;
    }

    /**
     * @param the loggerId to be set
     */
    protected void setLoggerId(String loggerId) {
        this.loggerId = loggerId;
    }

    /**
     * @return the current value of bidLogRecords
     */
    protected BlockingQueue<BidLogRecord> getBidLogRecords() {
        return bidLogRecords;
    }

    /**
     * @return the current value of priceLogRecords
     */
    protected BlockingQueue<PriceLogRecord> getPriceLogRecords() {
        return priceLogRecords;
    }

    /**
     * @return the current value of LOGGER
     */
    protected static Logger getLogger() {
        return LOGGER;
    }
}
