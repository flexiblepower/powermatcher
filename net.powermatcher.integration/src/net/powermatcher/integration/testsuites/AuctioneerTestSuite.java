package net.powermatcher.integration.testsuites;

import net.powermatcher.integration.auctioneer.AuctioneerResilienceAFPriceTests;
import net.powermatcher.integration.auctioneer.AuctioneerResilienceTestAF;
import net.powermatcher.integration.auctioneer.AuctioneerResilienceTestIAQ1;
import net.powermatcher.integration.auctioneer.AuctioneerResilienceTestIAQ2;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ AuctioneerResilienceAFPriceTests.class, AuctioneerResilienceTestAF.class,
        AuctioneerResilienceTestIAQ1.class, AuctioneerResilienceTestIAQ2.class })
public class AuctioneerTestSuite {

}
