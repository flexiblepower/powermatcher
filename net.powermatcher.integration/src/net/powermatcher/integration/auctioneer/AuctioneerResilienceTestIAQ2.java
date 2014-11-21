package net.powermatcher.integration.auctioneer;

import java.io.IOException;
import java.util.zip.DataFormatException;

import net.powermatcher.integration.base.AuctioneerResilienceTest;

import org.junit.Test;

public class AuctioneerResilienceTestIAQ2 extends AuctioneerResilienceTest {

    /**
     * No equilibrium (demand side). Agents send series of bids with no-equilibrium price.
     * 
     * Check the equilibrium.
     * 
     * @throws IOException
     * @throws DataFormatException
     */
    @Test
    public void qualityTestIAQ2NoEquilibriumOnDemandSide() throws IOException, DataFormatException {
        performEquilibriumTest("IAQ/IAQ2/IAF1", null);
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
    public void qualityAggrgationTestIAQ2NoEquilibriumOnDemandSide() throws IOException, DataFormatException {
        performAggregatedBidTest("IAQ/IAQ2/IAF1", null);
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
    public void qualityTestIAQ2NoEquilibriumOnSupplySide() throws IOException, DataFormatException {
        performEquilibriumTest("IAQ/IAQ2/IAF2", null);
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
    public void qualityAggregationTestIAQ2NoEquilibriumOnSupplySide() throws IOException, DataFormatException {
        performAggregatedBidTest("IAQ/IAQ2/IAF2", null);
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
    public void qualityTestIAQ2EquilibriumSmallNumberOfBidsIAF3T1() throws IOException, DataFormatException {
        performEquilibriumTest("IAQ/IAQ2/IAF3/Test1", null);
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
    public void qualityAggregationTestIAQ2EquilibriumSmallNumberOfBidsIAF3T1() throws IOException, DataFormatException {
        performAggregatedBidTest("IAQ/IAQ2/IAF3/Test1", null);
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
    public void qualityTestIAQ2EquilibriumSmallNumberOfBidsIAF3T2() throws IOException, DataFormatException {
        performEquilibriumTest("IAQ/IAQ2/IAF3/Test2", null);
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
    public void qualityAggregationTestIAQ2EquilibriumSmallNumberOfBidsIAF3T2() throws IOException, DataFormatException {
        performAggregatedBidTest("IAQ/IAQ2/IAF3/Test2", null);
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
    public void qualityTestIAQ2multipleConsecutiveEquilibriums() throws IOException, DataFormatException {
        performEquilibriumTest("IAQ/IAQ2/IAF4", "1");
        performEquilibriumTest("IAQ/IAQ2/IAF4", "2");
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
    public void qualityAggregationTestIAQ2multipleConsecutiveEquilibriums() throws IOException, DataFormatException {
        performAggregatedBidTest("IAQ/IAQ2/IAF4", "1");
        performAggregatedBidTest("IAQ/IAQ2/IAF4", "2");
    }

    /**
     * Equilibrium including bid rejection.
     * 
     * Series of bids with a guaranteed equilibrium price,including an ascending bid. Expected outcome is the difened
     * expectedquilibrium price, with the ascending bid being rejected.
     * 
     * Check the equilibrium.
     * 
     * @throws IOException
     * @throws DataFormatException
     */
    @Test
    public void qualityTestIAQ2equilibriumWithBidRejection() throws IOException, DataFormatException {
        performEquilibriumTest("IAQ/IAQ2/IAF5", null);
    }

    /**
     * Equilibrium including bid rejection.
     * 
     * Series of bids with a guaranteed equilibrium price,including an ascending bid. Expected outcome is the difened
     * expectedquilibrium price, with the ascending bid being rejected.
     * 
     * 
     * Check the aggregated bid.
     * 
     * @throws IOException
     * @throws DataFormatException
     */
    @Test
    public void qualityAggregationTestIAQ2equilibriumWithBidRejection() throws IOException, DataFormatException {
        performAggregatedBidTest("IAQ/IAQ2/IAF5", null);
    }

    private void performEquilibriumTest(String testID, String suffix) throws IOException, DataFormatException {
        prepareTest(testID, suffix);

        sendBidsToMatcher();

        checkEquilibriumPrice();
    }

    private void performAggregatedBidTest(String testID, String suffix) throws IOException, DataFormatException {
        prepareTest(testID, suffix);

        sendBidsToMatcher();

        checkAggregatedBid(this.auctioneer.getAggregatedBid(this.marketBasis));
    }
}
