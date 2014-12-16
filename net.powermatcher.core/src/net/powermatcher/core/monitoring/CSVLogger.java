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
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.powermatcher.api.TimeService;
import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.PointBid;
import net.powermatcher.api.data.PricePoint;
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
                name = "eventType",
                optionLabels = { "BIDEVENT", "PRICEEVENT" },
                description = "The AgentEventType this logger has to log.")
        AgentEventType eventType();

        @Meta.AD(
                deflt = "event_log_::yyyyMMdd::.csv",
                description = "The pattern for the file name of the log file. Dataformat strings are placed between the delimeter '::'")
        String logFilenamePattern();

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
    private File logFile;

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
        getLogger().info("CSVLogger [{}], activated", getLoggerId());
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
        getLogger().info("CSVLogger [{}], deactivated", getLoggerId());
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
        getLogger().info("CSVLogger [{}], modified", getLoggerId());
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

        setEventType(config.eventType());
        setLogUpdateRate(config.logUpdateRate());
        setLoggerId(config.loggerId());
        setDateFormat(new SimpleDateFormat(config.dateFormat()));
        this.separator = config.separator();

        this.logFile = createLogFile(config.logFilenamePattern(), config.logLocation());
        if (!logFile.exists()) {
            String[] header = null;
            if ("BidEvent".equals(getEventType().getDescription())) {
                header = BID_HEADER_ROW;
            } else if ("PriceEvent".equals(getEventType().getDescription())) {
                header = PRICE_HEADER_ROW;
            }
            if (header != null) {
                writeLineToCSV(header, logFile);
            }
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
     * @see AgentEventLogger#dumpLogs()
     */
    @Override
    protected void dumpLogs() {
        for (LogRecord logRecord : getLogRecords().toArray(new LogRecord[getLogRecords().size()])) {

            String[] output = null;

            if (logRecord instanceof BidLogRecord) {
                output = createLineForBidLogRecord((BidLogRecord) logRecord);
            } else if (logRecord instanceof PriceUpdateLogRecord) {
                output = createLineForPriceUpdateLog((PriceUpdateLogRecord) logRecord);
            }

            if (output != null) {
                writeLineToCSV(output, logFile);
            }
            removeLogRecord(logRecord);
        }
        getLogger().info("CSVLogger [{}] wrote to {}", getLoggerId(), logFile);
    }

    /**
     * @see BaseObserver#getFilter()
     */
    @Override
    protected List<String> getFilter() {
        return this.filter;
    }

    private String[] createLineForBidLogRecord(BidLogRecord logRecord) {

        Bid bid = logRecord.getBid();

        MarketBasis marketBasis = bid.getMarketBasis();

        StringBuilder demandBuilder = new StringBuilder();
        StringBuilder pricePointBuiler = new StringBuilder();

        if (bid instanceof ArrayBid) {
            ArrayBid temp = (ArrayBid) bid;

            for (Double d : temp.getDemand()) {
                if (demandBuilder.length() > 0) {
                    demandBuilder.append("#");
                }
                demandBuilder.append(d);
            }
        } else if (bid instanceof PointBid) {

            PointBid temp = (PointBid) bid;

            if (temp.getPricePoints() != null) {

                for (PricePoint p : temp.getPricePoints()) {
                    if (pricePointBuiler.length() > 0) {
                        pricePointBuiler.append("|");
                    }
                    // TODO fix this refactor

                    // int priceStep = marketBasis.toPriceStep(p.getNormalizedPrice());
                    pricePointBuiler.append(MarketBasis.PRICE_FORMAT.format(0));
                    // pricePointBuiler.append(MarketBasis.PRICE_FORMAT.format(marketBasis.toPrice(priceStep)));

                    pricePointBuiler.append("|").append(MarketBasis.DEMAND_FORMAT.format(p.getDemand()));
                }
            }
        }

        return new String[] { getDateFormat().format(logRecord.getLogTime()), logRecord.getClusterId(),
                logRecord.getAgentId(), logRecord.getQualifier().getDescription(), marketBasis.getCommodity(),
                marketBasis.getCurrency(), MarketBasis.PRICE_FORMAT.format(marketBasis.getMinimumPrice()),
                MarketBasis.PRICE_FORMAT.format(marketBasis.getMaximumPrice()),
                MarketBasis.DEMAND_FORMAT.format(bid.getMinimumDemand()),
                MarketBasis.DEMAND_FORMAT.format(bid.getMaximumDemand()),
                // TODO where/what is the "effective demand"?
                MarketBasis.DEMAND_FORMAT.format(0),
                // TODO where/what is the "effective price"?
                MarketBasis.PRICE_FORMAT.format(0), getDateFormat().format(logRecord.getEventTimestamp()),
                String.valueOf(bid.getBidNumber()), demandBuilder.toString(), pricePointBuiler.toString() };
    }

    private String[] createLineForPriceUpdateLog(PriceUpdateLogRecord logRecord) {
        MarketBasis marketbasis = logRecord.getPriceUpdate().getPrice().getMarketBasis();

        return new String[] { getDateFormat().format(logRecord.getLogTime()), logRecord.getClusterId(),
                logRecord.getAgentId(), logRecord.getQualifier().getDescription(), marketbasis.getCommodity(),
                marketbasis.getCurrency(), MarketBasis.PRICE_FORMAT.format(marketbasis.getMinimumPrice()),
                MarketBasis.PRICE_FORMAT.format(marketbasis.getMaximumPrice()),
                MarketBasis.PRICE_FORMAT.format(logRecord.getPriceUpdate().getPrice().getPriceValue()),
                getDateFormat().format(logRecord.getEventTimestamp()) };
    }
}
