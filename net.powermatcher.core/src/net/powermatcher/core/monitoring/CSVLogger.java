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
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.powermatcher.api.TimeService;
import net.powermatcher.api.monitoring.ObservableAgent;
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
public class CSVLogger extends AgentEventLogger {

    /**
     * The header for the bidlog file
     */
    private static final String[] BID_HEADER_ROW = new String[] { "logTime", "clusterId", "id", "qualifier",
            "commodity", "currency", "minimumPrice", "maximumPrice", "minimumDemand", "maximumDemand",
            "effectiveDemand", "effectivePrice", "lastUpdateTime", "bidNumber", "demand", "pricePoints" };

    /**
     * The header for the pricelog file
     */
    private static final String[] PRICE_HEADER_ROW = new String[] { "logTime", "clusterId", "id", "qualifier",
            "commodity", "currency", "minimumPrice", "maximumPrice", "priceValue", "lastUpdateTime" };

    /**
     * OSGI configuration of the {@link CSVLogger}
     */
    public static interface Config {
        @Meta.AD(
                required = false,
                description = "Filter for specific agentId's. When no filters are supplied, it will log everything.")
        List<String> filter();

        @Meta.AD(
                deflt = "agent_bid_log_::yyyyMMdd::.csv",
                description = "The pattern for the file name of the bid log file. Dataformat strings are placed between the delimeter '::'")
        String bidlogFilenamePattern();

        @Meta.AD(
                deflt = "agent_price_log_::yyyyMMdd::.csv",
                description = "The pattern for the file name of the price log file.")
        String pricelogFilenamePattern();

        @Meta.AD(deflt = "yyyy-MM-dd HH:mm:ss", description = "The date format for the timestamps in the log.")
        String dateFormat();

        @Meta.AD(deflt = ";", description = "The field separator the logger will use.")
        String separator();

        @Meta.AD(required = true, description = "The location of the log files.")
        String logLocation();

        @Meta.AD(deflt = "30", description = "Time in seconds between file dumps.")
        long logUpdateRate();

        @Meta.AD(deflt = "csvLogger")
        String loggerId();
    }

    /**
     * The filter containing the {@link ObservableAgent}s that have to be monitored
     */
    private List<String> filter;

    /**
     * The log file {@link BidLogRecord} will be written to.
     */
    private File bidlogFile;

    /**
     * The log file {@link PriceLogRecord} will be written to
     */
    private File priceLogFile;

    /**
     * The field separator the logger will use.
     */
    private String separator;

    /**
     * OSGi calls this method to activate a managed service.
     * 
     * @param properties
     *            the configuration properties
     */
    @Activate
    public synchronized void activate(Map<String, Object> properties) {
        super.baseActivate(properties);
    }

    /**
     * OSGi calls this method to deactivate a managed service.
     * 
     * @param properties
     *            the configuration properties
     */
    @Deactivate
    public void deactivate() {
        super.baseDeactivate();
    }

    /**
     * OSGi calls this method to modify a managed service.
     * 
     * @param properties
     *            the configuration properties
     */
    @Modified
    public synchronized void modified(Map<String, Object> properties) {
        super.baseModified(properties);
    }

    /**
     * @see BaseObserver#addObservable(ObservableAgent, Map)
     */
    @Override
    @Reference(dynamic = true, multiple = true, optional = true)
    public void addObservable(ObservableAgent observable, Map<String, Object> properties) {
        super.addObservable(observable, properties);
    }

    /**
     * @see AgentEventLogger#processConfig(Map)
     */
    @Override
    protected void processConfig(Map<String, Object> properties) {
        Config config = Configurable.createConfigurable(Config.class, properties);

        this.filter = config.filter();

        // ConfigAdmin will sometimes generate a filter with 1 empty element. Ignore it.
        if (filter != null && !filter.isEmpty() && filter.get(0).isEmpty()) {
            this.filter = new ArrayList<String>();
        }

        setLogUpdateRate(config.logUpdateRate());
        setLoggerId(config.loggerId());
        setDateFormat(new SimpleDateFormat(config.dateFormat()));
        this.separator = config.separator();

        this.priceLogFile = createLogFile(config.pricelogFilenamePattern(), config.logLocation());
        if (!priceLogFile.exists()) {
            writeLineToCSV(PRICE_HEADER_ROW, priceLogFile);
        }

        this.bidlogFile = createLogFile(config.bidlogFilenamePattern(), config.logLocation());
        if (!bidlogFile.exists()) {
            writeLineToCSV(BID_HEADER_ROW, bidlogFile);
        }

        updateObservables();
    }

    /**
     * Creates a new {@link File} to write the csv lines to. It also parses possible {@link DateFormat} strings in the
     * fileName parameter.
     * 
     * @param fileName
     *            the name of the {@link File} that has to be created
     * @param logLocation
     *            the location of the {@link File} that has to be created
     * @return The {@link File} with the fileName name and the logLocation as location
     */
    private File createLogFile(String fileName, String logLocation) {
        String newFileName = fileName;

        // in case somebody forgets the extention
        if (!fileName.endsWith(".csv")) {
            newFileName = newFileName.concat(".csv");
        }

        if (fileName.matches("\\S*::\\w*::*.csv")) {

            String logDateFormat = fileName.substring(fileName.indexOf("::") + 2, fileName.lastIndexOf("::"));
            String date = new SimpleDateFormat(logDateFormat).format(timeService.currentDate());

            Pattern pattern = Pattern.compile("::\\w*::");
            Matcher matcher = pattern.matcher(fileName);
            newFileName = matcher.replaceAll(date);
        }

        return new File(logLocation + File.separator + newFileName);
    }

    /**
     * Write a comma separated line to a specified file
     * 
     * @param line
     *            the comma separated line that has to be written to the outputFile
     * @param outputFile
     *            the csv log file where to line has to be written to
     */
    private void writeLineToCSV(String[] line, File outputFile) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outputFile, true)))) {
            StringBuilder sb = new StringBuilder();

            for (String s : line) {
                if (sb.length() > 0) {
                    sb.append(separator);
                }
                sb.append(s);
            }
            out.println(sb.toString());

        } catch (IOException e) {
            getLogger().error(e.getMessage());
        }
    }

    /**
     * OSGI calls this method to set the scheduler
     * 
     * @param scheduler
     *            The {@link ScheduledExecutorService} implementation to be injected
     */
    @Reference
    public void setScheduler(ScheduledExecutorService scheduler) {
        super.setScheduler(scheduler);
    }

    /**
     * OSGI calls this method to set the timeService
     * 
     * @param timeService
     *            The {@link TimeService} implementation to be injected
     */
    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }

    /**
     * This method goes over every {@link LogRecord} in records and calls {@link CSVLogger}
     * {@link #writeLineToCSV(String[], File)}
     * 
     * @param records
     *            the collection of {@link LogRecord}s
     * @param outputFile
     *            the {@link File} the csv lines will be written to.
     */
    private <E extends LogRecord> void writeLogs(BlockingQueue<E> records, File outputFile) {
        for (LogRecord l : records.toArray(new LogRecord[records.size()])) {
            writeLineToCSV(l.getLine(), outputFile);
            records.remove(l);
        }
        getLogger().info("CSVLogger [{}] wrote to {}", getLoggerId(), outputFile);
    }

    /**
     * @see AgentEventLogger#dumpLogs()
     */
    @Override
    protected void dumpLogs() {
        writeLogs(getBidLogRecords(), bidlogFile);
        writeLogs(getPriceLogRecords(), priceLogFile);
    }

    /**
     * @see BaseObserver#getFilter()
     */
    @Override
    protected List<String> getFilter() {
        return this.filter;
    }
}
