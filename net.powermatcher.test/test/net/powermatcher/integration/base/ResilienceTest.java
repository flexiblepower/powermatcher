package net.powermatcher.integration.base;

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.DataFormatException;

import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.Bid;
import net.powermatcher.core.bidcache.AggregatedBid;
import net.powermatcher.integration.util.CsvBidReader;
import net.powermatcher.integration.util.CsvExpectedResultsReader;
import net.powermatcher.test.helpers.TestClusterHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author FAN
 * @version 2.0
 */
public class ResilienceTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResilienceTest.class);

    private final AtomicInteger bidNumberGenerator = new AtomicInteger();

    protected TestClusterHelper cluster;
    protected CsvBidReader bidReader;
    protected CsvExpectedResultsReader resultsReader;

    protected Bid nextBidToMatcher() throws IOException, DataFormatException {
        Bid bid = bidReader.nextBid();

        if (bid != null) {
            cluster.addAgent().sendBid(bid, bidNumberGenerator.incrementAndGet());
        }

        return bid;
    }

    protected void sendBidsToMatcher() throws IOException, DataFormatException {
        AggregatedBid.Builder aggregatedDemand = new AggregatedBid.Builder(cluster.getMarketBasis());

        ArrayBid bid = null;
        int ix = 0;
        while ((bid = bidReader.nextBid()) != null) {
            aggregatedDemand.addBid(bid);
            cluster.getAgent(ix).sendBid(bid, bidNumberGenerator.incrementAndGet());
            ix++;
        }
        cluster.performTasks();

        // Write aggregated demand array
        LOGGER.info("Aggregated demand: {}", aggregatedDemand);
    }

    public String getExpectedResultsFile(String testID, String suffix) {
        String csvSuffix = null;
        if (suffix == null) {
            csvSuffix = ".csv";
        } else {
            csvSuffix = suffix + ".csv";
        }
        return "input/" + testID + "/AggBidPrice" + csvSuffix;
    }

    public String getBidInputFile(String testID, String suffix) {
        String csvSuffix = null;
        if (suffix == null) {
            csvSuffix = ".csv";
        } else {
            csvSuffix = suffix + ".csv";
        }
        return "input/" + testID + "/Bids" + csvSuffix;
    }

    protected void checkAggregatedBid(ArrayBid aggregatedBid) {
        assertArrayEquals(resultsReader.getAggregatedBid().getDemand(), aggregatedBid.getDemand(), 0);
    }
}
