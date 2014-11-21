package net.powermatcher.integration.auctioneer;

import java.io.IOException;
import java.util.zip.DataFormatException;

import net.powermatcher.integration.base.AuctioneerResilienceTest;

import org.junit.Test;

public class AuctioneerResilienceTestAF extends AuctioneerResilienceTest {

    /**
     * No equilibrium (demand side). Agents send series of bids with no-equilibrium price.
     * 
     * Check the equilibrium.
     * 
     * @throws IOException
     * @throws DataFormatException
     */
    @Test
    public void noEquilibriumOnDemandSideIAF1() throws IOException, DataFormatException {
        performEquilibriumTest("IAF/IAF1", null);
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
    public void noEquilibriumOnDemandSideCheckAggregatedBidIAF1() throws IOException, DataFormatException {
        performAggregatedBidTest("IAF/IAF1", null);
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
    public void noEquilibriumOnSupplySideIAF2() throws IOException, DataFormatException {
        performEquilibriumTest("IAF/IAF2", null);
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
    public void noEquilibriumOnSupplySideCheckAggregatedBidIAF2() throws IOException, DataFormatException {
        performAggregatedBidTest("IAF/IAF2", null);
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
    public void equilibriumSmallNumberOfBidsIAF3Test1() throws IOException, DataFormatException {
        performEquilibriumTest("IAF/IAF3/Test1", null);
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
    public void equilibriumSmallNumberOfBidsCheckAggregatedBidIAF3Test1() throws IOException, DataFormatException {
        performAggregatedBidTest("IAF/IAF3/Test1", null);
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
    public void equilibriumLargeSetIAF3Test2() throws IOException, DataFormatException {
        performEquilibriumTest("IAF/IAF3/Test2", null);
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
    public void equilibriumLargeSetCheckAggregatedBidIAF3Test2() throws IOException, DataFormatException {
        performAggregatedBidTest("IAF/IAF3/Test2", null);
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
    public void multipleConsecutiveEquilibriumsIAF4() throws IOException, DataFormatException {
        performEquilibriumTest("IAF/IAF4", "1");
        performEquilibriumTest("IAF/IAF4", "2");
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
    public void multipleConsecutiveEquilibriumsCheckAggregatedBidIAF4() throws IOException, DataFormatException {
        performAggregatedBidTest("IAF/IAF4", "1");
        performAggregatedBidTest("IAF/IAF4", "2");
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
    public void equilibriumWithBidRejectionIAF5() throws IOException, DataFormatException {
        performEquilibriumTest("IAF/IAF5", null);
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
    public void equilibriumWithBidRejectionCheckAggregatedBidIAF5() throws IOException, DataFormatException {
        performAggregatedBidTest("IAF/IAF5", null);
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
