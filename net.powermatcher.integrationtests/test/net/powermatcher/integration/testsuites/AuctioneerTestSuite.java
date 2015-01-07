package net.powermatcher.integration.testsuites;

import net.powermatcher.integration.auctioneer.AuctioneerResilienceAFPriceTests;
import net.powermatcher.integration.auctioneer.AuctioneerResilienceTestAF;
import net.powermatcher.integration.auctioneer.AuctioneerResilienceTestIAQ1;
import net.powermatcher.integration.auctioneer.AuctioneerResilienceTestIAQ2;
import net.powermatcher.integration.auctioneer.AuctioneerTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * 
 * @author FAN
 * @version 2.0
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ AuctioneerTest.class, AuctioneerResilienceAFPriceTests.class, AuctioneerResilienceTestAF.class,
        AuctioneerResilienceTestIAQ1.class, AuctioneerResilienceTestIAQ2.class })
public class AuctioneerTestSuite {

}
