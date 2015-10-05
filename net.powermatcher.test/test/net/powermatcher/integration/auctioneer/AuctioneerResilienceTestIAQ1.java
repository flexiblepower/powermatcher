package net.powermatcher.integration.auctioneer;

import java.io.IOException;
import java.util.zip.DataFormatException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 *
 * @author FAN
 * @version 2.0
 */
public class AuctioneerResilienceTestIAQ1
    extends AuctioneerResilienceTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    /**
     * No equilibrium (demand side). Agents send series of bids with no-equilibrium price.
     *
     * Check the equilibrium.
     *
     * @throws IOException
     * @throws DataFormatException
     */
    @Test
    public void qualityTestIAQ1NoEquilibriumOnDemandSide() throws IOException, DataFormatException {
        performEquilibriumTest("IAQ/IAQ1/IAF1", null);
    }

    /**
     * No equilibrium (demand side). Agents send series of bids with no-equilibrium price.
     *
     * Check the aggregated bid.
     *
     * @throws IOException
     * @throws DataFormatException
     */
    @Test
    public void qualityAggrgationTestIAQ1NoEquilibriumOnDemandSide() throws IOException, DataFormatException {
        performAggregatedBidTest("IAQ/IAQ1/IAF1/", null);
    }

    /**
     * No equilibrium (supply side) Agents send series of bids with no-equilibrium price.
     *
     * Check the equilibrium.
     *
     * @throws IOException
     * @throws DataFormatException
     */
    @Test
    public void qualityTestIAQ1NoEquilibriumOnSupplySide() throws IOException, DataFormatException {
        performEquilibriumTest("IAQ/IAQ1/IAF2", null);
    }

    /**
     * No equilibrium (supply side) Agents send series of bids with no-equilibrium price.
     *
     * Check the aggregated bid.
     *
     * @throws IOException
     * @throws DataFormatException
     */
    @Test
    public void qualityAggregationTestIAQ1NoEquilibriumOnSupplySide() throws IOException, DataFormatException {
        performAggregatedBidTest("IAQ/IAQ1/IAF2", null);
    }

    /**
     * Agents send series of bids with a guaranteed equilibrium price. Scenario 1.
     *
     * Check the equilibrium.
     *
     * @throws IOException
     * @throws DataFormatException
     */
    @Test
    public void qualityTestIAQ1EquilibriumSmallNumberOfBidsTestIAF3T1() throws IOException, DataFormatException {
        performEquilibriumTest("IAQ/IAQ1/IAF3/Test1", null);
    }

    /**
     * Agents send series of bids with a guaranteed equilibrium price. Scenario 1.
     *
     * Check the aggregated bid.
     *
     * @throws IOException
     * @throws DataFormatException
     */
    @Test
    public void qualityAggregationTestIAQ1EquilibriumSmallNumberOfBidsTest3IAF3T1() throws IOException,
                                                                                    DataFormatException {
        performAggregatedBidTest("IAQ/IAQ1/IAF3/Test1", null);
    }

    /**
     * Agents send series of bids with a guaranteed equilibrium price. Scenario 2.
     *
     * Check the equilibrium.
     *
     * @throws IOException
     * @throws DataFormatException
     */
    @Test
    public void qualityTestIAQ1EquilibriumSmallNumberOfBidsTestIAF3T2() throws IOException, DataFormatException {
        performEquilibriumTest("IAQ/IAQ1/IAF3/Test2", null);
    }

    /**
     * Agents send series of bids with a guaranteed equilibrium price. Scenario 2.
     *
     * Check the aggregated bid.
     *
     * @throws IOException
     * @throws DataFormatException
     */
    @Test
    public void qualityAggregationTestIAQ1EquilibriumSmallNumberOfBidsTestIAF3T2() throws IOException,
                                                                                   DataFormatException {
        performAggregatedBidTest("IAQ/IAQ1/IAF3/Test2", null);
    }

    /**
     * Multiple consecutive equilibriums.
     *
     * Series of bids with a guaranteed equilibrium price, followed by another single bid or series of bids with another
     * guaranteed equilibrium price. The first expected equilibrium price, followed by the second expected equilibrium
     * price.
     *
     * Check the equilibrium.
     *
     * @throws IOException
     * @throws DataFormatException
     */
    @Test
    public void qualityTestIAQ1multipleConsecutiveEquilibriums() throws IOException, DataFormatException {
        performEquilibriumTest("IAQ/IAQ1/IAF4", "1");
        performEquilibriumTest("IAQ/IAQ1/IAF4", "2");
    }

    /**
     * Multiple consecutive equilibriums.
     *
     * Series of bids with a guaranteed equilibrium price, followed by another single bid or series of bids with another
     * guaranteed equilibrium price. The first expected equilibrium price, followed by the second expected equilibrium
     * price.
     *
     * Check the aggregated bid.
     *
     * @throws IOException
     * @throws DataFormatException
     */
    @Test
    public void qualityAggregationTestIAQ1multipleConsecutiveEquilibriums() throws IOException, DataFormatException {
        performAggregatedBidTest("IAQ/IAQ1/IAF4", "1");
        performAggregatedBidTest("IAQ/IAQ1/IAF4", "2");
    }

    /**
     * Equilibrium including bid rejection.
     *
     * Series of bids with a guaranteed equilibrium price,including an ascending bid. Expected outcome is the defined
     * expected equilibrium price, with the ascending bid being rejected.
     *
     * Check the equilibrium.
     *
     * @throws IOException
     * @throws DataFormatException
     */
    @Test
    public void qualityTestIAQ1equilibriumWithBidRejection() throws IOException, DataFormatException {
        performEquilibriumTest("IAQ/IAQ1/IAF5", null);
    }

    /**
     * Equilibrium including bid rejection.
     *
     * Series of bids with a guaranteed equilibrium price,including an ascending bid. Expected outcome is the defined
     * expected equilibrium price, with the ascending bid being rejected.
     *
     *
     * Check the aggregated bid.
     *
     * @throws IOException
     * @throws DataFormatException
     */
    @Test
    public void qualityAggregationTestIAQ1equilibriumWithBidRejection() throws IOException, DataFormatException {
        performAggregatedBidTest("IAQ/IAQ1/IAF5", null);
    }

    private void performEquilibriumTest(String testID, String suffix) throws IOException, DataFormatException {
        prepareTest(testID, suffix);

        sendBidsToMatcher();

        checkEquilibriumPrice();
    }

    private void performAggregatedBidTest(String testID, String suffix) throws IOException, DataFormatException {
        prepareTest(testID, suffix);

        sendBidsToMatcher();

        checkAggregatedBid(auctioneer.getAggregatedBid());
    }
}
