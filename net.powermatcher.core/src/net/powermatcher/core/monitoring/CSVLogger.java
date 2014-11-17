package net.powermatcher.core.monitoring;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.powermatcher.api.TimeService;
import net.powermatcher.api.monitoring.BidEvent;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.api.monitoring.AgentEvent;
import net.powermatcher.api.monitoring.PriceEvent;
import net.powermatcher.core.monitoring.BaseObserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Modified;
import aQute.bnd.annotation.component.Reference;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

/**
 * Example Observer which simply writes log entries of received events.
 */
@Component(immediate = true, designateFactory = CSVLogger.Config.class)
public class CSVLogger extends BaseObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(CSVLogger.class);

    /**
     * The header for the bidlog file
     */
    private static final String[] BID_HEADER_ROW = new String[] { "logTime", "clusterId", "id", "qualifier",
            "commodity", "currency", "minimumPrice", "maximumPrice", "minimumDemand", "maximumDemand",
            "effectiveDemand", "effectivePrice", "lastUpdateTime", "bid" };

    /**
     * The header for the pricelog file
     */
    private static final String[] PRICE_HEADER_ROW = new String[] { "logTime", "clusterId", "id", "qualifier",
            "commodity", "currency", "minimumPrice", "maximumPrice", "currentPrice", "lastUpdateTime" };

    /**
     * OSGI configuration of the {@link CSVLogger}
     */
    public static interface Config {
        @Meta.AD(
                required = false,
                description = "Filter for specific agentId's. When no filters are supplied, it will log everything.")
        List<String> filter();

        @Meta.AD(
                deflt = "agent_bid_log_'yyyyMMdd'.csv",
                description = "The pattern for the file name of the bid log file.")
        String bidlogFilenamePattern();

        @Meta.AD(
                deflt = "agent_price_log_'yyyyMMdd'.csv",
                description = "The pattern for the file name of the price log file.")
        String pricelogFilenamePattern();

        @Meta.AD(deflt = "yyyy-MM-dd HH:mm:ss", description = "The date format for the timestamps in the log.")
        String dateFormat();

        @Meta.AD(deflt = ";", description = "The field separator the logger will use.")
        String separator();

        @Meta.AD(required= true, description = "The location of the log files.")
        String logLocation();

        @Meta.AD(deflt = "30", description = "Time in seconds between file dumps.")
        long logUpdateRate();

        @Meta.AD(deflt = "csvLogger")
        String loggerId();
    }

    private List<String> filter;

    /**
     * The id of this logger instance
     */
    private String loggerId;

    /**
     * The log file {@link BidLogRecord} will be written to.
     */
    private File bidlogFile;

    /**
     * The log file {@link PriceLogRecord} will be written to
     */
    private File priceLogFile;

    /**
     * The date format for the timestamps in the log.
     */
    private DateFormat dateFormat;

    /**
     * The field separator the logger will use.
     */
    private String separator;

    /**
     * A set containing all {@link BidLogRecord} instances that haven't been written to file yet.
     */
    private BlockingQueue<BidLogRecord> bidLogRecords = new LinkedBlockingQueue<>();

    /**
     * A set containing all {@link PriceLogRecord} instances that haven't been written to file yet.
     */
    private BlockingQueue<PriceLogRecord> priceLogRecords = new LinkedBlockingQueue<>();

    /**
     * Keeps the thread alive that performs the writeLog() at a set interval
     */
    private ScheduledFuture<?> scheduledFuture;

    /**
     * Used to create a {@link ScheduledExecutorService}
     */
    private ScheduledExecutorService scheduler;

    private TimeService timeService;

    @Override
    public void update(AgentEvent event) {
        LOGGER.info("Received event: {}", event);

        if (event instanceof BidEvent) {
            BidLogRecord bidLogRecord = new BidLogRecord((BidEvent) event, timeService.currentDate(), dateFormat);
            bidLogRecords.add(bidLogRecord);
        } else if (event instanceof PriceEvent) {
            PriceLogRecord priceLogRecord = new PriceLogRecord((PriceEvent) event, timeService.currentDate(),
                    dateFormat);
            priceLogRecords.add(priceLogRecord);
        }

    }

    private void writeLogs(BlockingQueue<? extends LogRecord> records, File outputFile) {

        // TODO concurrency issues
        for (LogRecord l : records.toArray(new LogRecord[records.size()])) {
            writeLineToCSV(l.getLine(), outputFile);
            records.remove(l);
        }

        LOGGER.info("CSVLogger [{}] wrote to {}", loggerId, outputFile);
    }

    /**
     * Activate the component.
     * 
     * @param properties
     *            updated configuration properties
     */
    @Activate
    public synchronized void activate(Map<String, Object> properties) {

        Config config = Configurable.createConfigurable(Config.class, properties);
        processConfig(properties);

        scheduledFuture = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                writeLogs(bidLogRecords, bidlogFile);
                writeLogs(priceLogRecords, priceLogFile);
            }
        }, 0, config.logUpdateRate(), TimeUnit.SECONDS);

        LOGGER.info("CSVLogger [{}], activated", loggerId);
    }

    /**
     * Deactivates the component
     */
    @Deactivate
    public void deactivate() {

        scheduledFuture.cancel(false);

        LOGGER.info("CSVLogger [{}], deactivated", loggerId);
    }

    /**
     * Handle configuration modifications.
     * 
     * @param properties
     *            updated configuration properties
     */
    @Modified
    public synchronized void modified(Map<String, Object> properties) {

        processConfig(properties);
    }

    @Override
    @Reference(dynamic = true, multiple = true, optional = true)
    public void addObservable(ObservableAgent observable, Map<String, Object> properties) {
        super.addObservable(observable, properties);
    }

    @Override
    protected List<String> filter() {
        return this.filter;
    }

    private void processConfig(Map<String, Object> properties) {
        Config config = Configurable.createConfigurable(Config.class, properties);

        this.filter = config.filter();

        // ConfigAdmin will sometimes generate a filter with 1 empty element. Ignore it.
        if (filter != null && !filter.isEmpty() && filter.get(0).isEmpty()) {
            this.filter = new ArrayList<String>();
        }

        this.separator = config.separator();
        this.loggerId = config.loggerId();

        this.dateFormat = new SimpleDateFormat(config.dateFormat());

        this.priceLogFile = new File(config.logLocation() + File.separator + config.pricelogFilenamePattern());

        if (!priceLogFile.exists()) {
            writeLineToCSV(PRICE_HEADER_ROW, priceLogFile);
        }

        this.bidlogFile = new File(config.logLocation() + File.separator + config.bidlogFilenamePattern());

        if (!bidlogFile.exists()) {
            writeLineToCSV(BID_HEADER_ROW, bidlogFile);
        }

        updateObservables();
    }

    private void writeLineToCSV(String[] line, File outputFile) {

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputFile, true)))) {
            for (String s : line) {
                out.print(s + separator);
            }
            out.println();

        } catch (IOException e) {
            // TODO do something with this exception
            LOGGER.error(e.getMessage());
        }

    }

    @Reference
    public void setScheduler(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

}
