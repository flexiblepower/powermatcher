package net.powermatcher.integration.auctioneer;

import java.io.IOException;
import java.util.zip.DataFormatException;

import net.powermatcher.integration.base.AuctioneerResilienceTest;

import org.junit.Test;

/**
 * JUnit test for the Auctioneer
 * 
 */
public class AuctioneerResilienceAFPriceTests extends AuctioneerResilienceTest {

    @Test
    public void testEquilibriumRoundingIAF6() throws IOException, DataFormatException {
        performEquilibriumTest("IAF/EquilibriumTests/IAF06");
    }

    @Test
    public void testEquilibriumRoundingIAF7() throws IOException, DataFormatException {
        performEquilibriumTest("IAF/EquilibriumTests/IAF07");
    }

    @Test
    public void testEquilibriumRoundingIAF8() throws IOException, DataFormatException {
        performEquilibriumTest("IAF/EquilibriumTests/IAF08");
    }

    @Test
    public void testEquilibriumRoundingIAF9() throws IOException, DataFormatException {
        performEquilibriumTest("IAF/EquilibriumTests/IAF09");
    }

    @Test
    public void testEquilibriumRoundingIAF10() throws IOException, DataFormatException {
        performEquilibriumTest("IAF/EquilibriumTests/IAF10");
    }

    @Test
    public void testEquilibriumRoundingIAF11() throws IOException, DataFormatException {
        performEquilibriumTest("IAF/EquilibriumTests/IAF11");
    }

    @Test
    public void testEquilibriumRoundingIAF12() throws IOException, DataFormatException {
        performEquilibriumTest("IAF/EquilibriumTests/IAF12");
    }

    private void performEquilibriumTest(String testID) throws IOException, DataFormatException {
        this.performEquilibriumTest(testID, null);
    }

    private void performEquilibriumTest(String testID, String suffix) throws IOException, DataFormatException {
        prepareTest(testID, suffix);

        sendBidsToMatcher();

        checkEquilibriumPrice();
    }

}
