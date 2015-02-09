package net.powermatcher.integration.bids;

import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.zip.DataFormatException;

import net.powermatcher.integration.auctioneer.AuctioneerResilienceTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 *
 * @author FAN
 * @version 2.0
 */
public class SendReceiveBidTestCBQ1
    extends AuctioneerResilienceTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    /**
     * Sending an invalid bid in an agent hierarchy is difficult because the creation of a bid info instance prohibits
     * this.
     *
     * Test 1: Create a bid with a demand array that is too long.
     *
     * Expected result: Bid constructor generates an InvalidParameterException
     *
     * @throws IOException
     * @throws DataFormatException
     */
    @Test
    public void createInvalidBid1CPF1() throws IOException, DataFormatException {
        // Prepare the test for reading test input
        prepareTest("CBQ/CBQ1/Test1", null);
        assertNull(bidReader.nextBid());
    }

    /**
     * Sending an invalid bid in an agent hierarchy is difficult because the creation of a bid info instance prohibits
     * this.
     *
     * Test 2: Create a bid with a demand array that is too short.
     *
     * Expected result: Bid constructor generates an InvalidParameterException
     *
     * @throws IOException
     * @throws DataFormatException
     */
    @Test
    public void createInvalidBid2CPF1() throws IOException, DataFormatException {
        // Prepare the test for reading test input
        prepareTest("CBQ/CBQ1/Test2", null);
        assertNull(bidReader.nextBid());
    }

    /**
     * Sending an invalid bid in an agent hierarchy is difficult because the creation of a bid info instance prohibits
     * this.
     *
     * Test 3: Create a bid with a demand array that is too short.
     *
     * Expected result: Bid constructor generates an InvalidParameterException
     *
     * @throws IOException
     * @throws DataFormatException
     */
    @Test
    public void createInvalidBid3CPF1() throws IOException, DataFormatException {
        // Prepare the test for reading test input
        prepareTest("CBQ/CBQ1/Test3", null);
        assertNull(bidReader.nextBid());
    }
}
